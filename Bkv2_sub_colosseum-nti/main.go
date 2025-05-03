package main

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"syscall"
	"time"

	"github.com/caarlos0/env"
	"github.com/redis/go-redis/v9"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

var (
	redisClient *redis.Client
	cursorKey   = "scan:cursor"
)

type ScanResponse struct {
	Cursor string `json:"cursor"`
}

type Config struct {
	Port     string `env:"PORT" envDefault:"8082"`
	RedisURL string `env:"REDIS_URL" envDefault:"redis://localhost:6379"`
}

type Server struct {
	server *http.Server
	router *http.ServeMux
	done   chan struct{}
	cfg    *Config
}

func initRedis(cfg *Config) error {
	opt, err := redis.ParseURL(cfg.RedisURL)
	if err != nil {
		return fmt.Errorf("failed to parse REDIS_URL: %v", err)
	}

	redisClient = redis.NewClient(opt)
	return nil
}

func scanRedis(ctx context.Context) error {
	// Get current cursor from Redis
	cursorStr, err := redisClient.Get(ctx, cursorKey).Result()
	if err != nil && err != redis.Nil {
		return fmt.Errorf("failed to get cursor: %v", err)
	}
	if cursorStr == "" {
		cursorStr = "0"
	}

	// Convert string cursor to uint64
	cursor, err := strconv.ParseUint(cursorStr, 0, 64)
	if err != nil {
		return fmt.Errorf("failed to parse cursor: %v", err)
	}

	// Scan Redis keys
	var keys []string
	var newCursor uint64
	keys, newCursor, err = redisClient.Scan(ctx, cursor, "", 10).Result()
	if err != nil {
		return fmt.Errorf("failed to scan: %v", err)
	}

	// Print scanned keys and their values
	for _, key := range keys {
		// Check if key was already processed
		exists, err := redisClient.SIsMember(ctx, "processed_keys", key).Result()
		if err != nil {
			log.Error().
				Err(err).
				Str("key", key).
				Msg("Error checking processed key")
			continue
		}
		if exists {
			continue // Skip already processed keys
		}

		// Check key type first
		keyType, err := redisClient.Type(ctx, key).Result()
		if err != nil {
			log.Error().
				Err(err).
				Str("key", key).
				Msg("Error getting type for key")
			continue
		}

		switch keyType {
		case "string":
			val, err := redisClient.Get(ctx, key).Result()
			if err != nil && err != redis.Nil {
				log.Error().
					Err(err).
					Str("key", key).
					Msg("Error getting value for key")
				continue
			}
			log.Info().
				Str("key", key).
				Str("value", val).
				Str("type", "string").
				Msg("Found key-value pair")
		case "hash":
			val, err := redisClient.HGetAll(ctx, key).Result()
			if err != nil && err != redis.Nil {
				log.Error().
					Err(err).
					Str("key", key).
					Msg("Error getting hash for key")
				continue
			}
			log.Info().
				Str("key", key).
				Interface("value", val).
				Str("type", "hash").
				Msg("Found hash")
		}

		// Mark key as processed
		err = redisClient.SAdd(ctx, "processed_keys", key).Err()
		if err != nil {
			log.Error().
				Err(err).
				Str("key", key).
				Msg("Error marking key as processed")
		}
	}

	// Save new cursor to Redis
	err = redisClient.Set(ctx, cursorKey, strconv.FormatUint(newCursor, 10), 0).Err()
	if err != nil {
		return fmt.Errorf("failed to save cursor: %v", err)
	}

	return nil
}

func getCurrentCursor(w http.ResponseWriter, r *http.Request) {
	cursor, err := redisClient.Get(r.Context(), cursorKey).Result()
	if err != nil && err != redis.Nil {
		log.Error().
			Err(err).
			Msg("Failed to get cursor")
		http.Error(w, "Failed to get cursor", http.StatusInternalServerError)
		return
	}

	if cursor == "" {
		cursor = "0"
	}

	log.Info().Msg("This request was processed normally")
	response := ScanResponse{Cursor: cursor}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

func NewServer(cfg *Config) (*Server, error) {
	// Configure zerolog
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
	log.Logger = zerolog.New(os.Stdout).With().Timestamp().Logger()

	if err := env.Parse(cfg); err != nil {
		return nil, fmt.Errorf("failed to parse config: %v", err)
	}

	return &Server{
		router: http.NewServeMux(),
		done:   make(chan struct{}),
		cfg:    cfg,
	}, nil
}

func (s *Server) Start() error {
	log.Info().
		Str("port", s.cfg.Port).
		Msg("Starting server")

	s.server = &http.Server{
		Addr:    ":" + s.cfg.Port,
		Handler: s.router,
	}

	go func() {
		if err := s.server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Error().
				Err(err).
				Msg("Server failed to start")
		}
	}()

	return nil
}

func (s *Server) Shutdown(ctx context.Context) error {
	// Create shutdown context with timeout
	shutdownCtx, cancel := context.WithTimeout(ctx, 10*time.Second)
	defer cancel()

	// Signal all goroutines to stop
	close(s.done)

	// Shutdown the server
	if err := s.server.Shutdown(shutdownCtx); err != nil {
		return fmt.Errorf("server shutdown failed: %v", err)
	}

	// Wait for ongoing operations to complete
	select {
	case <-shutdownCtx.Done():
		return fmt.Errorf("shutdown timeout")
	default:
		return nil
	}
}

func main() {
	cfg := &Config{}

	server, err := NewServer(cfg)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to create server")
	}

	if err := initRedis(cfg); err != nil {
		log.Fatal().Err(err).Msg("Failed to initialize Redis")
	}

	// Start periodic scanning
	go func() {
		for {
			if err := scanRedis(context.Background()); err != nil {
				log.Error().Err(err).Msg("Error scanning Redis")
			}
			time.Sleep(5 * time.Second)
		}
	}()

	// Setup HTTP server
	server.router.HandleFunc("/api/v1/cursor", getCurrentCursor)

	if err := server.Start(); err != nil {
		log.Fatal().Err(err).Msg("Server failed to start")
	}

	// Wait for interrupt signal
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	// Create context for shutdown
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Graceful shutdown
	if err := server.Shutdown(ctx); err != nil {
		log.Error().Err(err).Msg("Error during shutdown")
	}

	log.Info().Msg("Server stopped gracefully")
}

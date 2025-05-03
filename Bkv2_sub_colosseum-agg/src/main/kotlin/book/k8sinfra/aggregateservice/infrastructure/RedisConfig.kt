package book.k8sinfra.aggregateservice.infrastructure

import book.k8sinfra.aggregateservice.domain.UserScore
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private val redisHost: String = "localhost"
    @Value("\${spring.data.redis.port}")
    private val redisPort: Int = 6379

    @Bean
    fun redisConnectionFactory(): RedissonConnectionFactory {
        val config = Config().apply {
            useSingleServer().address = "redis://$redisHost:$redisPort"
        }
        return RedissonConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedissonConnectionFactory): RedisTemplate<String, UserScore> {
        val redisTemplate = RedisTemplate<String, UserScore>()
        redisTemplate.connectionFactory = redisConnectionFactory
        return redisTemplate
    }

}
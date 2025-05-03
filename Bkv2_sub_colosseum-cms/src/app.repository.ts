import Redis from 'ioredis';
import { Injectable } from '@nestjs/common';
import { redisConfig } from 'src/config/redis.config';

@Injectable()
export class RedisRepository {
	private readonly redisClient: Redis;

	constructor() {
		this.redisClient = new Redis(redisConfig);
	}

	async get(key: string) {
		return this.redisClient.get(key);
	}

	async set(key: string, value: string) {
		return this.redisClient.set(key, value);
	}
    
    async key(pattern: string) {
        return this.redisClient.keys(pattern);
    }

    async getHash(key: string) {
        return this.redisClient.hgetall(key);
    }

    async setHash(key: string, value: Object) {
        return this.redisClient.hset(key, value);
    }
}

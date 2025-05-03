import { Injectable } from '@nestjs/common';
import { RedisRepository } from './app.repository';

@Injectable()
export class AppService {
  
  constructor(
    private readonly redisRepository: RedisRepository
  ) {}

  async getPromotionTargets(judgementScore: number): Promise<Object> {
    const keys = await this.redisRepository.key('userScore:*');
    const response = {'totalCount': keys.length, 'userScores': {}};
    
    try {
      for (const key of keys) {
        const score = await this.redisRepository.getHash(key);
        if (score) {
          const userId = key.split(':')[1];
          response['userScores'][userId] = score.score;
        }
      }

      // extract score has 1000 belowed user 
      const filteredScores = {};
      for (const [userId, score] of Object.entries(response['userScores'])) {
        if (Number(score) <= judgementScore) {
          filteredScores[userId] = score;
        }
      }
      response['userScores'] = filteredScores;
      response['totalCount'] = Object.keys(filteredScores).length;

    } catch (error) {
      console.error('Error retrieving Redis hash data:', error);
      return {
        error: 'Failed to retrieve user scores'
      };
    }
    return {
      response
    };
  }
}

import { Logger, Injectable } from '@nestjs/common';
import { RedisRepository } from './app.repository';

@Injectable()
export class AppService {

  private readonly logger = new Logger(AppService.name);

  constructor(
    private readonly redisRepository: RedisRepository
  ) {}

  async getParticipateUser(): Promise<Object> {
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
    } catch (error) {
      this.logger.error('Error retrieving Redis hash data:', error);
      return {
        error: 'Failed to retrieve user scores'
      };
    }
    return {
      response
    };
  }
}

import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { RedisRepository } from './app.repository';

@Module({
  imports: [],
  controllers: [AppController],
  providers: [AppService, RedisRepository],
})
export class AppModule {}

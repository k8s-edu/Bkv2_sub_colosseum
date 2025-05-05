import { Logger ,Controller, Get, Param } from '@nestjs/common';
import { AppService } from './app.service';

@Controller("api/v1/")
export class AppController {
  private readonly logger = new Logger(AppController.name);
  constructor(private readonly appService: AppService) {}

  @Get()
  getHealthCheck(): Object {
    return {
      "message": "PROMOTION-SERVICE API 상태: OK"
    }
  }

  @Get("/promotion/target/:score")
  async getPromtionTargets(@Param('score') score: number) {
    const promotionTargetUsers = await this.appService.getPromotionTargets(score)
    this.logger.log("Get promotion target info")
    this.logger.log(`Condition: score is lower then ${score}`)
    this.logger.log("Detected normal response duration")
    return {
      "message": promotionTargetUsers,
    }
  }
}

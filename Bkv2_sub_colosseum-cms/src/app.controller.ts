import { Logger, Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';

@Controller("/api/v1/")
export class AppController {
  constructor(private readonly appService: AppService) {}

  private readonly logger = new Logger(AppController.name);

  @Get("/health")
  getHello(): Object {   
    return {
      "message": "CMS-SERVICE API 상태: OK"
    }
  }

  @Get("/participate/user")
  async getParticipateUser(): Promise<Object> {
    this.logger.log("Get users info")
    const participateUser = await this.appService.getParticipateUser()
    this.logger.log("Detected normal response duration")
    return {
      "message": participateUser
    }
  }
}

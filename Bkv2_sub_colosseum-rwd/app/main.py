from fastapi import FastAPI
import uvicorn
from pydantic import BaseModel
import random
import logging
import json

import os

# JSON 형식의 로그 포맷터 설정
class JsonFormatter(logging.Formatter):
    def format(self, record):
        log_record = {
            'time': self.formatTime(record),
            'level': record.levelname,
            'message': record.getMessage(),
        }
        return json.dumps(log_record, ensure_ascii=False)

# 로거 설정
logger = logging.getLogger("uvicorn")
logger.setLevel(logging.INFO)
handler = logging.StreamHandler()
handler.setFormatter(JsonFormatter())
logger.addHandler(handler)

app = FastAPI()

class User(BaseModel):
    user_id: int
    score: int

slot = [True, False]

@app.get("/health")
async def root():
    logger.info("Root endpoint accessed")
    return {"message": "REWARD-SERVICE API 상태: OK"}

@app.get("/api/v1/check-winner/{user_id}")
async def check_winner(user_id: int):
    
    is_winner = random.choice(slot)
    logger.info("The API processed logic going smoothly")
    result = {
        "user_id": user_id,
        "is_winner": is_winner,
        "message": "축하합니다! 당첨되셨습니다!" if is_winner else "아쉽게도 당첨되지 않았습니다."
    }
    logger.info("This request was processed normally")
    logger.info(f"Winner check completed for user {user_id}", extra={"result": result})
    return result

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8084)

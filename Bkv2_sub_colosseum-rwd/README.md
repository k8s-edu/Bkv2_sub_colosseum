# 리워드 애플리케이션

컨테이너 인프라 환경 구축을 위한 쿠버네티스/도커 책 실습 부분에서 예제로 배포되는 애플리케이션입니다. 애플리케이션은 fastapi(python 3.13)을 사용하여 작성하였으며, 요청으로 무작위로 유저 ID를 기준으로 당첨자를 추첨하는 서비스 입니다.
> Note: 이 애플리케이션을 단일로 구성하기 위해서는 python 3.13 버전 및 uv가 필요합니다.

## 로컬 개발환경
- python 편집이 가능한 에디터를 킵니다.
- 편집기에서 터미널 명령 `uv venv`로 가상환경을 설정하고 `uv run uvicorn app.main:app --host 0.0.0.0 --port 8084 --log-config logging_config.json` 으로 프로젝트를 실행할 수 있습니다.

## 컨테이너 이미지 빌드 방법
로그 데이터 확인을 위한 샘플 애플리케이션 빌드는 다음 명령어를 사용합니다.
본인이 사용하는 registry에 따라서 `[registry]` 값을 변경해야 합니다.
```shell
docker buildx build --platform linux/amd64,linux/arm64 \
-t seongjumoon/reward-service:log . \
--push
```
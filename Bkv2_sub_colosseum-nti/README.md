# 안내 애플리케이션

컨테이너 인프라 환경 구축을 위한 쿠버네티스/도커 책 실습 부분에서 예제로 배포되는 애플리케이션입니다. 애플리케이션은 Go 언어를 사용하여 작성되었으며, Redis의 키-값을 스캔하고 새로운 사용자의 등록을 로그로 기록하는 기능을 제공합니다.
> Note: 이 애플리케이션을 실행하기 위해서는 golang 1.23 버전이 필요합니다.

## 로컬 개발환경
- 정상 동작을 위해 레디스 서버가 필요합니다.
    * 레디스 접속 정보 설정은 환경변수 `REDIS_URL`로 설정해야 합니다. 이때 형식은 `redis://{HOST_OR_IP}:{PORT}`으로 입력해야합니다.
- golang 편집이 가능한 에디터를 킵니다.
- 편집기에서 제공하는 터미널 명령 입력기에 `go run main.go`으로 프로젝트를 실행할 수 있습니다.

## 컨테이너 이미지 빌드 방법
로그 데이터 확인을 위한 샘플 애플리케이션 빌드는 다음 명령어를 사용합니다.
본인이 사용하는 registry에 따라서 `[registry]` 값을 변경해야 합니다.
```shell
docker buildx build --platform linux/amd64,linux/arm64 \
-f .\Dockerfile \ 
-t seongjumoon/notice-service:log . \
--push
```

# 집계 애플리케이션

컨테이너 인프라 환경 구축을 위한 쿠버네티스/도커 책 실습 부분에서 예제로 배포되는 애플리케이션입니다. 애플리케이션은 SpringBoot 3(kotlin)을 사용하여 작성하였으며, 사용자가 요청한 대상에 점수를 집계하는 서비스를 제공합니다.
> Note: 이 애플리케이션을 실행하기 위해서는 OpenJDK 21 버전 이상이 필요합니다.

## 로컬 개발환경
- 정상 동작을 위해 레디스 서버가 필요합니다.
    * 레디스 접속 호스트 설정은 환경변수 `SPRING_DATA_REDIS_HOST`로 혹은 호스트 정보를 설정해야 합니다.
    * 레디스 접속 설정에 필요한 포트 설정은 환경변수 `SPRING_DATA_REDIS_PORT` 로 설정해야 합니다.
- intelij IDEA 또는 이에 준하는 Java IDE을 통해 프로젝트를 확인합니다.
- Gradle을 통해 프로젝트를 빌드하거나 실행합니다.

## 컨테이너 이미지 빌드 방법
로그 데이터 확인을 위한 집계 애플리케이션 빌드는 다음 명령어를 사용합니다.
본인이 사용하는 registry에 따라서 `[registry]` 값을 변경해야 합니다.
```shell
docker buildx build --platform linux/amd64,linux/arm64 \
-f .\Dockerfile.log \
-t [registry]/aggregate-service:log . \
--push
```
트레이스 데이터 확인을 위한 집계 애플리케이션 빌드는 다음 명령어를 사용합니다.
본인이 사용하는 registry에 따라서 `[registry]` 값을 변경해야 합니다.
```shell
docker buildx build --platform linux/amd64,linux/arm64 \
-f .\Dockerfile.trace \
-t  [registry]/aggregate-service:trace . \
--push
```
프로파일 데이터 확인을 위한 집계 애플리케이션 빌드는 다음 명령어를 사용합니다.
본인이 사용하는 registry에 따라서 `[registry]` 값을 변경해야 합니다.
```shell
docker buildx build --platform linux/amd64,linux/arm64 \
-f .\Dockerfile.profile \
-t [registry]/aggregate-service:profile . \
--push
```
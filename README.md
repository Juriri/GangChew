
### 강츄(GangChew)- 펀딩 강의 개설 웹 프로젝트
![img.png](src/main/resources/static/icon/main.png)
> 다양한 주제: 내가 듣고 싶은 강의를 펀딩 오픈하여 수강생을 모아보아요!

> 이런 강의 어때요?: 커뮤니티 게시판에서 다른 사람들과 관심있는 수업에 대해 얘기를 나누어보아요.

> 이 강의 너무 좋았어요!: 학습자들이 후기를 남기면 확장된 강의가 오픈될 수 있어요.</p>
### REST API
REST API를 처리하는 SpringBoot 프로젝트   


- 도메인 폴더 구조
> Controller - Service - Repository


## Structure
- **백엔드 기술**:
    - **Spring Boot**: 백엔드 서버를 구축하고 API를 개발하는 데 사용.
    - **Spring Security**: 사용자 인증 및 보안을 관리.
    - **JWT (JSON Web Tokens)**: 사용자 인증을 위한 토큰 기반 시스템을 구현하는 데 사용.
    - **JPA (Java Persistence API)**: 데이터베이스 연동을 단순화하고 ORM(Object-Relational Mapping)을 구현.
    - **Gradle**: 프로젝트 빌드 및 의존성 관리에 사용.
    - **Oracle DB**: 데이터 저장 및 관리를 위한 데이터베이스 시스템으로 사용.

```text
api-server-spring-boot
  > * build
  > gradle
  > src.main.java.com.example.final_project
    > common
        > config
          | RestTemplateConfig.java
          | SecurityConfig.java
          | SwaggerConfig.java
          | WebConfig.java
        > entity
          | BaseEntity.java
        > exceptions
          | BaseException.java
          | ExceptionAdvice.java
        > jwt
          | JwtAuthenticationFilter.java
          | JwtAuthenticationToken.java
          | JwtUtil.java
        > oauth
          | KakaoOauth.java
          | NaverOauth.java
          | OAuthService.java
          | SocialOauth.java (interfece)
        > response
          | BaseResponse.java
          | BaseResponseStatus.java
        | Constant.java
    > security
        | CustomAuthenticationProvider.java
        | CustomUserDetailService.java
    > src
      > user
        > entity
          | User.java // User Entity
        | UserController.java
        | UserService.java
        | UserRepository.java
        
      > funding
        > entity
          | Funding.java // Funding Entity
        | FundingController.java
        | FundingService.java
        | FundingRepository.java
        
      > funding_cart
        > entity
        | FundingCartController.java
        
      > funding_message
      
      > post_like
      > student_request
      
    > utils
      | JwtService.java // JWT 관련 클래스
      | SHA256.java // 암호화 알고리즘 클래스
      | ValidateRegex.java // 정규표현식 관련 클래스
    | DemoApplication // SpringBootApplication 서버 시작 지점
  > resources
    | application.yml // Database 연동을 위한 설정 값 세팅 및 Port 정의 파일

```
## ERD
![img_1.png](src/main/resources/static/icon/ERD.png)




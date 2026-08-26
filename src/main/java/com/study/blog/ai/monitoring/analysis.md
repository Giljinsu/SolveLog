# AI Error Monitoring Analysis

> 분석 전용 문서. 이 섹션이 작성된 시점 기준으로 코드 수정은 수행하지 않았다.
> 모든 근거는 실제 코드(`src/main/java/com/study/blog/**`, `src/main/resources/**`, `worker/**`)를 직접 확인한 결과이며, 파일 경로와 라인을 함께 표기한다.

---

## 1. 현재 Exception 처리 구조

`GlobalExceptionHandler`(`src/main/java/com/study/blog/GlobalExceptionHandler.java`, `@ControllerAdvice`)는 15개의 커스텀 예외를 개별 `@ExceptionHandler`로 매핑하고 있고, 공통 응답 바디는 `ErrorResponse{code, message}` (`dto/ErrorResponse.java`)다.

| 예외 | HTTP | code |
|---|---|---|
| `ExistUserException` | 409 | `DUPLICATE_USERNAME` |
| `LockedException`(Spring Security) | 423 | `LOCKED_USER` |
| `NotExistUserException` | 404 | `NOT_EXIST_USER` |
| `NotValidateEmailCode` | 404 | `NOT_VALIDATE_EMAIL_CODE` |
| `NotValidateResetToken` | 404 | `NOT_VALIDATE_RESET_TOKEN` |
| `AiGenerationException` | 502 | `AI_GENERATION_FAILED` |
| `ThumbnailJobNotFoundException` | 404 | `THUMBNAIL_JOB_NOT_FOUND` |
| `ThumbnailJobAccessDeniedException` | 403 | `THUMBNAIL_JOB_ACCESS_DENIED` |
| `ThumbnailJobInvalidStateException` | 409 | `THUMBNAIL_JOB_INVALID_STATE` |
| `RoleChangeNotAllowedException` | 409 | `ROLE_CHANGE_NOT_ALLOWED` |
| `BatchJobAlreadyRunningException` | 409 | `BATCH_JOB_ALREADY_RUNNING` |
| `PostNotFoundException` | 404 | `POST_NOT_FOUND` |
| `PostAccessDeniedException` | 403 | `POST_ACCESS_DENIED` |
| `CommentNotFoundException` | 404 | `COMMENT_NOT_FOUND` |
| `CommentAccessDeniedException` | 403 | `COMMENT_ACCESS_DENIED` |

**핵심 발견 — 이 문서 전체의 출발점이 되는 사실이다:**

1. **`Exception`/`RuntimeException`에 대한 catch-all 핸들러가 없다.** `GlobalExceptionHandler`에 `@ExceptionHandler(Exception.class)`가 존재하지 않는다. 매핑되지 않은 모든 런타임 예외(순수 `RuntimeException`, `NullPointerException`, DB 예외, Redis 예외 등)는 Spring Boot 기본 whitelabel `/error` 처리로 흘러가며, 이때는 `ErrorResponse` 형식이 아니라 Spring 기본 `{timestamp, status, error, path}` JSON(또는 HTML)이 내려간다.
2. **`GlobalExceptionHandler`에는 로그 호출이 단 한 줄도 없다.** `@Slf4j`조차 없다. 즉 지금 이 서버에서 500 에러가 발생해도 "그 요청이 500이었다"는 사실이 로그에 남는다는 보장이 없다(개별 서비스가 자체적으로 로그를 남기지 않는 한).
3. 매핑이 빠진 예외가 이미 2개 존재한다 — `DuplicateLikeException`(`LikeService.java:46`), `InvalidTokenException`(`LoginController_JwtAuth.java` 4곳). 둘 다 whitelabel 처리로 흘러간다.

**Spring Security 계층의 401/403** 은 `SecurityConfig.java`의 `exceptionHandling(...)` 람다에서 처리한다(라인 128-139). `authenticationEntryPoint`는 401 + `{"error": "Unauthorized or Token expired"}`, `accessDeniedHandler`는 403 + `{"error": "Forbidden"}`을 **직접 문자열로 write**한다 — `ErrorResponse` DTO를 쓰지 않고, 로그도 남기지 않는다. 이 경로는 `DispatcherServlet`보다 앞단(필터 체인)에서 처리되므로 `@ControllerAdvice`가 절대 개입할 수 없는 지점이라는 점이 중요하다(향후 모니터링 훅을 추가하려면 `GlobalExceptionHandler`가 아니라 이 람다 자체를 수정해야 한다).

**`JwtAuthenticationFilter`**(`service/JwtAuthenticationFilter.java`)는 토큰이 없거나 유효하지 않으면 그냥 인증 없이 필터 체인을 통과시킨다(64-67행) — 401을 던지지 않고 로그도 없다. 이후 `NotExistUserException`만 별도로 잡아 401을 수동으로 write한다(96-101행). 그 외의 예외(NPE 등)는 필터 안에서 잡히지 않고 그대로 위로 전파된다. 이 필터에도 로그 호출이 없다.

**서비스 레이어에서 직접 처리하는 예외** (요약, 상세는 근거 리서치 원본 참조):
- `S3Uploader.delete()` — 실패 시 `RuntimeException`으로 래핑 후 rethrow.
- `FileUpload.moveFileToS3TargetDir()` — `catch(Exception e)` 후 `System.err.println`만 하고 **완전히 삼킴**(rethrow 없음).
- `LoginController_JwtAuth`(`/api/refresh`) — 토큰 파싱 실패 시 원본 예외를 버리고 `InvalidTokenException`으로 치환(체이닝 없음).
- `StatisticBatchScheduler` — `BatchJobAlreadyRunningException`은 `log.warn`, 그 외는 로그 없이 `RuntimeException`으로 rethrow.
- `UserStatisticBatchService` — Redis 실패 시 `log.warn` 후 DB로 폴백(의도된 정상 동작).
- `FileService` — 두 곳에서 정리(cleanup) 후 원본 예외를 `throw e`로 재던짐(로그 없음).

**결론**: 지금 이 서비스에서 "예상하지 못한 오류"가 발생하면 (a) 사용자에게는 형식이 일관되지 않은 응답이 가고 (b) 서버 콘솔에도 남는다는 보장이 없다. AI 오류 모니터링 기능이 채워야 할 가장 근본적인 공백이 바로 이 지점이다.

---

## 2. 현재 Logging 구조

- `log.info`: 0건, `log.warn`: 7건, `log.error`: 8건, `log.debug`: 0건, `System.out/err.println`: 2건(그중 1건은 에러 처리 목적 — `FileUpload.java:192`). `@Slf4j`가 붙은 클래스는 7개뿐이며 `controller/`, `entity/`, `dto/`, `repository/`, `config/` 패키지에는 로그 호출이 전혀 없다.
- 로그가 존재하는 곳: `OpenAiClient`(GPT 오류, 7건 — 가장 로깅이 잘 되어 있는 곳), `ThumbnailJobService`(2건 warn), `StatisticBatchRunner`/`StatisticBatchScheduler`/`UserStatisticBatchService`(배치 관련), `PostService`(1건).
- `application.yml`은 프로파일 활성화만 하고 로깅 설정이 없다. `application-local.yml`/`application-prod.yml`은 **둘 다** `logging.level.org.hibernate.sql: debug` + `hibernate.show_sql: true` + `format_sql: true`를 설정하고 있다 — **prod에서도 SQL 디버그 로그가 그대로 켜져 있다.** 별도의 `logging.file`, logback 커스텀 설정, Spring Actuator 설정은 전혀 없다(`actuator` 키워드로 전체 리소스를 검색해도 0건).
- `logback-spring.xml`/`logback.xml` 없음 — Spring Boot 기본 콘솔 Logback만 사용. 파일 appender/rotation 없음.
- `Dockerfile`은 `eclipse-temurin:21-jre` 기반으로 `ENTRYPOINT ["java","-jar","app.jar"]`만 실행한다. 로그는 stdout으로만 나가고, 볼륨 마운트도 없다. `docker-compose.yml`은 리포지토리에 없다.

**정리**: SolveLog는 "로그가 거의 없는" 상태에 가깝다. 있는 로그도 GPT 클라이언트와 배치 쪽에 편중되어 있고, 정작 가장 중요한 예외 처리의 최종 관문(`GlobalExceptionHandler`)과 인증 필터에는 로그가 전혀 없다. 컨테이너 로그는 stdout뿐이라 별도 로그 파일/로테이션을 만드는 대신, **DB 기반 Error 테이블 자체를 "지속 저장소"로 사용하는 방향이 이 프로젝트의 현재 인프라와 가장 잘 맞는다**(파일 로그 인프라를 새로 구축하는 것은 이 기능의 목적과 무관하게 과한 작업이다).

---

## 3. 현재 DB / Entity 구조

`Error`, `ErrorLog`, `ApplicationError`, `SystemError`, `Incident`, `ExceptionLog`, `Monitoring` 등 유사 명칭의 Entity/Table은 **전혀 존재하지 않는다**(엔티티 패키지, Flyway 마이그레이션 V1~V12 전체 확인). 현재 엔티티는 `Alarm`, `Category`, `Comment`, `File`, `Likes`, `Post`, `PostTag`, `Tag`, `UserStatistic`, `Users`뿐이다.

관련해서 두 가지 기존 패턴이 참고할 만하다:
- `Users.isDeleted`(`character varying(1)`, `'y'/'n'`), Flyway로 컬럼을 점진적으로 추가해온 이력(V3~V9) — 새 테이블도 이 컨벤션(`snake_case` 컬럼, `*_id` PK, `BasicDate`로 `created_date`/`last_modified_date` 상속)을 따르는 것이 일관적이다.
- `ThumbnailJob`은 JPA 엔티티가 **아니라** Redis에 JSON으로 직렬화되는 순수 POJO(`ai/thumbnail/domain/ThumbnailJob.java`)다. 상태 전이(`QUEUED→PROCESSING→COMPLETED/FAILED`)를 도메인 객체 스스로 검증하고 위반 시 전용 예외(`ThumbnailJobInvalidStateException`)를 던지는 패턴은, 이번 Error 저장 구조에서도 "저장 여부/분석 여부 상태"를 다루는 데 그대로 참고할 수 있다. 다만 Error 데이터는 (a) 영구 보존이 필요하고 (b) 관리자 페이지에서 검색/페이지네이션 조회가 필요하므로 Redis POJO가 아니라 **JPA 엔티티 + PostgreSQL 테이블**이 맞다.

다음 마이그레이션 번호는 **V13**이다.

---

## 4. 현재 Admin 구조

`com.study.blog.admin` 패키지는 `controller/`, `service/`, `dto/` 3개 하위 패키지로 구성되어 있고, 다음 컨벤션을 일관되게 따른다:

- Controller: `Admin{명사}Controller`, `@RestController`, `@RequestMapping("/api/admin/{복수명사}")`, 클래스 상단에 `// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능` 주석.
- Service: `Admin{명사}Service`, `@Service @RequiredArgsConstructor @Transactional(readOnly = true)` 클래스 레벨, 변경 메서드만 메서드 레벨 `@Transactional`로 오버라이드. **삭제 등 변경 로직은 직접 구현하지 않고 기존 도메인 서비스의 `*ByAdmin` 메서드(`postService.deletePostByAdmin(...)`, `commentService.deleteCommentByAdmin(...)`)를 호출**하는 위임 패턴이 확립되어 있다.
- DTO: `Admin{명사}ResponseDto`, `@Getter` + 명시적 all-args 생성자(QueryDSL `Projections.constructor`용).

한 가지 중요한 구조적 관찰: **배치 관리 기능(`StatisticBatchAdminController`, `BatchStatusResponseDto`)은 `com.study.blog.admin` 밑이 아니라 `com.study.blog.batch.controller`/`.dto`에 있다.** 즉 이 프로젝트는 "관리자 전용 기능이라고 해서 무조건 `admin` 패키지에 몰아넣지 않고, 기능의 실체가 있는 도메인 패키지(`batch`, `ai.thumbnail`)에 두고 `admin`은 어디까지나 기존 도메인(Users/Post/Comment)을 관리자 시점에서 조회·조작하는 얇은 래퍼"라는 컨벤션을 갖고 있다. AI 오류 모니터링처럼 그 자체로 실체가 있는 새 기능(캡처/분류/저장/AI 분석 엔진)은 `batch`, `ai`와 동급의 **새 최상위 패키지**(`com.study.blog.monitoring`)로 만들고, 그 위에 얇은 `admin.controller.AdminMonitoringController` + `admin.service.AdminMonitoringService`를 얹는 것이 기존 컨벤션과 가장 잘 맞는다(§16에서 구체화).

프론트엔드는 `AdminLayout.jsx`가 라우트 가드(비-ADMIN은 UI 자체를 렌더링하지 않음, 최종 보안은 백엔드 `/api/admin/**`)와 좌측 네비게이션(Dashboard/Users/Posts/Comments/Operations)을 갖고 있고, 각 페이지는 `page/admin/Admin{명사}.jsx` + 동명 `.css`, 검색폼 + 테이블 + 페이지네이션의 동일한 구조를 반복 사용한다. `/admin/monitoring` 페이지도 이 틀을 그대로 재사용할 수 있다.

---

## 5. 기존 GPT API 구조

GPT 연동은 이미 **완전히 재사용 가능한 형태로 캡슐화**되어 있다 — 새 Monitoring 기능은 이 클라이언트에 메서드 하나만 추가하면 된다.

- 클라이언트: `client/OpenAiClient.java` (`@Slf4j`, `@Component`). `RestTemplate`(`RestTemplateBuilder`) 기반 **동기(블로킹)** HTTP 호출. `openai.api-key`(env `OPENAI_API_KEY`), `openai.model`(기본 `gpt-5-mini`), `connect-timeout-ms`(5000), `read-timeout-ms`(90000)를 생성자에서 주입받는다. 엔드포인트는 상수로 하드코딩된 `https://api.openai.com/v1/chat/completions`.
- 시스템 프롬프트는 `src/main/resources/prompts/*.txt`로 분리되어 있고, `@PostConstruct`에서 클래스패스로부터 1회만 읽어 메모리에 캐시한다(`post-draft-system-prompt.txt`, `thumbnail-prompt-system-prompt.txt`). 사용자 프롬프트는 각 호출부 서비스(`AiPostGenerationService`, `ThumbnailJobService`)가 매 요청마다 문자열로 조립한다.
- 공통 private 메서드 `chatComplete(systemPrompt, userPrompt)`가 요청 조립/호출/예외 변환/응답 파싱을 전부 담당하고, `generatePostDraft(...)`/`generateThumbnailPrompt(...)` 두 public 메서드가 이를 감싸는 구조다.
- 오류 처리: `HttpClientErrorException.Unauthorized`/`TooManyRequests`/그 외 4xx/`HttpServerErrorException`/`ResourceAccessException`(타임아웃)/`RestClientException`(그 외) 각각을 `log.error`로 기록한 뒤 **전부 동일하게 `AiGenerationException`으로 변환**한다. `GlobalExceptionHandler`가 이를 502로 매핑한다.
- 응답은 OpenAI 표준 봉투(choices[0].message.content)만 Jackson record로 역직렬화하고, `content` 자체는 항상 **자유 텍스트**로 받는다 — OpenAI의 JSON 모드(`response_format`)는 쓰지 않고, JSON이 필요할 때는 시스템 프롬프트 지시만으로 강제한다(썸네일 프롬프트 케이스). 즉 **구조화된 JSON 응답이 필요하면 호출부에서 직접 파싱해야 한다** — Monitoring 기능에서 `{summary, possibleCause, impact, solution, checkPoints, severity}` 같은 JSON을 받으려면 동일한 방식(시스템 프롬프트로 JSON 강제 + 호출부에서 Jackson으로 파싱, 파싱 실패 시 방어 코드 필요)을 따라야 한다.

**재사용 방법**: `OpenAiClient`에 새 시스템 프롬프트 파일(`error-analysis-system-prompt.txt`)과 `analyzeError(userPrompt)` 메서드만 추가하면, API 키/타임아웃/RestTemplate/예외 변환 로직을 전부 그대로 재사용할 수 있다. **별도의 GPT 클라이언트를 새로 만들 필요가 전혀 없다.**

---

## 6. 발견된 문제점

1. **(최우선)** `GlobalExceptionHandler`에 catch-all이 없고 로깅도 없다 — 지금 이 순간 500 에러가 나도 아무 기록도 남지 않을 수 있다. Monitoring 기능의 가장 자연스러운 진입점이자, 이 기능 없이도 그 자체로 고쳐야 할 결함이다.
2. `DuplicateLikeException`, `InvalidTokenException`이 `GlobalExceptionHandler`에 매핑되어 있지 않아 whitelabel 응답이 나간다.
3. `JwtAuthenticationFilter`가 유효하지 않은 토큰을 조용히 통과시키고, 어떤 로그도 남기지 않는다(64-67행).
4. prod 환경에서도 `hibernate.show_sql`/SQL debug 로그가 켜져 있다 — Monitoring 기능이 도입되면 콘솔 로그 볼륨이 더 늘어나므로, 이 설정을 끄는 것이 새 기능의 신호 대 잡음비를 위해 함께 검토할 가치가 있다(다만 이번 기능의 필수 전제조건은 아니다).
5. `RedisConfiguration`이 읽는 `spring.data.redis.ssl` 플랫 키와 yml의 `spring.data.redis.ssl.enabled` 중첩 키가 불일치한다 — 현재는 항상 기본값(false)으로 떨어지는 잠재적 dead config로 보인다(이번 기능과 직접 관련은 없지만, Redis 관련 코드를 만질 때 함께 인지해둘 사항).
6. MDC/traceId/requestId 상관관계 메커니즘이 전혀 없다 — 로그와 향후 Error 레코드를 연결할 수단이 없다.
7. Trace/Error 저장을 위한 Entity/Table이 전혀 없다 — 완전히 새로 설계해야 한다.
8. Spring `@Async`/`@EnableAsync`/이벤트 발행(`ApplicationEventPublisher`)이 프로젝트 전체에서 단 한 번도 쓰인 적이 없다 — 비동기 처리 방식을 도입한다면 이 프로젝트에서는 **처음** 도입하는 패턴이 된다(반대로 `@Transactional(propagation = REQUIRES_NEW)`는 `FileService`에 이미 2곳 선례가 있다).

---

## 7. Error 수집 대상 / 제외 대상

**HTTP 상태 코드 단독으로 판단하지 않는다.** 이유: 이 프로젝트는 이미 "비즈니스적으로 예상된 실패"를 404/403/409로 정교하게 매핑해두었다(§1 표 참고) — 상태 코드만 보면 409(`RoleChangeNotAllowedException` vs `BatchJobAlreadyRunningException` vs 향후 진짜 동시성 버그)를 구분할 수 없다. **예외 타입 기준으로 판단**하고, `GlobalExceptionHandler`의 매핑 여부 자체를 1차 분류 기준으로 삼는 것이 가장 유지보수하기 좋다:

```
Exception 발생
   ↓
GlobalExceptionHandler가 "구체적으로 매핑한" 예외인가?
   ├─ Yes → 기본은 IGNORE (비즈니스적으로 예상된 흐름)
   │         단, 명시적 화이트리스트(RECORD 대상)에 있으면 RECORD/RECORD_AND_AI_ANALYZE
   │         예: AiGenerationException (인프라성 실패이므로 기록 가치 있음)
   └─ No  → 매핑되지 않은 모든 예외 = RECORD_AND_AI_ANALYZE (기본값)
             지금 이 상태가 whitelabel로 새어나가던 바로 그 예외들이다
```

- **저장하지 않을 예외(IGNORE, 기본값)**: `ExistUserException`, `LockedException`, `NotExistUserException`, `NotValidateEmailCode`, `NotValidateResetToken`, `ThumbnailJobNotFoundException`, `ThumbnailJobAccessDeniedException`, `ThumbnailJobInvalidStateException`, `RoleChangeNotAllowedException`, `BatchJobAlreadyRunningException`, `PostNotFoundException`, `PostAccessDeniedException`, `CommentNotFoundException`, `CommentAccessDeniedException`, `DuplicateLikeException`, `InvalidTokenException`, 그리고 필터 단의 401/403(§1 참고 — 애초에 `GlobalExceptionHandler`를 거치지 않으므로 이 파이프라인 대상이 아니다).
- **AI 분석까지는 아니어도 저장은 고려(RECORD)**: 명시적 화이트리스트 없이 여기에 해당하는 항목은 현재 없음 — MVP에서는 "매핑 안 됨 = 곧 AI 분석 대상"으로 단순화한다(2단계 분류를 두면 운영 초기부터 "왜 이건 RECORD인데 분석 안 됐지"라는 혼란만 늘어난다).
- **저장 + AI 분석(RECORD_AND_AI_ANALYZE)**: (a) `GlobalExceptionHandler`에 매핑되지 않은 모든 예외(신설할 catch-all로 흡수), (b) `AiGenerationException`(GPT 자체 실패 — 단, §9의 순환 방지 규칙 적용), (c) `StatisticBatchRunner`/`StatisticBatchScheduler`가 지금 `log.error`만 하고 흘려보내는 배치 실패(§3 배치 리서치 참고 — 현재 이 실패는 로그에만 남고 관리자 페이지 어디에도 알림이 가지 않는다).

이 정책은 코드 한 곳(`ErrorClassifier` 또는 유사 이름의 작은 컴포넌트, `Map<Class<?>, ErrorAction>` 화이트리스트 + 기본값 로직)에만 존재해야 하며, 새 예외를 추가할 때마다 이 한 곳만 고치면 되도록 만든다.

---

## 8. Error 데이터 모델 후보

**Stack Trace 전체 저장 여부**: 저장하지 않는다. 전체 스택은 수십 프레임까지 내려가며 대부분 프레임워크 내부 프레임이라 AI 분석에도 큰 도움이 안 되고 DB 용량만 키운다. **상위 N개 프레임(예: 20개, 애플리케이션 패키지 `com.study.blog` 우선 필터링)만 텍스트로 잘라 저장**하고, 원한다면 나머지는 stdout 로그(§2 — 어차피 Docker가 stdout을 갖고 있음)에 맡긴다.

**Request Body 저장 여부**: 기본적으로 저장하지 않는다. 이 프로젝트는 로그인/회원가입/비밀번호 재설정 요청 바디에 `password`, `authCode`, 재설정 토큰 등 민감정보가 그대로 담기고(`UserRequestDto`, `ResetTokenDto` 등), JWT는 httpOnly 쿠키로 오가므로 바디보다는 **쿠키/Authorization 헤더 값이 절대 저장되면 안 되는 항목**이다. `requestUri`, `httpMethod`, `httpStatus`는 저장하되 쿼리스트링에 토큰류가 실리는 엔드포인트(`/api/resetPassword` 등)는 쿼리스트링도 마스킹하거나 저장 대상에서 제외한다.

**사용자 정보 범위**: `userId`, `username`은 저장(이미 관리자 페이지들이 username/nickname을 노출하는 것과 동일 수준의 민감도). `ip`는 저장 가능(운영 트러블슈팅에 필요). 비밀번호/토큰/쿠키/Authorization 헤더는 **절대 저장 금지** — 캡처 지점에서 화이트리스트 방식으로 필요한 필드만 뽑아 담고, "제외 필드 블랙리스트" 방식은 쓰지 않는다(새 민감 필드가 추가돼도 자동으로 안전하도록).

**DB 크기 / 보존 정책**: §9에서 다룰 fingerprint 기반 중복 억제가 크기 문제의 1차 해법이다. 추가로 `ErrorEvent`(발생 이력)는 오래된 것부터 주기적으로 삭제(예: 90일)하는 배치를 향후 확장으로 둔다 — 이미 `StatisticBatchRunner` 같은 수동/스케줄 배치 인프라가 있으므로 같은 패턴을 재사용하면 된다. `ErrorIssue`(그룹 단위 요약)는 삭제하지 않고 계속 보존해도 크기 부담이 작다.

**필드 후보** (§9의 Issue/Event 분리 구조 전제):

```
error_issue                          error_event
------------------------------       ------------------------------
issue_id (PK)                        event_id (PK)
fingerprint (unique)                 issue_id (FK)
exception_class                      occurred_at
first_message                        request_uri
severity                             http_method
status            (OPEN/RESOLVED)    http_status
first_occurred_at                    user_id (nullable)
last_occurred_at                     username (nullable)
occurrence_count                     ip
                                      stack_trace_excerpt (top N frames)
ai_analysis_status (PENDING/DONE/FAILED/SKIPPED)
ai_summary
ai_possible_cause
ai_impact
ai_solution
ai_check_points   (text, JSON 배열 직렬화)
analyzed_at
```

`status`(OPEN/RESOLVED)는 사용자가 예시로 준 필드에는 없었지만, 관리자가 "이미 확인/조치한 이슈"를 구분할 최소 장치로 추가 제안한다 — 반복 발생하는 이슈를 관리자 페이지에서 계속 미확인 취급하지 않기 위함이다.

---

## 9. 중복 Error 처리 전략

**Issue/Event 2테이블 분리를 채택한다** (§8 모델). 이유: 이 프로젝트는 트래픽이 몰릴 때 동일 원인의 예외가 짧은 시간에 대량 발생할 수 있는 코드가 이미 존재한다(예: `S3Uploader`, `FileService`의 반복 루프 내 실패). Event를 Issue와 분리하지 않으면 "저장은 됐지만 GPT 분석 비용이 발생 횟수만큼 청구되는" 문제를 피할 수 없다 — 이는 MVP를 단순화해서 얻는 이득보다 비용 통제 실패의 리스크가 더 크므로, 이 부분만큼은 "과한 설계"가 아니라 **핵심 요구사항**으로 본다.

**Fingerprint 생성 규칙**:
```
fingerprint = SHA-256(
    exceptionClass.getName()
    + ":" + topApplicationFrame   // com.study.blog.* 로 시작하는 첫 스택 프레임 (class#method:line)
    + ":" + normalizedMessage     // 메시지에서 숫자/UUID/이메일 등 가변 토큰을 정규식으로 치환한 버전
)
```
`exceptionClass`만 쓰면 서로 다른 원인의 동일 예외 타입이 하나로 뭉쳐지고, 스택 전체를 쓰면 프레임워크 버전 차이 등으로 사소하게 갈라진 같은 원인이 다른 fingerprint가 된다. **"애플리케이션 코드 프레임 1개 + 정규화된 메시지"** 조합이 이 프로젝트 규모에서 가장 실용적이다.

**동작**: 예외 발생 → fingerprint 계산 → 동일 fingerprint의 `ErrorIssue`가 있으면 `occurrence_count++`, `last_occurred_at` 갱신, `ErrorEvent` 1건 추가만 하고 **AI 재분석은 하지 않음**(이미 `ai_analysis_status = DONE`이면 스킵). 없으면 새 `ErrorIssue` 생성 + AI 분석 큐잉. 이렇게 하면 "같은 NPE가 수백 번 나도 GPT 호출은 최초 1회"가 보장된다.

**재분석 트리거(향후 확장, MVP 아님)**: 동일 이슈가 오래 잠잠하다가(예: 7일) 다시 발생하면 "재발"로 간주해 재분석하는 옵션은 유용하지만, MVP에서는 "한 번 분석된 Issue는 관리자가 수동으로 재분석 버튼을 누르기 전까지 다시 분석하지 않는다"로 단순화한다.

---

## 10. AI 분석 구조

§5에서 확인했듯 `OpenAiClient`를 그대로 재사용한다. 새로 만들 것:
- `prompts/error-analysis-system-prompt.txt`: "너는 Spring Boot 서버 오류를 분석하는 어시스턴트다. 아래 예외 정보를 보고 반드시 JSON만 출력하라: {summary, possibleCause, impact, solution, checkPoints(배열), severity(LOW/MEDIUM/HIGH/CRITICAL)}" 형태 — `thumbnail-prompt-system-prompt.txt`가 이미 "JSON만 출력하라"는 동일한 패턴을 쓰고 있으므로 그 문체를 그대로 따른다.
- `OpenAiClient.analyzeError(userPrompt)`: 기존 `chatComplete`를 재사용하는 3번째 public 메서드.
- 사용자 프롬프트 조립: `exceptionClass + message + stackTraceExcerpt + requestUri + httpMethod + occurrenceCount`를 하나의 텍스트로 구성.
- 응답 파싱: `OpenAiClient`가 돌려주는 것은 여전히 자유 문자열이므로, 호출부(`ErrorAnalysisService`)에서 Jackson `ObjectMapper.readValue(content, ErrorAnalysisResult.class)`로 파싱하고, **파싱 실패 시 예외를 던지지 않고** `aiAnalysisStatus = FAILED`, `aiSummary = "AI 응답 파싱 실패"` 정도로 안전하게 기록한다(§12 격리 원칙과 동일한 이유).

**GPT 분석 자신이 실패를 만드는 경우의 순환 방지**: `analyzeError()` 호출이 `AiGenerationException`을 던지면, 이 예외는 **다시 캡처 파이프라인으로 들어가면 안 된다.** `ErrorAnalysisService`의 GPT 호출부는 반드시 자체 try/catch로 감싸고, 실패 시 `ai_analysis_status = FAILED`만 기록한 뒤 종료한다 — 이 catch 블록은 §7의 `ErrorClassifier`를 절대 다시 타지 않는, 파이프라인 바깥의 "종착점"이어야 한다.

---

## 11. 비동기 처리 방식 비교

| 방식 | 이 프로젝트에 적합한가 |
|---|---|
| 완전 동기(에러 응답 안에서 GPT까지 호출) | **부적합.** GPT 응답 지연(최대 90초, `openai.read-timeout-ms`)이 그대로 사용자 요청 응답 시간에 더해진다. 안 그래도 500 에러가 난 요청을 더 오래 붙잡아두는 셈. |
| Redis Queue + 기존 Python Worker 재사용 | **부적합.** 기존 Worker는 ComfyUI(GPU 이미지 생성)처럼 "JVM 밖에서, 오래 걸리는" 작업을 위해 만들어진 구조다. GPT 오류 분석은 순수 HTTP 호출이라 JVM 안에서 처리 못 할 이유가 없고, 오히려 Python Worker에 새 Job 타입을 추가하면 상태 머신(`ThumbnailJobStatus`)을 억지로 재활용하거나 새로 만들어야 해서 복잡도만 늘어난다. |
| Spring `@Async` | **채택.** 이 프로젝트에 `@Async`가 쓰인 전례는 없지만(§6 문제점 8), 도입 비용이 가장 낮다 — `@EnableAsync` + 전용 `ThreadPoolTaskExecutor` 빈 하나, `ErrorAnalysisService` 메서드에 `@Async` 애노테이션 하나면 된다. Error 저장은 즉시 동기로 하고, AI 분석만 비동기로 분리한다. |
| Spring Batch / Scheduler로 주기적 처리 | **부적합.** "실시간에 가깝게 관리자에게 보여준다"는 목표와 맞지 않고, 이미 있는 통계 배치와 섞이면 책임이 불분명해진다. |

**결론**: `저장(동기, REQUIRES_NEW) → 응답 반환 → @Async로 AI 분석`. MVP 규모에서 가장 단순하면서 요구사항(사용자 응답 지연 없음, GPT 실패 격리)을 모두 만족한다.

---

## 12. Transaction / 장애 격리 문제

**Transaction 분리**: 이 프로젝트에는 이미 `FileService.java:117,160`에 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 선례가 있다 — 원본 트랜잭션과 무관하게 독립 커밋시켜야 하는 부수 효과를 다룰 때 쓰는 패턴으로 이미 검증되어 있다. Error 저장은 **바로 이 케이스의 정석적인 사례**다: 예외 때문에 원본 트랜잭션이 롤백되더라도 "이 예외가 발생했다"는 기록만은 살아남아야 한다. `ErrorCaptureService.capture(...)`를 `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 선언한다.

**Error 저장 자체가 실패하는 경우(DB 장애 등)의 무한 루프 방지**: `capture(...)` 호출부(신설할 catch-all 핸들러, 또는 배치 실패 catch 블록)는 **반드시** 다음과 같이 감싼다:

```java
try {
    errorCaptureService.capture(exception, context);
} catch (Exception captureFailure) {
    // 재귀 방지: 여기서 다시 capture()를 부르지 않는다. 로그만 남긴다.
    log.error("에러 모니터링 자체가 실패함", captureFailure);
}
```

이 규칙이 지켜지면 "DB 장애 → Error 저장 시도 → DB 장애로 저장 실패 → 그 실패를 또 저장 시도 → ..." 무한루프가 구조적으로 불가능해진다. **모니터링 파이프라인 안에서 발생한 실패는 절대 모니터링 파이프라인으로 다시 들어가지 않는다**는 것이 이 기능 전체를 관통하는 단 하나의 안전 규칙이다(§10의 GPT 순환 방지와 동일한 원칙의 재적용).

**본 서비스에 대한 영향 격리**: (a) 저장은 `REQUIRES_NEW`라 원본 트랜잭션/응답 흐름과 분리, (b) AI 분석은 `@Async`라 응답 스레드와 분리, (c) 두 실패 지점 모두 위 규칙으로 로그 종착 처리 — 세 겹으로 격리되므로 모니터링 기능 자체의 장애가 본 서비스 응답에 영향을 줄 경로가 없다.

---

## 13. Logging / Trace ID 전략

MDC/traceId가 전무한 상태(§1 문제점 6)에서, 아주 저비용으로 큰 가치를 주는 개선이 하나 있다: **요청당 UUID를 생성해 MDC에 넣는 서블릿 Filter**를 새로 추가하고, `ErrorEvent.traceId` 컬럼에 그 값을 함께 저장한다. 이렇게 하면 관리자가 Monitoring 페이지에서 특정 오류를 보고, 그 `traceId`로 (별도 로그 수집기가 없어도) `docker logs` stdout을 grep해서 그 요청의 앞뒤 로그 흐름을 재구성할 수 있다. 로그 인프라가 거의 없는 지금 상태에서는 이 traceId가 사실상 유일한 "로그 ↔ DB 레코드" 연결 고리가 된다.

이 Filter는 20줄 내외로 구현 비용이 낮고, Monitoring 기능이 없어도 그 자체로 유용하므로 **MVP에 포함하는 것을 권장**한다(완전히 별도 기능이 아니라, Monitoring의 필수 부속으로 취급).

전체 로그 수집기(ELK, Loki 등) 도입은 이번 범위에서 명확히 제외한다 — 현재 트래픽/조직 규모에서 과설계다.

---

## 14. Backend / Batch / Redis / Worker Monitoring 범위

| 구성요소 | MVP 포함 여부 | 근거 |
|---|---|---|
| Spring Backend 예외 | **포함** | `GlobalExceptionHandler` catch-all 하나로 전체 커버 가능, 가장 비용 대비 효과가 크다. |
| Spring Batch 실패 | **포함** | `StatisticBatchRunner`/`StatisticBatchScheduler`가 이미 예외를 catch하는 지점이 명확히 있고(§1), 거기에 `errorCaptureService.capture(...)` 호출 1줄만 추가하면 된다. 지금은 실패가 로그에만 남고 관리자 화면 어디에도 노출되지 않는다는 실질적 공백이 있다. |
| Redis 오류(백엔드 측) | **향후 확장** | `UserStatisticBatchService`는 이미 Redis 실패를 DB 폴백으로 정상 처리하고 있어 "장애"라기보다 "저하 모드"에 가깝다. 다만 폴백 자체가 실패하는 경우는 위 배치 경로로 자연히 포함된다. |
| GPT API 오류(`AiGenerationException`) | **포함(단, §9/§10 순환 방지 필수)** | 실제 인프라 실패 신호로서 가치가 있다. |
| Python Worker / ComfyUI 오류 | **제외(MVP), 향후 확장으로 명시** | JVM 스택 트레이스가 없는 이종 소스라 `ErrorEvent` 모델에 `source`(BACKEND/BATCH/WORKER) 구분 컬럼이 필요해지고, `stack_trace_excerpt`를 nullable로 바꿔야 한다. 이미 `ThumbnailJobService.failJob()` → `errorMessage` 저장 경로가 존재하므로(§2 리서치), 나중에 그 지점에 `capture(source=WORKER, ...)` 한 줄을 추가하는 방식으로 매우 저비용으로 통합 가능하다 — 지금 무리해서 넣을 이유가 없다. |

**Backend 우선, Batch는 곁들이는 정도, Worker는 다음 단계** — 이것이 이 프로젝트 규모에서 가장 안전한 범위 설정이다.

---

## 15. 보안 및 개인정보 위험

- 절대 저장 금지: `password`, `authCode`(이메일 인증코드), 비밀번호 재설정 토큰, `Authorization` 헤더 값, 쿠키(`accessToken`/`refreshToken`) 원문.
- 저장 시 마스킹 필요: 쿼리스트링에 토큰이 실리는 요청(`/api/resetPassword` 관련 플로우 등)은 `requestUri`를 저장하기 전에 쿼리스트링을 제거하거나 `***`로 치환한다.
- `stack_trace_excerpt`에 예외 메시지가 그대로 들어가는데, 드물게 예외 메시지 자체에 사용자 입력값(예: `NoSuchElementException("사용자를 찾을 수 없습니다: " + username)` 같은 패턴)이 섞여 들어갈 수 있다 — 치명적 개인정보는 아니지만, AI 분석 요청 시 OpenAI로 이 텍스트가 그대로 전송된다는 점은 인지하고 있어야 한다(기존 GPT 기능들도 이미 사용자 게시글 본문을 OpenAI로 보내고 있으므로, 이 프로젝트가 이미 감수하고 있는 수준의 리스크와 동일선상이다).
- 관리자 페이지 접근 제어는 기존 `/api/admin/**` → `hasAuthority("ADMIN")`(SecurityConfig)를 그대로 재사용하면 되고, 별도 권한 체계를 새로 만들 필요가 없다.

---

## 16. 추천 Architecture

```
com.study.blog.monitoring                (신규 최상위 패키지 — batch, ai와 동급)
  domain/
    ErrorIssue.java          (JPA 엔티티)
    ErrorEvent.java          (JPA 엔티티)
    ErrorSeverity.java, AiAnalysisStatus.java, IssueStatus.java (enum)
  classifier/
    ErrorClassifier.java     (§7 정책 — 유일하게 "이 예외를 기록할지" 결정하는 곳)
  fingerprint/
    ErrorFingerprintGenerator.java   (§9)
  repository/
    ErrorIssueRepository.java (+ QueryDSL Custom, 관리자 목록 조회용 — 기존 PostRepositoryCustom 패턴 재사용)
    ErrorEventRepository.java
  service/
    ErrorCaptureService.java  (REQUIRES_NEW, 동기 저장 — §12)
    ErrorAnalysisService.java (@Async, GPT 호출 — §10, §11)
  dto/
    ErrorAnalysisResult.java  (GPT JSON 응답 파싱용)

com.study.blog.admin.controller.AdminMonitoringController   (기존 admin 컨벤션 그대로)
com.study.blog.admin.service.AdminMonitoringService          (얇은 위임 — 기존 Admin*Service 패턴)
com.study.blog.admin.dto.{AdminErrorIssueResponseDto, AdminErrorEventResponseDto}

client/OpenAiClient.java  (기존 파일에 analyzeError() 메서드만 추가)
GlobalExceptionHandler.java (catch-all 1개 추가 + @Slf4j 추가)
config/AsyncConfig.java  (신규 — @EnableAsync + TaskExecutor 빈)
config/TraceIdFilter.java 또는 filter 패키지 (신규 — §13)
db/migration/V13__create_error_monitoring_tables.sql (신규)
resources/prompts/error-analysis-system-prompt.txt (신규)
```

데이터 흐름:
```
Exception 발생
   ↓ (GlobalExceptionHandler 신규 catch-all, 또는 Batch catch 블록)
ErrorClassifier.classify(exception) → IGNORE | RECORD_AND_AI_ANALYZE
   ↓ (RECORD_AND_AI_ANALYZE인 경우만)
ErrorCaptureService.capture(exception, context)   [REQUIRES_NEW, 실패 시 log.error만]
   ↓ fingerprint 계산 → 기존 Issue면 count++, 신규면 Issue 생성
   ↓ (신규 Issue이고 저장 성공한 경우만)
ErrorAnalysisService.analyze(issue)   [@Async, 실패 시 status=FAILED만 기록]
   ↓
OpenAiClient.analyzeError(userPrompt)
   ↓
ErrorIssue.aiSummary/aiCause/... 갱신
   ↓
/admin/monitoring 에서 조회
```

---

## 17. MVP 구현 범위

1. `V13` 마이그레이션: `error_issue`, `error_event` 테이블 (§8 필드).
2. `ErrorIssue`/`ErrorEvent` JPA 엔티티 + 기본 Repository.
3. `ErrorClassifier` — §7의 화이트리스트/기본값 정책.
4. `ErrorFingerprintGenerator` — §9 규칙.
5. `ErrorCaptureService` — `REQUIRES_NEW` 저장, 실패 시 로그만(§12 안전 규칙).
6. `TraceIdFilter` — 요청당 UUID, MDC + `ErrorEvent.traceId`(§13).
7. `GlobalExceptionHandler`에 `@Slf4j` + catch-all `@ExceptionHandler(Exception.class)` 추가, 그 안에서 분류 후 캡처 호출.
8. `AsyncConfig`(`@EnableAsync`) + `ErrorAnalysisService`(`@Async`) + `OpenAiClient.analyzeError()` + `prompts/error-analysis-system-prompt.txt`.
9. `StatisticBatchRunner`/`StatisticBatchScheduler`의 기존 catch 블록에 `errorCaptureService.capture(...)` 1줄 연동.
10. `AdminMonitoringController`(`GET /api/admin/monitoring/issues`, `GET /api/admin/monitoring/issues/{id}`) + `AdminMonitoringService` + DTO.
11. 프론트: `page/admin/AdminMonitoring.jsx`(+css) — 기존 `AdminPosts.jsx` 패턴 재사용(검색/테이블/페이지네이션), 상세 화면(모달 또는 별도 라우트)에서 스택트레이스/AI 분석 결과 표시. `AdminLayout.jsx` 네비게이션에 "Monitoring" 항목 추가, `App.jsx`에 `/admin/monitoring` 라우트 추가.
12. `DuplicateLikeException`/`InvalidTokenException`을 `GlobalExceptionHandler`에 마저 매핑(§6 문제점 2) — catch-all을 추가하는 김에 함께 정리하는 것을 권장하되, 사용자 승인 시에만 진행.

---

## 18. 향후 확장 범위

- Python Worker/ComfyUI 오류 통합(§14) — `source` 컬럼 추가 + `ThumbnailJobService.failJob()` 연동.
- Redis 백엔드 측 오류의 명시적 캡처(현재는 폴백으로 흡수됨).
- 오래된 `ErrorEvent` 자동 삭제 배치(보존 기간 정책).
- 동일 Issue 재발 시 재분석 트리거(잠잠기간 기반).
- `ErrorIssue.status`(OPEN/RESOLVED) 기반 관리자 확인 워크플로우 + 알림(Slack 등, 이번 범위에는 없음).
- 401/403 급증 같은 보안 이상징후 탐지(§1에서 언급한, `SecurityConfig`의 인라인 핸들러 쪽 — 이번 파이프라인과는 별도 설계가 필요).

---

## 19. 예상 수정 / 신규 파일

**신규**
```
src/main/java/com/study/blog/monitoring/domain/{ErrorIssue, ErrorEvent, ErrorSeverity, AiAnalysisStatus, IssueStatus}.java
src/main/java/com/study/blog/monitoring/classifier/ErrorClassifier.java
src/main/java/com/study/blog/monitoring/fingerprint/ErrorFingerprintGenerator.java
src/main/java/com/study/blog/monitoring/repository/{ErrorIssueRepository, ErrorIssueRepositoryCustom, ErrorIssueRepositoryCustomImpl, ErrorEventRepository}.java
src/main/java/com/study/blog/monitoring/service/{ErrorCaptureService, ErrorAnalysisService}.java
src/main/java/com/study/blog/monitoring/dto/ErrorAnalysisResult.java
src/main/java/com/study/blog/admin/controller/AdminMonitoringController.java
src/main/java/com/study/blog/admin/service/AdminMonitoringService.java
src/main/java/com/study/blog/admin/dto/{AdminErrorIssueResponseDto, AdminErrorEventResponseDto}.java
src/main/java/com/study/blog/config/AsyncConfig.java
src/main/java/com/study/blog/filter/TraceIdFilter.java (패키지명은 구현 시 확정)
src/main/resources/db/migration/V13__create_error_monitoring_tables.sql
src/main/resources/prompts/error-analysis-system-prompt.txt
src/main/webFront/src/page/admin/{AdminMonitoring.jsx, AdminMonitoring.css}
```

**수정**
```
src/main/java/com/study/blog/GlobalExceptionHandler.java        (@Slf4j + catch-all 추가, 기존 매핑은 유지)
src/main/java/com/study/blog/client/OpenAiClient.java            (analyzeError() 메서드만 추가)
src/main/java/com/study/blog/batch/service/StatisticBatchRunner.java        (catch 블록에 capture 호출 1줄)
src/main/java/com/study/blog/batch/scheduler/StatisticBatchScheduler.java   (동일)
src/main/webFront/src/components/admin/AdminLayout.jsx           (네비게이션 항목 추가)
src/main/webFront/src/App.jsx                                    (라우트 추가)
```

---

## 20. 구현 순서

1. `V13` 마이그레이션 + JPA 엔티티(가장 기반이 되는 레이어, 다른 모든 것이 이를 전제로 함).
2. `ErrorClassifier` + `ErrorFingerprintGenerator`(순수 로직, 단위 테스트로 검증 가능 — DB/외부 연동 없이 먼저 신뢰도 확보).
3. `ErrorCaptureService`(REQUIRES_NEW 저장) — 이 시점까지는 GPT/비동기 없이도 "저장은 된다"를 curl로 검증 가능.
4. `GlobalExceptionHandler` catch-all 연동 — 실제 요청 흐름에서 캡처가 동작하는지 확인(일부러 500을 유발하는 테스트 엔드포인트나 임시 예외로 검증).
5. `TraceIdFilter` 추가.
6. `AsyncConfig` + `OpenAiClient.analyzeError()` + `ErrorAnalysisService` — GPT 연동은 가장 마지막(외부 의존성이 가장 크고, 실패해도 앞 단계가 이미 정상 동작해야 하므로).
7. `StatisticBatchRunner`/`Scheduler` 연동(배치 실패 캡처).
8. `AdminMonitoringController`/`Service`/DTO.
9. 프론트엔드 `AdminMonitoring` 페이지 + 네비게이션/라우트.
10. 전체 플로우 통합 테스트: 의도적 예외 → 저장 → (중복 발생 시 count만 증가하는지) → AI 분석 → 관리자 페이지 조회, 그리고 §12 안전 규칙(DB 장애 시뮬레이션, GPT 실패 시뮬레이션이 본 서비스에 영향 없는지) 검증.

이 순서는 "DB에 안전하게 쌓이는 것"을 먼저 확정한 뒤 "그 위에 AI를 얹는" 구조라서, 어느 단계에서 멈춰도 이미 완료된 앞 단계는 그 자체로 가치가 있다(예: GPT 연동 전이라도 4번까지만 끝나면 "지금까지 안 보이던 500 에러가 전부 DB에 쌓이기 시작한다"는 즉각적 개선이 이미 발생한다).

# Secret Management Module

## 개요

Secret Management Module은 중앙화된 시크릿 관리 시스템에서 민감한 설정 정보(데이터베이스 접속 정보, API 키 등)를 안전하게 가져오고 관리하기 위한 모듈입니다.

## 주요 기능

- 시크릿 서버 API를 통한 설정 정보 조회
- 다양한 타입(DB, Redis 등)의 시크릿 정보 지원
- 타입 안전한 시크릿 정보 매핑
- 자동화된 예외 처리 및 검증

## 사용 방법

### 1. 의존성 추가

#### Maven

```xml

<dependency>
    <groupId>co.kr.kwt.starter</groupId>
    <artifactId>module-kms</artifactId>
    <version>${module-kms.version}</version>
</dependency>
```

#### Gradle

```groovy
// Gradle Groovy DSL
implementation 'co.kr.kwt.starter:module-kms:${moduleKmsVersion}'

// Gradle Kotlin DSL
implementation("co.kr.kwt.starter:module-kms:${moduleKmsVersion}")
```

### 2. Secret 구현체 생성

Secret 인터페이스를 구현하여 필요한 시크릿 타입을 정의합니다.

```java
// Secret 인터페이스 구현
public interface Secret {
    String getSecretKey();
}

// Secret 구현체 예시 - BaseSecret
@Getter
@Setter
public class BaseSecret implements Secret {
    private String id;
    private String secretType;
    private String secretKey;
    private String description;
    private String lastModified;
    private Integer version;
}

// DB 설정을 위한 Secret 구현체
@Getter
@Setter
public class DatabaseSecret extends BaseSecret {
    private SecretValue secretValue;

    @Getter
    @Setter
    public static class SecretValue {
        private String driver;
        private String host;
        private String port;
        private String database;
        private String username;
        private String password;
    }
}

// Redis 설정을 위한 Secret 구현체
@Getter
@Setter
public class RedisSecret extends BaseSecret {
    private SecretValue secretValue;

    @Getter
    @Setter
    public static class SecretValue {
        private String host;
        private String port;
        private String password;
    }
}
```

### 3. 시크릿 서비스 사용

```java

@Service
public class ApplicationService {
    private final SecretService secretService;

    public ApplicationService(SecretService secretService) {
        this.secretService = secretService;
    }

    public void initializeDatabase() {
        try {
            // DB 시크릿 정보 조회
            DatabaseSecret dbSecret = secretService.getSecrets(DatabaseSecret.class);
            DatabaseSecret.SecretValue secretValue = dbSecret.getSecretValue();

            // DB 연결 설정
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(secretValue.getDriver());
            dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s",
                    secretValue.getHost(),
                    secretValue.getPort(),
                    secretValue.getDatabase()));
            dataSource.setUsername(secretValue.getUsername());
            dataSource.setPassword(secretValue.getPassword());

        }
        catch (SecretNotFoundException e) {
            log.error("Database secret not found", e);
        }
        catch (SecretParsingException e) {
            log.error("Failed to parse database secret", e);
        }
    }

    public void initializeRedis() {
        try {
            // Redis 시크릿 정보 조회
            RedisSecret redisSecret = secretService.getSecrets(RedisSecret.class);
            RedisSecret.SecretValue secretValue = redisSecret.getSecretValue();

            // Redis 연결 설정
            RedisStandaloneConfiguration redisConfig =
                    new RedisStandaloneConfiguration(secretValue.getHost(),
                            Integer.parseInt(secretValue.getPort()));
            redisConfig.setPassword(secretValue.getPassword());

        }
        catch (SecretNotFoundException | SecretParsingException e) {
            log.error("Failed to initialize Redis", e);
        }
    }
}
```

## 4. 예외 처리

모듈은 다음과 같은 예외를 발생시킬 수 있습니다:

1. **SecretNotFoundException**
    - 시크릿을 찾을 수 없는 경우
    - API 응답이 비어있는 경우
    - secrets 배열이 비어있는 경우

2. **SecretParsingException**
    - JSON 파싱 실패 시
    - 시크릿 데이터 형식이 잘못된 경우
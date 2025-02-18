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

### 2. 속성 설정

application.yml 또는 application.properties 파일에 KMS 관련 설정을 추가합니다.

#### application.yml
```yaml
# yaml
kms:
   enabled: true  # KMS 모듈 활성화 (기본값: true)
   url: "https://your-kms-server.com/api"  # KMS 서버 URL
   token: "your-access-token"  # KMS 서버 접근 토큰
   secret-key: "your-secret-key"  # KMS 암호화 키
   
```

```properties
# properties
kms.enabled=true
kms.url=https://your-kms-server.com/api
kms.token=your-access-token
kms.secret-key=your-secret-key
```

- kms.enabled: KMS 모듈의 활성화 여부를 설정합니다. 
  - 기본값은 true입니다.
  - 따라서, 따로 작성해주지 않아도 됩니다.
- kms.token: KMS 서버 접근을 위한 인증 토큰입니다.
- kms.url: KMS 서버의 API 엔드포인트 URL입니다.
- kms.secret-key: KMS 암호화에 사용되는 비밀키입니다.

### 3. Secret 구현체 생성

Secret 인터페이스를 구현하여 필요한 시크릿 타입을 정의합니다.

```java
// Secret 인터페이스 구현
public interface Secret {
    String getSecretKey();
}

// Secret 구현체 예시 - BaseSecret 
// 자신이 등록한 Secret 구조에 맞게 구성하면 됩니다. 
// 아래는 예시입니다...
@Getter
@Setter
public class MySecret extends Secret {
   private String driver;
   private String host;
   private String port;
   private String database;
   private String username;
   private String password;
}
```

### 4. 시크릿 서비스 사용

```java

@Service
public class ApplicationService {
    private final KmsService kmsService;

    public ApplicationService(KmsService kmsService) {
        this.kmsService = kmsService;
    }

    public void initializeDatabase() {
        try {
            // DB 시크릿 정보 조회
           MySecret mySecret = kmsService.getSecrets(MySecret.class);

            // DB 연결 설정
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(mySecret.getDriver());
            dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s",
                    mySecret.getHost(),
                    mySecret.getPort(),
                    mySecret.getDatabase()));
            dataSource.setUsername(mySecret.getUsername());
            dataSource.setPassword(mySecret.getPassword());

        }
        catch (SecretNotFoundException e) {
            log.error("Database secret not found", e);
        }
        catch (SecretParsingException e) {
            log.error("Failed to parse database secret", e);
        }
    }
}
```
- 필요한 곳에서 KmsService를 주입받고, getSecrets() 메소드를 호출합니다.
- 메소드를 호출할 때는 앞서 생성한 자신만의 Secret 타입 구현체를 인자값으로 전달합니다.
- 메소드 호출 결과로 Secret 타입 구현체에 Kms 정보가 바인딩되어 전달됩니다.

## 5. 예외 처리

모듈은 다음과 같은 예외를 발생시킬 수 있습니다:

1. **SecretNotFoundException**
    - 시크릿을 찾을 수 없는 경우
    - API 응답이 비어있는 경우
    - secrets 배열이 비어있는 경우

2. **SecretParsingException**
    - JSON 파싱 실패 시
    - 시크릿 데이터 형식이 잘못된 경우
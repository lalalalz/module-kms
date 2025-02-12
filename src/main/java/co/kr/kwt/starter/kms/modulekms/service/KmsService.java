package co.kr.kwt.starter.kms.modulekms.service;

import co.kr.kwt.starter.kms.modulekms.config.KmsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KmsService {

    private static final String KMS_HEADER = "x-api-key";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final KmsProperties kmsProperties;

    /**
     * API로부터 시크릿 정보를 조회하여 지정된 타입으로 변환
     *
     * @param tClass 변환할 타입 클래스
     * @param <T>    Secret를 상속받는 제네릭 타입
     * @return 변환된 시크릿 객체
     * @throws SecretNotFoundException 시크릿을 찾을 수 없는 경우
     * @throws SecretParsingException  JSON 파싱 실패시
     */
    public <T extends Secret> T getSecrets(Class<T> tClass) {
        try {
            String responseBody = fetchFromApi();
            return parseSecret(responseBody, tClass);
        }
        catch (JsonProcessingException e) {
            throw new SecretParsingException("Failed to parse secret response", e);
        }
    }

    /**
     * API 호출하여 응답 받기
     */
    private String fetchFromApi() {
        ResponseEntity<String> response = restTemplate.exchange(getRequestEntity(), String.class);
        validateResponse(response);
        return response.getBody();
    }

    /**
     * API 응답을 파싱하여 시크릿 객체로 변환
     */
    private <T extends Secret> T parseSecret(String responseBody, Class<T> tClass) throws JsonProcessingException {
        JsonNode rootNode = getRootNode(responseBody);
        JsonNode secretNode = getFirstSecret(rootNode);
        validateSecret(secretNode);

        return objectMapper.treeToValue(secretNode, tClass);
    }

    /**
     * 응답 바디에서 루트 노드 추출
     */
    private JsonNode getRootNode(String responseBody) throws JsonProcessingException {
        JsonNode rootArray = objectMapper.readTree(responseBody);
        if (rootArray.isEmpty()) {
            throw new SecretNotFoundException("Empty response from API");
        }
        return rootArray.get(0);
    }

    /**
     * 루트 노드에서 첫 번째 시크릿 추출
     */
    private JsonNode getFirstSecret(JsonNode rootNode) {
        JsonNode secretsArray = rootNode.get("secrets");
        if (secretsArray == null || secretsArray.isEmpty()) {
            throw new SecretNotFoundException("No secrets found in response");
        }
        return secretsArray.get(0);
    }

    /**
     * API 응답 검증
     */
    private void validateResponse(ResponseEntity<String> response) {
        if (response == null || response.getBody() == null) {
            throw new SecretNotFoundException("Empty response from API");
        }
    }

    /**
     * 시크릿 노드 검증
     */
    private void validateSecret(JsonNode secretNode) {
        if (secretNode == null || secretNode.isEmpty()) {
            throw new SecretNotFoundException("Secret not found or empty");
        }
    }

    private RequestEntity<Void> getRequestEntity() {
        return RequestEntity
                .get(kmsProperties.getUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(KMS_HEADER, kmsProperties.getToken())
                .build();
    }
}

package co.kr.kwt.starter.kms.modulekms.config;

import co.kr.kwt.starter.kms.modulekms.service.KmsRepository;
import co.kr.kwt.starter.kms.modulekms.service.KmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kms", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KmsProperties.class)
public class KmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public KmsRepository kmsRepository() {
        return new KmsRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public KmsService kmsService(
            RestTemplate restTemplate,
            KmsProperties kmsProperties,
            ObjectMapper objectMapper,
            KmsRepository kmsRepository
    ) {
        return new KmsService(objectMapper, restTemplate, kmsProperties, kmsRepository);
    }
}

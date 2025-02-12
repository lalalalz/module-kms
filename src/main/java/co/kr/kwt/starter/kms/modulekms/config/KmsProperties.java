package co.kr.kwt.starter.kms.modulekms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "kms")
public class KmsProperties {

    private String url;
    private String token;
    private String secretKey;

}

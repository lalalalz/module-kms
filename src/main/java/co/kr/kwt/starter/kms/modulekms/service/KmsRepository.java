package co.kr.kwt.starter.kms.modulekms.service;

import org.springframework.stereotype.Repository;

@Repository
public class KmsRepository {

    private boolean isCached;
    private Secret cache;

    @SuppressWarnings("unchecked")
    public <T extends Secret> T save(T secret) {
        isCached = true;
        cache = secret;
        return (T) cache;
    }

    public boolean isCached() {
        return isCached;
    }

    @SuppressWarnings("unchecked")
    public <T extends Secret> T getSecret() {
        if (!isCached) {
            throw new IllegalStateException("Secret not stored");
        }

        return (T) cache;
    }
}

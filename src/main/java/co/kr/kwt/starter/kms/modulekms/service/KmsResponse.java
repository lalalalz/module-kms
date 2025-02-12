package co.kr.kwt.starter.kms.modulekms.service;

import lombok.Value;

import java.util.List;

@Value
public class KmsResponse<T extends Secret> {

    String id;
    String serviceId;
    String environment;
    List<T> secrets;
}

package com.xiaxinyu.reactivex.entity;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Record{
    private Long id;
    private String status;
    private String receiveAccount;
    private String failedReason;
    private String businessType;
    private Integer retryCount;
    private String messageType;
    private String variables;
    private Long templateId;
    private Integer maxRetryCount;
    private Boolean isManualRetry;
}

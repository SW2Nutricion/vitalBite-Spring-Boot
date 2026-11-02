package com.vitalbite.documental.application.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePdfRequestDTO {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String clinicName;
    private String planName;
    @NotNull
    @Positive
    private Double amount;
    @NotBlank
    private String billingPeriod;
    private String transactionHash;
    @NotBlank
    private String date;
}

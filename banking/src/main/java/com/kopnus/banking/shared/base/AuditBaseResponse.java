package com.kopnus.banking.shared.base;

@Data
public class AuditBaseResponse {

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

}

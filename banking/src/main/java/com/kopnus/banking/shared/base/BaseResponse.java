package com.kopnus.banking.shared.base;

@Data
public class BaseResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;
    private String message;
    private List<String> errors;
    private transient T data;
    private Date serverTime;
}

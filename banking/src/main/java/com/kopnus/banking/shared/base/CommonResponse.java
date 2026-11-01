package com.kopnus.banking.shared.base;

import java.util.Date;
import java.util.List;

public class CommonResponse {
    public static <T> BaseResponse<T> constructResponse(String code, String message, List<String> errors, T data) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.setCode(code);
        baseResponse.setErrors(errors);
        baseResponse.setMessage(message);
        baseResponse.setData(data);
        baseResponse.setServerTime(new Date());
        return baseResponse;

    }
}

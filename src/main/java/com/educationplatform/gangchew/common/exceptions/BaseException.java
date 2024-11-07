package com.educationplatform.gangchew.common.exceptions;


import com.educationplatform.gangchew.common.response.BaseResponseStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}

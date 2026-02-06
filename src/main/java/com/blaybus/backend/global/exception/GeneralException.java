package com.blaybus.backend.global.exception;

import com.blaybus.backend.global.response.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private final ErrorStatus errorStatus;
}
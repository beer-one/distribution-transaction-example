package com.trx.errors.exception

import com.trx.errors.CustomException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class IncorrectFormatBodyException: CustomException() {
    override val message: String = "요청 바디 입력 오류"
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class IncorrectParameterException(parameter: String): CustomException() {
    override val message: String = "파라미터(${parameter}) 입력 오류."
}
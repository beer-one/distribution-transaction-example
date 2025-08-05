package com.trx.errors.exception

import com.trx.errors.CustomException

class IncorrectFormatBodyException: CustomException() {
    override val message: String = "요청 바디 입력 오류"
}


class IncorrectParameterException(parameter: String): CustomException() {
    override val message: String = "파라미터(${parameter}) 입력 오류."
}
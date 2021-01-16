package com.trx.errors.exception

import com.trx.errors.CustomException

class IncorrectFormatBodyException: CustomException() {
    override val message: String = "요청 바디 입력 오류"
}
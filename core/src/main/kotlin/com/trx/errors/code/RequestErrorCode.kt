package com.trx.errors.code

import com.trx.errors.ErrorCode
import com.trx.utils.MessageConverter

enum class RequestErrorCode(override val code: String) : ErrorCode {
    INCORRECT_FORMAT_BODY("${PREFIX}incorrect-format-body"),
    ;

    override val message: String = MessageConverter.getMessage(code)
}

private const val PREFIX = "request."
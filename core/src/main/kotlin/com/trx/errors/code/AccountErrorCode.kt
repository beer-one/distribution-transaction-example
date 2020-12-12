package com.trx.errors.code

import com.trx.errors.ErrorCode
import com.trx.utils.MessageConverter

enum class AccountErrorCode(override val code: String) : ErrorCode {
    NOT_FOUND("${PREFIX}not-found"),
    INSUFFICIENT_BALANCE("${PREFIX}insufficient-balance"),
    ;

    override val message: String = MessageConverter.getMessage(code)
}

private const val PREFIX = "account."
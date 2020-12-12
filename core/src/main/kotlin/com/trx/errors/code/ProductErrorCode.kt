package com.trx.errors.code

import com.trx.errors.ErrorCode
import com.trx.utils.MessageConverter

enum class ProductErrorCode(override val code: String) : ErrorCode {
    NOT_FOUND("${PREFIX}not-found"),
    OUT_OF_STOCK("${PREFIX}out-of-stock"),
    ;

    override val message: String = MessageConverter.getMessage(code)
}

private const val PREFIX = "product."
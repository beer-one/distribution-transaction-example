package com.trx.errors.code

import com.trx.errors.ErrorCode
import com.trx.utils.MessageConverter

enum class TransactionErrorCode(override val code: String) : ErrorCode {
    TRANSACTION_ERROR("${PREFIX}error"),
    ;

    override val message: String = MessageConverter.getMessage(code)
}

private const val PREFIX = "transaction."
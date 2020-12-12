package com.trx.errors.exception

import com.trx.errors.CustomException
import com.trx.errors.code.TransactionErrorCode

class TransactionException(val reason: String = "트랜잭션 에러"): CustomException(TransactionErrorCode.TRANSACTION_ERROR)
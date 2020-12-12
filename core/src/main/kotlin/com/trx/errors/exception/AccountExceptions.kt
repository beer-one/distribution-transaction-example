package com.trx.errors.exception

import com.trx.errors.CustomException
import com.trx.errors.code.AccountErrorCode
import com.trx.errors.code.ProductErrorCode

class AccountNotFoundException: CustomException(AccountErrorCode.NOT_FOUND)
class InsufficientBalanceException: CustomException(AccountErrorCode.INSUFFICIENT_BALANCE)
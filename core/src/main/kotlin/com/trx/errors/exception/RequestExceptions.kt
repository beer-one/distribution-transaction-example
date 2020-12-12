package com.trx.errors.exception

import com.trx.errors.CustomException
import com.trx.errors.code.RequestErrorCode

class IncorrectFormatBodyException: CustomException(RequestErrorCode.INCORRECT_FORMAT_BODY)
package com.trx.errors

import java.lang.RuntimeException

abstract class CustomException(val errorCode: ErrorCode) : RuntimeException()
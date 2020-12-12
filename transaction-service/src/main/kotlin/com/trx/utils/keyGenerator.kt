package com.trx.utils

import java.util.*

object KeyGenerator {

    fun generateKey(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
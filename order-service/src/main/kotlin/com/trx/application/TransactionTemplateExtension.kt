package com.trx.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import kotlin.coroutines.CoroutineContext

suspend fun <T> TransactionTemplate.executeWithContext(
    context: CoroutineContext = Dispatchers.Main,
    readOnly: Boolean = false,
    block: CoroutineScope.() -> T
): T? = withContext(context) {
    this@executeWithContext.execute {
        if (readOnly) TransactionSynchronizationManager.setCurrentTransactionReadOnly(true)
        block()
    }
}
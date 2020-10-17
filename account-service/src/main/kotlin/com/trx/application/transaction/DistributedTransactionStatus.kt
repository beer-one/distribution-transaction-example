package com.trx.application.transaction

enum class DistributedTransactionStatus {
    NEW, CONFIRMED, ROLLBACK, TO_ROLLBACK
}
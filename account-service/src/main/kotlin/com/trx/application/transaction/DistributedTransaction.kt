package com.trx.application.transaction

data class DistributedTransaction(
    var id: String? = null,
    val status: DistributedTransactionStatus,
    val participants: MutableList<DistributedTransactionParticipant> = mutableListOf()
)

package com.trx.application.transaction

data class DistributedTransaction(
    var id: String? = null,
    val status: DistributedTransactionStatus = DistributedTransactionStatus.NEW,
    val participants: MutableList<DistributedTransactionParticipant> = mutableListOf()
)

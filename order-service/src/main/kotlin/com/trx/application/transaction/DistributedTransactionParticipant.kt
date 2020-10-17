package com.trx.application.transaction

class DistributedTransactionParticipant(
    val serviceId: String,
    var status: DistributedTransactionStatus
)
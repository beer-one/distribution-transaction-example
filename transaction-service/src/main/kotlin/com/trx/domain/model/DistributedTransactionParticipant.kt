package com.trx.domain.model

import javax.persistence.*

/**
 * 마이크로서비스와 통신하면서 사용하는 객체
 */
@Entity
@Table(name = "DISTRIBTED_TRANSACTION_PARTICIPANT")
data class DistributedTransactionParticipant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val no: Int = 0,

    @Column(name = "transaction_no")
    val transactionNo: Int,

    @Column(name = "service_id")
    val serviceId: String,

    var status: DistributedTransactionStatus
)
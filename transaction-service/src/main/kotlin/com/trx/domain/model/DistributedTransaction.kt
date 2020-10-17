package com.trx.domain.model

import javax.persistence.*

/**
 * 마이크로서비스와 통신하면서 사용하는 객체
 */
@Entity
@Table
data class DistributedTransaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val no: Int = 0,

    val id: String,

    var status: DistributedTransactionStatus,

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_no")
    val participants: MutableList<DistributedTransactionParticipant> = mutableListOf()
)
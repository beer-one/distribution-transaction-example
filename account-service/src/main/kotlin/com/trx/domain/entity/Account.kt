package com.trx.domain.entity

import javax.persistence.*

@Table
@Entity
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val customerId: Int,

    var balance: Int
)
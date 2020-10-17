package com.trx.domain.entity

import javax.persistence.*

@Entity
@Table
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val productId: Int,

    val count: Int,

    val customerId: Int
)
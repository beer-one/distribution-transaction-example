package com.trx.domain.entity

import javax.persistence.*

@Entity
@Table
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val name: String = "",

    var count: Int = 0,

    val price: Int = 0
)
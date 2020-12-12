package com.trx.domain.entity

import com.trx.errors.exception.InsufficientBalanceException
import javax.persistence.*

@Table
@Entity
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val customerId: Int,

    var balance: Int
) {
    fun applyPayment(price: Int) {
        if (balance < price) throw InsufficientBalanceException()

        balance -= price
    }
}
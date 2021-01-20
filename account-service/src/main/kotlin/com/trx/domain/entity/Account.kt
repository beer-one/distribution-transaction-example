package com.trx.domain.entity

import com.trx.errors.exception.InsufficientBalanceException
import javax.persistence.*

@Entity
@Table(name = "account")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "customer_id")
    val customerId: Int,

    var balance: Int
) {
    fun applyPayment(price: Int): Int {
        if (balance < price) throw InsufficientBalanceException(id, balance, price)

        balance -= price

        return balance
    }

    fun deposit(money: Int) {
        balance += money
    }
}
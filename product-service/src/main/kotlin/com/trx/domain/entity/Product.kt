package com.trx.domain.entity

import com.trx.errors.exception.ProductOutOfStockException
import javax.persistence.*

@Entity
@Table(name = "product")
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val name: String = "",

    var count: Int = 0,

    val price: Int = 0
) {
    fun subtract(subtractCount: Int): Int {
        if (count < subtractCount) throw ProductOutOfStockException()

        return (price * subtractCount).also {
            count -= subtractCount
        }
    }
}
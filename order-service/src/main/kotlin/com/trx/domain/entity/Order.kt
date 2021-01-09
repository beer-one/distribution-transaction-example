package com.trx.domain.entity

import com.trx.domain.enums.OrderStatus
import javax.persistence.*

@Entity
@Table(name = "o_order")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "product_id")
    val productId: Int,

    val count: Int,

    @Column(name = "customer_id")
    val customerId: Int
) {

    @Column(name = "order_status")
    var orderStatus: OrderStatus = OrderStatus.PENDING
}
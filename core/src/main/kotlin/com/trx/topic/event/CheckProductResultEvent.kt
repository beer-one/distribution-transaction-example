package com.trx.topic.event

data class CheckProductResultEvent(
    val success: Boolean,
    val failureReason: String,
    val totalPrice: Int
) {
    companion object {
        fun success(price: Int) = CheckProductResultEvent(
            success = true,
            failureReason = "",
            totalPrice = price
        )

        fun fail(reason: String) = CheckProductResultEvent(
            success = false,
            failureReason = reason,
            totalPrice = 0
        )
    }
}
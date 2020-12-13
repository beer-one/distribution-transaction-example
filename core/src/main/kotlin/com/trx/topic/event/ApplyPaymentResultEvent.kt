package com.trx.topic.event

data class ApplyPaymentResultEvent(
    val success: Boolean,
    val failureReason: String,
    val restBalance: Int
) {
    companion object {
        fun success(restBalance: Int) = ApplyPaymentResultEvent(
            success = true,
            failureReason = "",
            restBalance = restBalance
        )

        fun fail(reason: String) = ApplyPaymentResultEvent(
            success = false,
            failureReason = reason,
            restBalance = -1
        )
    }
}
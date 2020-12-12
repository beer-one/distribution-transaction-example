package com.trx.topic.event

data class ApplyPaymentResultEvent(
    val success: Boolean,
    val failureReason: String
) {
    companion object {
        fun success() = ApplyPaymentResultEvent(
            success = true,
            failureReason = ""
        )

        fun fail(reason: String) = ApplyPaymentResultEvent(
            success = false,
            failureReason = reason
        )
    }
}
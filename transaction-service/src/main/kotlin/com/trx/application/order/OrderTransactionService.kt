package com.trx.application.order

import com.trx.application.event.TransactionEventListener
import com.trx.application.event.TransactionEventPublisher
import com.trx.errors.exception.TransactionException
import com.trx.topic.Topic.APPLY_PAYMENT
import com.trx.topic.Topic.APPLY_PAYMENT_RESULT
import com.trx.topic.Topic.CHECK_PRODUCT
import com.trx.topic.Topic.CHECK_PRODUCT_RESULT
import com.trx.topic.event.ApplyPaymentEvent
import com.trx.topic.event.ApplyPaymentResultEvent
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.CheckProductResultEvent
import com.trx.presentation.request.OrderTransactionRequest
import com.trx.utils.KeyGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class OrderTransactionService (
    private val transactionEventListener: TransactionEventListener,
    private val transactionEventPublisher: TransactionEventPublisher
) {
    /**
     * 1. 상품 수량 체크, 수량 까기
     * 2. 결제 진행하기
     *
     * 어느 하나라도 망가지면 롤백
     * TODO: 실패 시 롤백 로직 생각해보기.
     */
    suspend fun orchestrateOrderTransaction(request: OrderTransactionRequest): Int {
        val key = KeyGenerator.generateKey()

        // 1-1. 상품 수량 체크
        val productCheckResult = requestCheckingProduct(
            event = CheckProductEvent(
                productId = request.productId,
                count = request.count
            ),
            key = key
        )

        return requestApplyingPayment(
            event = ApplyPaymentEvent(
                customerId = request.customerId,
                price = productCheckResult.totalPrice
            ),
            key = key
        )
    }

    private suspend fun requestCheckingProduct(event: CheckProductEvent, key: String): CheckProductResultEvent {
        transactionEventPublisher.publishEvent(
            topic = CHECK_PRODUCT,
            key = key,
            event = event
        ).awaitFirstOrNull()
            ?: throw TransactionException()

        // 1-2 상품 수량 체크 이벤트 확인, 성공 => 가격 확인, 실패 => 롤백 이벤트
        // 대기는 어떻게?
        return transactionEventListener.listenEvent(
            topic = CHECK_PRODUCT_RESULT,
            key = key,
            kClass = CheckProductResultEvent::class
        ).map { record ->
            record.value().also {
                if (!it.success) throw TransactionException(it.failureReason)
            }
        }
            .awaitFirstOrNull()
            ?: throw TransactionException()
    }
    private suspend fun requestApplyingPayment(event: ApplyPaymentEvent, key: String): Int {
        transactionEventPublisher.publishEvent(
            topic = APPLY_PAYMENT,
            key = key,
            event = event
        ).awaitFirstOrNull()
            ?: throw TransactionException()

        return transactionEventListener.listenEvent(
            topic = APPLY_PAYMENT_RESULT,
            key = key,
            kClass = ApplyPaymentResultEvent::class
        ).map { record ->
            record.value().let {
                if (!it.success) throw TransactionException(it.failureReason)
                it.restBalance
            }
        }
            .awaitFirstOrNull()
            ?: throw TransactionException()
    }

}
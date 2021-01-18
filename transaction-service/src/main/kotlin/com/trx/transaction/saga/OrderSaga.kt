package com.trx.transaction.saga

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.Topic
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.OrderCreateEvent
import com.trx.transaction.state.OrderPending
import com.trx.transaction.state.OrderSagaState
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult

class OrderSaga (
    private var state: OrderSagaState,
    val orderId: Int,
    val customerId: Int,
    val productId: Int,
    val count: Int,
    val key: String
) {

    @Autowired
    private lateinit var eventPublisher: TransactionEventPublisher

    companion object {
        fun init(
            key: String,
            event: OrderCreateEvent
        ): OrderSaga = OrderSaga(
            state = OrderPending(),
            orderId = event.orderId,
            customerId = event.customerId,
            productId = event.productId,
            count = event.count,
            key = key
        )
    }

    suspend fun changeStateAndOperate(state: OrderSagaState) {
        this.state = state
        this.operate()
    }

    suspend fun operate() {
        state.operate(this)
    }

    fun publishEvent(topic: String, key: String, event: Any): Mono<SenderResult<Void>> {
        return eventPublisher.publishEvent(topic, key, event)
    }
}
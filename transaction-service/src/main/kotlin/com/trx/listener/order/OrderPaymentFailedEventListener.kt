package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.CHECK_PRODUCT_SUCCEED
import com.trx.topic.Topic.ORDER_CREATE_TRANSACTION
import com.trx.topic.Topic.PAYMENT_FAILED
import com.trx.topic.Topic.PAYMENT_SUCCEED
import com.trx.topic.event.*
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.saga.OrderSaga
import com.trx.transaction.state.OrderPaymentFinished
import com.trx.transaction.state.OrderProductChecked
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 상품 확인 완료 => 결제 요청
 */
@Component
class OrderPaymentFailedEventListener(
    private val objectMapper: ObjectMapper,
    private val eventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [PAYMENT_FAILED], groupId = "transaction-orchestrator", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentFailed::class.java)

        boundedElasticScope.launch {
            OrderSagaInMemoryRepository.findByID(key)?.let {
                it.changeStateAndOperate(
                    OrderPaymentFinished(eventPublisher)
                )
            }
        }
    }
}

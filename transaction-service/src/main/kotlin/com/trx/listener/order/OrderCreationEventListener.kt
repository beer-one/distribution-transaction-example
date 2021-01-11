package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.ORDER_CREATE_TRANSACTION
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.OrderCreateEvent
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 주문 생성 이벤트 리스너
 * 상품 확인 -> 결제 -> Order Approved
 */
@Component
class OrderCreationEventListener(
    private val objectMapper: ObjectMapper,
    private val eventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [ORDER_CREATE_TRANSACTION], groupId = "transaction-orchestrator", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCreateEvent::class.java)

        val orderSaga = OrderSaga.init(eventPublisher, key, event)

        boundedElasticScope.launch {
            OrderSagaInMemoryRepository.save(key, orderSaga)
            orderSaga.operate()
        }
    }
}

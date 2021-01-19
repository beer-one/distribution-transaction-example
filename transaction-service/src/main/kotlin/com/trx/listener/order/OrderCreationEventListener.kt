package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.ORDER_CREATED
import com.trx.topic.event.OrderCreateEvent
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
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
    private val eventPublisher: TransactionEventPublisher,
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_CREATED], groupId = "transaction-orchestrator", containerFactory = "orderCreationEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCreateEvent::class.java)

        logger.info("Topic: $ORDER_CREATED, key: $key, event: $event")

        val orderSaga = OrderSaga.init(eventPublisher, key, event)

        boundedElasticScope.launch {
            OrderSagaInMemoryRepository.save(key, orderSaga)
            orderSaga.operate()
        }
    }
}

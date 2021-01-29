package com.trx.presentation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.order.OrderCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.domain.enums.OrderStatus
import com.trx.topic.Topic.ORDER_CANCELED
import com.trx.topic.event.OrderCancelEvent
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 에러 => 주문 취소 상태로 변경
 *
 * 편의상 도메인 객체 주입.. (OrderRepository)
 */
@Component
class OrderCancelEventListener(
    private val objectMapper: ObjectMapper,
    private val orderCommandService: OrderCommandService
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_CANCELED], groupId = "order-consumer", containerFactory = "orderCancelEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCancelEvent::class.java)

        logger.info("Topic: $ORDER_CANCELED, key: $key, event: $event")

        boundedElasticScope.launch {
            orderCommandService.modifyOrderStatus(event.orderId, OrderStatus.CANCELED)
        }

        acknowledgment.acknowledge()
    }
}

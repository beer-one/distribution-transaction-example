package com.trx.presentation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.order.OrderCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.ORDER_FAILED
import com.trx.topic.event.OrderFailed
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 에러 => 주문 취소 상태로 변경
 */
@Component
class OrderFailedEventListener(
    private val objectMapper: ObjectMapper,
    private val orderCommandService: OrderCommandService
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_FAILED], groupId = "order-consumer", containerFactory = "orderFailedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderFailed::class.java)

        logger.info("Topic: $ORDER_FAILED, key: $key, event: $event")

        boundedElasticScope.launch {
            orderCommandService.cancel(event.orderId, event.failureReason)
        }

        acknowledgment.acknowledge()
    }
}

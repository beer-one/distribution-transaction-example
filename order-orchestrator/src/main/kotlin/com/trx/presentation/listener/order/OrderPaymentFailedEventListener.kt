package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.PAYMENT_FAILED
import com.trx.topic.event.*
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.state.OrderPaymentFailed
import com.trx.transaction.state.OrderPaymentFinished
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 상품 확인 완료 => 결제 요청
 */
@Component
class OrderPaymentFailedEventListener(
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PAYMENT_FAILED], groupId = "order-orchestrator", containerFactory = "orderPaymentFailedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentFailed::class.java)

        logger.info("Topic: $PAYMENT_FAILED, key: $key, event: $event")
        logger.info("Failure reason: ${event.failureReason}")

        OrderSagaInMemoryRepository.findByID(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderPaymentFailed(event.failureReason)
                )
            }
            acknowledgment.acknowledge()
        }
    }
}

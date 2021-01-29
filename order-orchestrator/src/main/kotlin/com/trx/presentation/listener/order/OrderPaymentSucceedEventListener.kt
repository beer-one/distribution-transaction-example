package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.PAYMENT_SUCCEED
import com.trx.topic.event.*
import com.trx.transaction.OrderSagaInMemoryRepository
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
class OrderPaymentSucceedEventListener(
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PAYMENT_SUCCEED], groupId = "order-orchestrator", containerFactory = "orderPaymentSucceedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentSucceed::class.java)

        logger.info("Topic: $PAYMENT_SUCCEED, key: $key, event: $event")

        OrderSagaInMemoryRepository.findByID(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderPaymentFinished()
                )
            }
            acknowledgment.acknowledge()
        }
    }
}

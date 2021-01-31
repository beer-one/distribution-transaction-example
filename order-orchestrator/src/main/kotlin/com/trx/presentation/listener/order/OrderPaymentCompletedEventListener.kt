package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.PAYMENT_COMPLETED
import com.trx.topic.event.*
import com.trx.transaction.SagaRepository
import com.trx.transaction.state.OrderPaymentCompleted
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 결제 완료 => 승인 상태로
 */
@Component
class OrderPaymentCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PAYMENT_COMPLETED], groupId = "order-orchestrator", containerFactory = "orderPaymentCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentCompleted::class.java)

        logger.info("Topic: $PAYMENT_COMPLETED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderPaymentCompleted()
                )
                sagaRepository.deleteById(key)
            }
            acknowledgment.acknowledge()
        }
    }
}

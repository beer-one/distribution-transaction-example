package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.CHECK_PRODUCT_COMPLETED
import com.trx.topic.event.CheckProductCompleted
import com.trx.transaction.SagaRepository
import com.trx.transaction.state.OrderProductChecked
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
class OrderProductCheckCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [CHECK_PRODUCT_COMPLETED], groupId = "order-orchestrator", containerFactory = "orderProductCheckCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductCompleted::class.java)

        logger.info("Topic: $CHECK_PRODUCT_COMPLETED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderProductChecked(event.totalPrice)
                )
            }
            acknowledgment.acknowledge()
        }
    }
}

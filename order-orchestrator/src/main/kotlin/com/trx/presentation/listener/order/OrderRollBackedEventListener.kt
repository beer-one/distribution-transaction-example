package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.ORDER_ROLLBACKED
import com.trx.topic.event.CheckProductFailed
import com.trx.transaction.SagaRepository
import com.trx.transaction.state.OrderRollBacked
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
class OrderRollBackedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_ROLLBACKED], groupId = "order-orchestrator", containerFactory = "orderRollBackedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductFailed::class.java)

        logger.info("Topic: $ORDER_ROLLBACKED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderRollBacked(event.failureReason)
                )
                sagaRepository.deleteById(key)
            }
            acknowledgment.acknowledge()
        }
    }
}

package com.trx.presentation.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.CHECK_PRODUCT_SUCCEED
import com.trx.topic.event.CheckProductSucceed
import com.trx.transaction.OrderSagaInMemoryRepository
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
class OrderProductCheckSucceedEventListener(
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [CHECK_PRODUCT_SUCCEED], groupId = "transaction-orchestrator", containerFactory = "orderProductCheckSucceedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductSucceed::class.java)

        logger.info("Topic: $CHECK_PRODUCT_SUCCEED, key: $key, event: $event")

        OrderSagaInMemoryRepository.findByID(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderProductChecked(event.totalPrice)
                )
            }
            acknowledgment.acknowledge()
        }
    }
}

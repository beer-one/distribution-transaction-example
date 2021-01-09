package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.topic.Topic.ORDER_CREATE_TRANSACTION
import com.trx.topic.event.CheckProductEvent
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
class OrderCreationTransactionEventListener(
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [ORDER_CREATE_TRANSACTION], groupId = "transaction-orchestrator", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductEvent::class.java)

    }

}

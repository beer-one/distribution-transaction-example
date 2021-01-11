package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.domain.repository.OrderRepository
import com.trx.topic.Topic.ORDER_CANCELED
import com.trx.topic.event.OrderApproveEvent
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.state.OrderApproved
import com.trx.transaction.state.OrderCanceled
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 성공 => 주문 승인 상태로 변경 (FINAL)
 *
 * 편의상 도메인 객체 주입.. (OrderRepository)
 */
@Component
class OrderApproveEventListener(
    private val objectMapper: ObjectMapper,
    private val orderRepository: OrderRepository
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [ORDER_CANCELED], groupId = "transaction-orchestrator", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderApproveEvent::class.java)

        boundedElasticScope.launch {
            OrderSagaInMemoryRepository.findByID(key)?.let {
                it.changeStateAndOperate(
                    OrderApproved(orderRepository)
                )

                OrderSagaInMemoryRepository.deleteById(key)
            }
        }
    }
}

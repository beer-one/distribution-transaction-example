package com.trx.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.product.ProductCommandService
import com.trx.topic.Topic.CHECK_PRODUCT_ROLLBACK
import com.trx.topic.event.ProductRollBackEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ProductRollBackEventListener(
    private val objectMapper: ObjectMapper,
    private val productCommandService: ProductCommandService
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [CHECK_PRODUCT_ROLLBACK], groupId = "product-consumer", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), ProductRollBackEvent::class.java)

        productCommandService.incrementProductCount(event)
    }

}

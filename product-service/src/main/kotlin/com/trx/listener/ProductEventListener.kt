package com.trx.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.application.product.ProductCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.CHECK_PRODUCT
import com.trx.topic.Topic.CHECK_PRODUCT_RESULT
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.CheckProductResultEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ProductEventListener(
    private val objectMapper: ObjectMapper,
    private val productCommandService: ProductCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [CHECK_PRODUCT], containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductEvent::class.java)

        try {
            val price = productCommandService.checkAndSubtractProduct(event)
            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_RESULT,
                key = key,
                event = CheckProductResultEvent.success(price)
            )
        } catch (e: Exception) {
            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_RESULT,
                key = key,
                event = CheckProductResultEvent.fail("무슨 오류")
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }
    }

}

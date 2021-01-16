package com.trx.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.application.product.ProductCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.errors.CustomException
import com.trx.topic.Topic.CHECK_PRODUCT
import com.trx.topic.Topic.CHECK_PRODUCT_FAILED
import com.trx.topic.Topic.CHECK_PRODUCT_SUCCEED
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.CheckProductFailed
import com.trx.topic.event.CheckProductSucceed
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [CHECK_PRODUCT], groupId = "product-consumer", containerFactory = "productEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductEvent::class.java)

        logger.info("Topic: $CHECK_PRODUCT, key: $key, event: $event")

        try {
            val price = productCommandService.checkAndSubtractProduct(event)
            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_SUCCEED,
                key = key,
                event = CheckProductSucceed(price)
            )
        } catch (e: CustomException) {
            logger.error("[Error]: ", e)

            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_FAILED,
                key = key,
                event = CheckProductFailed(e.message!!)
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }
    }
}

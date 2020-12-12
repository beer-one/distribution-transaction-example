package com.trx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.web.reactive.config.EnableWebFlux

@EnableKafka
@ConfigurationPropertiesScan
@SpringBootApplication
class ProductServiceApplication

fun main(args: Array<String>) {
    runApplication<ProductServiceApplication>(*args)
}
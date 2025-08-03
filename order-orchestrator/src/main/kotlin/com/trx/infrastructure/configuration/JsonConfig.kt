package com.trx.infrastructure.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JsonConfig {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(
            KotlinModule.Builder()
                .enable(KotlinFeature.NullIsSameAsDefault)
                .build()
        )
        enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}
package com.trx.utils

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource

object MessageConverter {
    private val locale = LocaleContextHolder.getLocale()
    private val message: MessageSource = ResourceBundleMessageSource().apply {
        setBasenames("messages/message", "messages/error")
        setDefaultEncoding("UTF-8")
    }

    fun getMessage(code: String, vararg args: Any): String {
        val default = "No message for $code"
        return message.getMessage(code, args, default, LocaleContextHolder.getLocale()) ?: default
    }

    fun getMessageOrNull(code: String, vararg args: Any): String? {
        return message.getMessage(code, args, null, locale)
    }
}

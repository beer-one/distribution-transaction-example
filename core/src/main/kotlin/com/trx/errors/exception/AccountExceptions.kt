package com.trx.errors.exception

import com.trx.errors.CustomException

class AccountNotFoundException(customerId: Int): CustomException() {
    override val message: String = "(customerId = $customerId)인 계정이 없습니다."
}

class ExistedAccountException(customerId: Int): CustomException() {
    override val message: String = "(customerId = $customerId)인 계정이 이미 있습니다."
}

class InsufficientBalanceException(balance: Int, required: Int): CustomException() {
    override val message: String = "잔액이 부족합니다. current: $balance, required: $required"
}
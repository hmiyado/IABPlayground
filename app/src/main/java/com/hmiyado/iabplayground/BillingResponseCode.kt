package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult

enum class BillingResponseCode {
    OK,
    BILLING_UNAVAILABLE,
    SERVICE_TIMEOUT,
    FEATURE_NOT_SUPPORTED,
    SERVICE_DISCONNECTED,
    USER_CANCELED,
    SERVICE_UNAVAILABLE,
    ITEM_UNAVAILABLE,
    DEVELOPER_ERROR,
    ERROR,
    ITEM_ALREADY_OWNED,
    ITEM_NOT_OWNED,
    UNEXPECTED_APPLICATION_ERROR;

    companion object {
        fun from(billingResult: BillingResult): BillingResponseCode =
            from(billingResult.responseCode)

        private fun from(intCode: Int): BillingResponseCode = when (intCode) {
            BillingClient.BillingResponseCode.OK -> OK
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BILLING_UNAVAILABLE
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> SERVICE_TIMEOUT
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FEATURE_NOT_SUPPORTED
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> SERVICE_DISCONNECTED
            BillingClient.BillingResponseCode.USER_CANCELED -> USER_CANCELED
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ITEM_UNAVAILABLE
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DEVELOPER_ERROR
            BillingClient.BillingResponseCode.ERROR -> ERROR
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ITEM_ALREADY_OWNED
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ITEM_NOT_OWNED
            else -> UNEXPECTED_APPLICATION_ERROR
        }
    }
}

fun BillingResult.responseCode(): BillingResponseCode = BillingResponseCode.from(this)
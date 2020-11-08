package com.hmiyado.iabplayground

import com.android.billingclient.api.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun BillingClient.querySkuDetails(params: SkuDetailsParams): Pair<BillingResult, List<SkuDetails>> {
    return suspendCoroutine {
        querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            it.resume(billingResult to (skuDetailsList ?: emptyList()))
        }
    }
}

typealias OutToken = String

suspend fun BillingClient.consume(params: ConsumeParams): Pair<BillingResult, OutToken> {
    return suspendCoroutine { continuation ->
        consumeAsync(params) { billingResult, outToken ->
            continuation.resume(billingResult to outToken)
        }
    }
}

suspend fun BillingClient.acknowledge(params: AcknowledgePurchaseParams): BillingResult {
    return suspendCoroutine { continuation ->
        acknowledgePurchase(params) { billingResult ->
            continuation.resume(billingResult)
        }
    }
}
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
    return suspendCoroutine {
        consumeAsync(params) { billingResult, outToken ->
            it.resume(billingResult to outToken)
        }
    }
}
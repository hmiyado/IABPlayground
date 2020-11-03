package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun BillingClient.querySkuDetails(params: SkuDetailsParams): Pair<BillingResult, List<SkuDetails>> {
    return suspendCoroutine {
        querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            it.resume(billingResult to (skuDetailsList ?: emptyList()))
        }
    }
}

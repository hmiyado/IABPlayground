package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class BillingClientState {
    object BillingServiceDisconnected : BillingClientState()

    data class BillingSetupFinished(val billingResult: BillingResult) : BillingClientState()
}

suspend fun BillingClient.startConnection(): BillingClientState = suspendCoroutine {
    this@startConnection.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            it.resume(BillingClientState.BillingSetupFinished(billingResult))
        }

        override fun onBillingServiceDisconnected() {
            it.resume(BillingClientState.BillingServiceDisconnected)
        }
    })
}


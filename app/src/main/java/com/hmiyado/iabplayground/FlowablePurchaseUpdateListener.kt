package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlowablePurchaseUpdateListener(private val scope: CoroutineScope) : PurchasesUpdatedListener {
    private val _purchaseUpdated: MutableStateFlow<Pair<BillingResult, List<Purchase>>> =
        MutableStateFlow(BillingResult.newBuilder().build() to emptyList())
    val purchaseUpdated: StateFlow<Pair<BillingResult, List<Purchase>>>
        get() = _purchaseUpdated

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchaseList: MutableList<Purchase>?
    ) {
        scope.launch {
            _purchaseUpdated.value = billingResult to (purchaseList ?: emptyList())
        }
    }
}
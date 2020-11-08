package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetailsParams

object IABPlaygroundSku {
    private val inAppProductIds = listOf("iabplayground_item_1")
    private val subscriptionProductIds = listOf("iabplayground_subscription_1")

    val InAppSku = SkuDetailsParams.newBuilder()
        .setSkusList(inAppProductIds)
        .setType(BillingClient.SkuType.INAPP)
        .build()

    val SubscriptionSku = SkuDetailsParams.newBuilder()
        .setSkusList(subscriptionProductIds)
        .setType(BillingClient.SkuType.SUBS)
        .build()

    fun isConsumable(purchase: Purchase): Boolean {
        if (purchase.isAcknowledged) {
            return false
        }
        return purchase.sku in inAppProductIds
    }

    fun isAcknowledgeEnabled(purchase: Purchase): Boolean {
        if (purchase.isAcknowledged) {
            return false
        }
        return purchase.sku in subscriptionProductIds
    }
}
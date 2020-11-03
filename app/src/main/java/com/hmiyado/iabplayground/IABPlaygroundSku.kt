package com.hmiyado.iabplayground

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetailsParams

object IABPlaygroundSku {
    val InAppSku = SkuDetailsParams.newBuilder()
        .setSkusList(listOf("iabplayground_item_1"))
        .setType(BillingClient.SkuType.INAPP)
        .build()

    val SubscriptionSku = SkuDetailsParams.newBuilder()
        .setSkusList(listOf("iabplayground_subscription_1"))
        .setType(BillingClient.SkuType.SUBS)
        .build()
}
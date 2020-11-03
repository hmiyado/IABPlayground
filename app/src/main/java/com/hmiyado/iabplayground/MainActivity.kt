package com.hmiyado.iabplayground

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.hmiyado.iabplayground.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainViewModel::class.java)
    }

    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, mutableList ->
        Logger.debug("onPurchaseUpdated: ${billingResult.responseCode()}")
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)


        viewModel.viewModelScope.launch {
            var state = billingClient.startConnection()
            while (state == BillingClientState.BillingServiceDisconnected) {
                delay(500)
                billingClient.endConnection()
                state = billingClient.startConnection()
            }
            val (inAppBillingResult, inAppSkuDetailsList) = querySkuDetails(IABPlaygroundSku.InAppSku)
            val inAppDescription =
                if (inAppBillingResult.responseCode() == BillingResponseCode.OK) {
                    inAppSkuDetailsList.joinToString()
                } else {
                    inAppBillingResult.debugMessage
                }

            val (subscriptionBillingResult, subscriptionSkuDetailsList) = querySkuDetails(
                IABPlaygroundSku.SubscriptionSku
            )
            val subscriptionDescription =
                if (subscriptionBillingResult.responseCode() == BillingResponseCode.OK) {
                    subscriptionSkuDetailsList.joinToString()
                } else {
                    subscriptionBillingResult.debugMessage
                }


            binding.textView.text = "$inAppDescription \n $subscriptionDescription"
        }.start()

        binding.buttonStartBillingItem.setOnClickListener {
            viewModel.viewModelScope.launch {
                val (_, skuDetails) = querySkuDetails(IABPlaygroundSku.InAppSku)
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails.first())
                    .build()
                val result = billingClient.launchBillingFlow(this@MainActivity, flowParams)
                Logger.debug("after billing ${result.responseCode()}")
            }.start()
        }
    }

    private suspend fun querySkuDetails(params: SkuDetailsParams): Pair<BillingResult, List<SkuDetails>> {
        return suspendCoroutine {
            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                it.resume(billingResult to (skuDetailsList ?: emptyList()))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
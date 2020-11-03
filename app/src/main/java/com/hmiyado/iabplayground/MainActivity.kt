package com.hmiyado.iabplayground

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ConsumeParams
import com.hmiyado.iabplayground.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainViewModel::class.java)
    }

    private val purchaseUpdateListener by lazy {
        FlowablePurchaseUpdateListener(viewModel.viewModelScope)
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
            val (inAppBillingResult, inAppSkuDetailsList) = billingClient.querySkuDetails(
                IABPlaygroundSku.InAppSku
            )
            val inAppDescription =
                if (inAppBillingResult.responseCode() == BillingResponseCode.OK) {
                    inAppSkuDetailsList.joinToString()
                } else {
                    inAppBillingResult.debugMessage
                }

            val (subscriptionBillingResult, subscriptionSkuDetailsList) = billingClient.querySkuDetails(
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

        viewModel
            .viewModelScope
            .launch {
                purchaseUpdateListener.purchaseUpdated
                    .collect { (billingResult, purchaseList) ->
                        binding.textView.text = """
                            ${billingResult.responseCode()}
                            ${purchaseList.joinToString()}
                            ${binding.textView.text}
                        """.trimIndent()

                        val unAcknowledgedPurchase =
                            purchaseList.firstOrNull { !it.isAcknowledged } ?: return@collect
                        val params = ConsumeParams.newBuilder()
                            .setPurchaseToken(unAcknowledgedPurchase.purchaseToken)
                            .build()
                        val (consumeResult, outToken) = billingClient.consume(params)
                        binding.textView.text = """
                            ${consumeResult.responseCode()}
                            $outToken
                            ${binding.textView.text}
                        """.trimIndent()
                    }
            }
            .start()

        binding.buttonStartBillingItem.setOnClickListener {
            viewModel.viewModelScope.launch {
                val (_, skuDetails) = billingClient.querySkuDetails(IABPlaygroundSku.InAppSku)
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails.first())
                    .build()
                val result = billingClient.launchBillingFlow(this@MainActivity, flowParams)
                Logger.debug("after billing ${result.responseCode()}")
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
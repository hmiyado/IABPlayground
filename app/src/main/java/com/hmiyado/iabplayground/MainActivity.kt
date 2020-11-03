package com.hmiyado.iabplayground

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.hmiyado.iabplayground.databinding.ActivityMainBinding
import kotlinx.coroutines.*
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

    private val billingClientStateListener: BillingClientStateListener =
        object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Logger.debug("onBillingServiceDisconnected")
                billingClient.endConnection()
                billingClient.startConnection(this)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Logger.debug("onBillingSetupFinished: ${billingResult.responseCode()}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        billingClient.startConnection(billingClientStateListener)

        viewModel.viewModelScope.launch {
            while (!billingClient.isReady) {
                delay(500)
                Log.d("","waiting billing client is ready")
            }
            val (billingResult, skuDetailsList) = querySkuDetails()
            Log.d("","onSkuDetailsResponse: ${billingResult.responseCode()}, $skuDetailsList")
            binding.textView.text =
                if (billingResult.responseCode() == BillingResponseCode.OK) {
                    skuDetailsList.joinToString()
                } else {
                    billingResult.debugMessage
                }
        }.start()
//
//        binding.buttonStartBillingItem.setOnClickListener {
//            viewModel.viewModelScope.launch {
//                val skuDetails = querySkuDetails()
//                val flowParams = BillingFlowParams.newBuilder()
//                    .setSkuDetails(skuDetails.first())
//                    .build()
//                val result = billingClient.launchBillingFlow(this@MainActivity, flowParams)
//                Logger.debug("after billing ${result.responseCode()}")
//            }.start()
//        }
    }

    private suspend fun querySkuDetails(): Pair<BillingResult, List<SkuDetails>> {
        val skuList = listOf("iabplayground_item_1")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        return suspendCoroutine {
            billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                it.resume(billingResult to (skuDetailsList ?: emptyList()))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
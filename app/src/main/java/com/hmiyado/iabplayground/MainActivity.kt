package com.hmiyado.iabplayground

import android.os.Bundle
import android.util.Log
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
            val (billingResult, skuDetailsList) = querySkuDetails()
            Log.d("", "onSkuDetailsResponse: ${billingResult.responseCode()}, $skuDetailsList")
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
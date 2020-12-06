package com.hmiyado.iabplayground

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ConsumeParams
import com.hmiyado.iabplayground.databinding.ActivityMainBinding
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
            billingClient.startConnection()

            val (inAppBillingResult, inAppSkuDetailsList) = billingClient.querySkuDetails(
                IABPlaygroundSku.InAppSku
            )

            val (subscriptionBillingResult, subscriptionSkuDetailsList) = billingClient.querySkuDetails(
                IABPlaygroundSku.SubscriptionSku
            )

            val purchaseListItemAdapter =
                PurchaseListItemAdapter(inAppSkuDetailsList + subscriptionSkuDetailsList)
            binding.purchaseList.adapter =
                purchaseListItemAdapter
            binding.purchaseList.layoutManager = LinearLayoutManager(baseContext)
            purchaseListItemAdapter.onClick
                .collect {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                    val result = billingClient.launchBillingFlow(this@MainActivity, flowParams)
                    Logger.debug("after billing ${result.responseCode()}")

                }
        }.start()

        viewModel
            .viewModelScope
            .launch {
                purchaseUpdateListener.purchaseUpdated
                    .collect { (billingResult, purchaseList) ->
                        binding.textView.appendHead(
                            """
                        # PurchaseUpdated #
                        ResponseCode: ${billingResult.responseCode()}
                        PurchaseList: ${purchaseList.joinToString()}
                        """.trimIndent()
                        )

                        val purchase = purchaseList.firstOrNull() ?: return@collect
                        if (IABPlaygroundSku.isConsumable(purchase)) {
                            val params = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            val (result, outToken) = billingClient.consume(params)
                            binding.textView.appendHead(
                                """
                            # Consumed #
                            ResponseCode: ${result.responseCode()}
                            OutToken: $outToken
                            """.trimIndent()
                            )
                        }

                        if (IABPlaygroundSku.isAcknowledgeEnabled(purchase)) {
                            val params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            val result = billingClient.acknowledge(params)
                            binding.textView.appendHead(
                                """
                            # Acknowledged #
                            ResponseCode: ${result.responseCode().toString().trimIndent()}
                            """.trimIndent()
                            )
                        }
                    }
            }
            .start()
    }

    private fun TextView.appendHead(text: CharSequence) {
        this.text = text.toString() + "\n" + this.text
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
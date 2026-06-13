package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

import com.example.BuildConfig
import com.example.api.CreatePaymentIntentRequest
import com.example.api.PaymentApi
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPointPack by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(PointPack("Starter Pack", 100, "Rs 99")) }
    var pointBalance by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(PointWallet.balance(context)) }
    var isCheckoutLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val paymentSheet = rememberPaymentSheet(
        paymentResultCallback = { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    pointBalance = PointWallet.add(context, selectedPointPack.points)
                    Toast.makeText(context, "Payment successful! Added ${selectedPointPack.points} points.", Toast.LENGTH_SHORT).show()
                    onClose()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(context, "Payment canceled.", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(context, "Payment failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    fun startPointCheckout(pack: PointPack) {
        selectedPointPack = pack
        if (BuildConfig.PAYMENTS_BASE_URL.isBlank()) {
            Toast.makeText(context, "Payment backend URL missing. Add PAYMENTS_BASE_URL in .env.", Toast.LENGTH_LONG).show()
            return
        }

        scope.launch {
            isCheckoutLoading = true
            try {
                val response = PaymentApi.create(BuildConfig.PAYMENTS_BASE_URL).createPaymentIntent(
                    CreatePaymentIntentRequest(
                        packName = pack.name,
                        points = pack.points,
                        priceLabel = pack.price
                    )
                )
                paymentSheet.presentWithPaymentIntent(
                    paymentIntentClientSecret = response.clientSecret,
                    configuration = PaymentSheet.Configuration(
                        merchantDisplayName = "Leaf Lens",
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Payment setup error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isCheckoutLoading = false
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Buy Points", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pay only when AI helps your garden.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use points for plant scans and AI Botanist talks. Current balance: $pointBalance points.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                PointPackCard(
                    title = "Starter Pack",
                    price = "Rs 99",
                    points = 100,
                    subtitle = "10 plant scans or 50 AI talks",
                    features = listOf("Best for quick diagnosis", "No subscription lock-in", "Points stay in your wallet"),
                    isLoading = isCheckoutLoading,
                    onBuy = { startPointCheckout(PointPack("Starter Pack", 100, "Rs 99")) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PointPackCard(
                    title = "Garden Pack",
                    price = "Rs 249",
                    points = 300,
                    subtitle = "30 plant scans or 150 AI talks",
                    features = listOf("Most useful for active plant owners", "Lower cost per scan", "Works for scan and chat"),
                    badge = "Best Value",
                    isLoading = isCheckoutLoading,
                    onBuy = { startPointCheckout(PointPack("Garden Pack", 300, "Rs 249")) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PointPackCard(
                    title = "Nursery Pack",
                    price = "Rs 499",
                    points = 750,
                    subtitle = "75 plant scans or 375 AI talks",
                    features = listOf("For many plants or client visits", "Highest point bonus", "Supports cloud AI costs"),
                    isLoading = isCheckoutLoading,
                    onBuy = { startPointCheckout(PointPack("Nursery Pack", 750, "Rs 499")) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Plant scan: ${PointWallet.SCAN_COST} points. AI talk: ${PointWallet.CHAT_COST} points. New users get ${PointWallet.STARTER_POINTS} free points.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secure payments by Stripe (backend PaymentIntent required)", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No monthly commitment. Terms and conditions apply.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class PointPack(val name: String, val points: Int, val price: String)

@Composable
fun PointPackCard(
    title: String,
    price: String,
    points: Int,
    subtitle: String,
    features: List<String>,
    badge: String? = null,
    isLoading: Boolean = false,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Envelope),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = EnvTextPrimary)
                badge?.let {
                    Surface(color = HeroCardBg.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(it, color = HeroCardBg, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            Text("$points points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HeroCardBg)
            Text("$price - $subtitle", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            features.forEach { PricingFeatureItem(it) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HeroCardBg),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Preparing Checkout..." else "Buy $points Points", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun PricingFeatureItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(HeroCardBg.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = HeroCardBg, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, color = EnvTextPrimary)
    }
}

private val Envelope = Color(0xFFF3F4ED)


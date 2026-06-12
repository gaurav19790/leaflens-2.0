package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast

import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val paymentSheet = rememberPaymentSheet(
        paymentResultCallback = { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    val prefs = context.getSharedPreferences("leaflens_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_premium", true).apply()
                    Toast.makeText(context, "Payment successful! Welcome to Premium.", Toast.LENGTH_SHORT).show()
                    onClose()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(context, "Payment canceled.", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(context, "Payment simulation failed (Backend Required).", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    fun simulateStripePayment() {
        try {
            // Note: In a production app, you would fetch this from your secure backend
            val fakeClientSecret = "pi_3MtwBwLkdIwHu7ix28a3tqPa_secret_a1b2c3d4e5f6g7h8i9j0k"
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = fakeClientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Leaf Lens",
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Stripe setup error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Upgrade to Pro", fontWeight = FontWeight.Bold) },
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
                    text = "Unlock the full potential of your garden.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FloraScan Premium gives you unlimited AI plant analysis, custom care schedules, and disease history tracking.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Monthly Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Envelope),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Monthly Plan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = EnvTextPrimary)
                        Text("₹149 / month", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HeroCardBg)
                        Spacer(modifier = Modifier.height(12.dp))
                        PricingFeatureItem("Unlimited AI plant scans")
                        PricingFeatureItem("Priority AI disease diagnosis")
                        PricingFeatureItem("Smart, personalized reminders")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                simulateStripePayment()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HeroCardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Subscribe Monthly", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yearly Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Envelope),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Yearly Plan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = EnvTextPrimary)
                            Surface(color = HeroCardBg.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text("Best Value", color = HeroCardBg, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Text("₹999 / year", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HeroCardBg)
                        Text("Save ~44% annually", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        PricingFeatureItem("All Monthly features")
                        PricingFeatureItem("Cloud synced collections")
                        PricingFeatureItem("Support independent developers")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                simulateStripePayment()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HeroCardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Subscribe Yearly", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Free tier includes: 10 AI scans & full garden journal.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secure payments by Stripe (Test Mode)", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cancel anytime. Terms and conditions apply.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
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

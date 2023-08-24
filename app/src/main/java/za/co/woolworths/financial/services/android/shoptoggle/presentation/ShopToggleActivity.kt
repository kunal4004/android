package za.co.woolworths.financial.services.android.shoptoggle.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoptoggle.presentation.components.shopToggleScreen
import za.co.woolworths.financial.services.android.shoptoggle.data.ShopToggleData
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

class ShopToggleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: adding for showing UI
        val item = List(3) { index ->
            ShopToggleData(
                index,
                title = "use standard delivery",
                subTitle = "shop FASHION, BEAUTY, HOME AND food",
                icon = R.drawable.ic_toggle_collection_bag,
                deliveryType = "Earliest Standard Delivery Dates:",
                deliveryCost = "Fashion, Beauty, Home: Weds, 21 March",
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Delivery Cost:",
                learnMore = "Determined at checkout. Learn more",
                deliveryButtonText = "SET TO STANDARD DELIVERY",
                isDashDelivery = true)
        }
        setContent {
            OneAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        TopAppBar(
                            modifier = Modifier.offset(x = (-18).dp),
                            title = { Text(text = "") },
                            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White),
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(painter = painterResource(id = R.drawable.back24),
                                        contentDescription = "Back Arrow")

                                }
                            }
                        )

                        shopToggleScreen(item)
                    }
                }
            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun HeaderViewPreview() {
        OneAppTheme {
           // shopToggleScreen(item)
        }
    }
}
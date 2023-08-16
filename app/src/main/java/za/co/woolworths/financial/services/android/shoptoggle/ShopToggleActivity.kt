package za.co.woolworths.financial.services.android.shoptoggle

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
import za.co.woolworths.financial.services.android.shoptoggle.components.shopToggleScreen
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

class ShopToggleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // expendable list
        val item  = List(3) { index ->
            ShopToggleData(
                1,
                title = "tesssst fgfg",
                subTitle = "hsdfkhsdfsddfdgdfg",
                icon = R.drawable.back24,
                deliveryType = "gsfjagdsfq",
                deliveryCost = "gsjfkjasgf",
                deliveryTime = "784872352",
                deliveryProduct = "food",
                learnMore = "Learn More"
            )
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
                            title = { Text(text = "") },
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
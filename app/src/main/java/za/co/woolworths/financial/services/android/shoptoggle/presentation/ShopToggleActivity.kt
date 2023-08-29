package za.co.woolworths.financial.services.android.shoptoggle.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.shoptoggle.presentation.components.shopToggleScreen
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@AndroidEntryPoint
class ShopToggleActivity : ComponentActivity() {
    private val viewModel by viewModels<ShopToggleViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                        shopToggleScreen(viewModel,
                            viewModel.listItem.value)
                    }
                }
            }

        }
    }

}
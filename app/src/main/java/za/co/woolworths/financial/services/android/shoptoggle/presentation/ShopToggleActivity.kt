package za.co.woolworths.financial.services.android.shoptoggle.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.shoptoggle.presentation.components.ShopToggleScreen
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
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
                    modifier = Modifier.fillMaxSize(), color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.sixteen_dp)
                    ) {

                        TopAppBar(modifier = Modifier.offset(x = (-18).dp),
                            title = { Text(text = "") },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.back24),
                                        contentDescription = stringResource(id = R.string.back_arrow)
                                    )

                                }
                            })

                        val state = viewModel.state.value

                        if (state.isLoading) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(color = Color.Black)
                            }
                        } else {
                            ShopToggleScreen(viewModel, state.data) { delivery ->
                                if (delivery != null) {
                                    // TODO, add the confirm address API call
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
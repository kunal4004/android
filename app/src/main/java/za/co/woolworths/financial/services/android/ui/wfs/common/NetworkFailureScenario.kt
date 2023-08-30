package za.co.woolworths.financial.services.android.ui.wfs.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.theme.HeaderGrey
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

sealed class ButtonEvent {
    object Retry : ButtonEvent()
    object Dismiss : ButtonEvent()
}


@Composable
fun FailureScenario(item: Pair<AnnotatedString, String?>, onTap: (ButtonEvent) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.height(57.dp))
        DividerThicknessEight()
        Box(modifier = Modifier.background(White)) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.npc_failure_icon),
                        modifier = Modifier.fillMaxSize(0.06f),
                        contentDescription = stringResource(id = R.string.failure_icon)
                    )
                    CircularProgress()
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextFuturaFamilySemiBoldHeader1(
                    text = stringResource(id = R.string.unfortunately_something_went_wrong),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    textColor = HeaderGrey
                )

                Spacer(modifier = Modifier.height(8.dp))

                LabelPhoneNumber(
                    LabelProperties(
                        annotatedString = item.first,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        annotatedPhoneNumber = item.second,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(60.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            ) {
                ButtonBlack { onTap(ButtonEvent.Retry) }
                ButtonNoBackground { onTap(ButtonEvent.Dismiss) }
            }
        }
    }
}


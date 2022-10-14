package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Preview(showBackground = true)
@Composable
fun FontsPreview(){
    OneAppTheme {
        Column {
            TitleLargeText(R.string.contact_us_financial_services)
            TitleLargeText("Financial Services")
            TitleMediumText(R.string.contact_us_financial_services)
            TitleMediumText("Financial Services")
            TitleSmallText(R.string.contact_us_financial_services)
            TitleSmallText("Financial Services")
        }
    }
}

@Composable
fun TitleLargeText(@StringRes id : Int) {
    Text(text = stringResource(id = id), style = MaterialTheme.typography.titleLarge)
}

@Composable
fun TitleLargeText(text : String?, modifier : Modifier = Modifier) {
    Text(text = text ?: "", modifier = modifier, style = MaterialTheme.typography.titleLarge)
}

@Composable
fun TitleMediumText(@StringRes id : Int) {
    Text(text = stringResource(id = id), style = MaterialTheme.typography.titleMedium)
}

@Composable
fun TitleMediumText(text : String?, modifier : Modifier = Modifier) {
    Text(text = text ?: "", modifier = modifier, style = MaterialTheme.typography.titleMedium)
}

@Composable
fun TitleSmallText(@StringRes id : Int) {
    Text(text = stringResource(id = id), style = MaterialTheme.typography.titleSmall)
}


@Composable
fun TitleSmallText(text : String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall)
}


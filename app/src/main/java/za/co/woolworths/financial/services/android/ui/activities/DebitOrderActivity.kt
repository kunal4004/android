
package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_debit_order.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import kotlin.math.absoluteValue

class DebitOrderActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debit_order)
        Utils.updateStatusBarBackground(this)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }

        var debitOrder: DebitOrder = intent.extras.getSerializable("DebitOrder") as DebitOrder

        var title = getResources().getString(R.string.debit_order_title)
                .replace("debit_order_status", if (debitOrder.debitOrderActive) "ACTIVE" else "EXPIRED")
        var description = getResources().getString(R.string.debit_order_description)
                .replace("debit_order_status", if (debitOrder.debitOrderActive) "active" else "expired")
        var amountToBeDebited = WFormatter.formatAmount(debitOrder.debitOrderProjectedAmount.toInt())
        if (amountToBeDebited.contains("-")) {
            amountToBeDebited = "- " + amountToBeDebited.replace("-", "")
        }

        tvDebitOrderTitle.setText(title)
        tvDebitOrderDescription.setText(description)
        tvDeductionDay.setText(debitOrder.debitOrderDeductionDay)
        tvAmountToBeDebited.setText(amountToBeDebited)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.DEBIT_ORDERS)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed();
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(
                R.anim.slide_in_from_left,
                R.anim.slide_out_to_right
        )
    }

}


package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityDebitOrderBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class DebitOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebitOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebitOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }

        val debitOrder: DebitOrder? = intent.extras?.getSerializable("DebitOrder") as? DebitOrder

        val title = bindString(R.string.debit_order_title)
                .replace("debit_order_status", if (debitOrder?.debitOrderActive == true) "ACTIVE" else "EXPIRED")
        val description = bindString(R.string.debit_order_description)
                .replace("debit_order_status", if (debitOrder?.debitOrderActive == true) "active" else "expired")
        var amountToBeDebited = CurrencyFormatter.formatAmountToRandAndCentWithSpace(debitOrder?.debitOrderProjectedAmount?.toInt() ?: 0)
        if (amountToBeDebited.contains("-")) {
            amountToBeDebited = "- " + amountToBeDebited.replace("-", "")
        }

        with(binding) {
            tvDebitOrderTitle.setText(title)
            tvDebitOrderDescription.text = description
            tvDeductionDay.text = debitOrder?.debitOrderDeductionDay
            tvAmountToBeDebited.text = amountToBeDebited
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.DEBIT_ORDERS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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

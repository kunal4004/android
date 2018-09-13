package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.awfs.coordination.R

import kotlinx.android.synthetic.main.activity_debit_order.*
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.WFormatter
import kotlin.math.absoluteValue

class DebitOrderActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var tvDebitOrderTitle: WTextView
    lateinit var tvDebitOrderDescription: WTextView
    lateinit var tvDeductionDay: WTextView
    lateinit var tvAmountToBeDebited: WTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debit_order)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.elevation = 0f
        supportActionBar!!.setTitle(null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayUseLogoEnabled(false)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.back24)
        toolbar.setNavigationOnClickListener { arrow -> onBackPressed()}

        tvDebitOrderTitle = findViewById(R.id.tvDebitOrderTitle)
        tvDebitOrderDescription = findViewById(R.id.tvDebitOrderDescription)
        tvDeductionDay = findViewById(R.id.tvDeductionDay)
        tvAmountToBeDebited = findViewById(R.id.tvAmountToBeDebited)

        var debitOrder: DebitOrder = intent.extras.getSerializable("DebitOrder") as DebitOrder

        var title = getResources().getString(R.string.debit_order_title)
                .replace("debit_order_status", if (debitOrder.debitOrderActive) "ACTIVE" else "EXPIRED")
        var description = getResources().getString(R.string.debit_order_description)
                .replace("debit_order_status", if (debitOrder.debitOrderActive) "active" else "expired")

        tvDebitOrderTitle.setText(title)
        tvDebitOrderDescription.setText(description)
        tvDeductionDay.setText(debitOrder.debitOrderDeductionDay)
        tvAmountToBeDebited.setText(WFormatter.formatAmount(debitOrder.debitOrderProjectedAmount.toDouble().absoluteValue))

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
                R.anim.slide_in_from_left,
                R.anim.slide_out_to_right
        )
    }

}

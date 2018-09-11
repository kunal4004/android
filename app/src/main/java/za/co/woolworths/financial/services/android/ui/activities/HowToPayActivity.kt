package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.accounts_how_to_pay.*
import za.co.woolworths.financial.services.android.util.Utils

class HowToPayActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accounts_how_to_pay)
        Utils.updateStatusBarBackground(this)
        initViews()
    }

    private fun initViews() {
        btnClose.setOnClickListener(this)
        loadPaymentOptions()
        loadAccountDetails()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun loadAccountDetails() {
        howToPayAccountDetails.removeAllViews()
        val inflater: LayoutInflater = LayoutInflater.from(this)
        for (i in 1..5) {
            val v: View = inflater.inflate(R.layout.how_to_pay_account_details_list_item, howToPayAccountDetails, false)
            howToPayAccountDetails.addView(v)
        }
    }

    private fun loadPaymentOptions() {
        howToPayOptionsList.removeAllViews()
        val inflater: LayoutInflater = LayoutInflater.from(this)
        for (i in 1..4) {
            val v: View = inflater.inflate(R.layout.how_to_pay_options_list_item, howToPayOptionsList, false)
            howToPayOptionsList.addView(v)
        }
    }
}

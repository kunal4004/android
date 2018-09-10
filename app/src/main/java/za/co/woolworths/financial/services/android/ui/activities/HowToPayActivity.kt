package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        loadBankDetails()

    }

    private fun loadBankDetails() {
        bankDetailsList.layoutManager = LinearLayoutManager(this)
        bankDetailsList.adapter = BankDetailsAdapter(this)
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

    class BankDetailsAdapter(var context: Activity) : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return 5
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.how_to_pay_account_details_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }
}

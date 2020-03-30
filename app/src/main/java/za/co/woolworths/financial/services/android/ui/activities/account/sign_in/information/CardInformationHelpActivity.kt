package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.card_information_activity.*
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.util.Utils

class CardInformationHelpActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val HELP_INFORMATION = "HELP_INFORMATION"
        const val  TERMS_AND_CONDITION_URL = "https://www.woolworths.co.za/corporate/cmp100103"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_information_activity)
        Utils.updateStatusBarBackground(this)

        val helpInformation = intent?.extras?.getString(HELP_INFORMATION, "")
        val informationList: MutableList<AccountHelpInformation>? = Gson().fromJson(helpInformation, object : TypeToken<MutableList<AccountHelpInformation>>() {}.type)

        informationList?.forEach { helpInformation ->
            val view = View.inflate(this, R.layout.account_card_information_item, null)
            val titleTextView: TextView? = view?.findViewById(R.id.titleTextView)
            val descriptionTextView: TextView? = view?.findViewById(R.id.descriptionTextView)
            titleTextView?.text = helpInformation.title
            descriptionTextView?.text = helpInformation.description
            informationContainerLinearLayout?.addView(view)
        }

        closeIcon?.setOnClickListener(this)
        termsAndConditionsTextView?.setOnClickListener(this)
        termsAndConditionsTextView?.paintFlags = termsAndConditionsTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeIcon -> onBackPressed()
            R.id.termsAndConditionsTextView -> Utils.openBrowserWithUrl(TERMS_AND_CONDITION_URL)
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}
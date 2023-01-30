package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CardInformationActivityBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.util.Utils

class CardInformationHelpActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val HELP_INFORMATION = "HELP_INFORMATION"
    }

    private lateinit var binding: CardInformationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardInformationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)

        val helpInformation = intent?.extras?.getString(HELP_INFORMATION, "")
        val informationList: MutableList<AccountHelpInformation>? = Gson().fromJson(helpInformation, object : TypeToken<MutableList<AccountHelpInformation>>() {}.type)

        with(binding) {
            informationList?.forEach { helpInformation ->
                val view = View.inflate(this@CardInformationHelpActivity, R.layout.account_card_information_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.titleTextView)
                val descriptionTextView: TextView? = view?.findViewById(R.id.descriptionTextView)
                titleTextView?.text = helpInformation.title
                descriptionTextView?.text = helpInformation.description
                informationContainerLinearLayout?.addView(view)
            }
            /** Note:: Temporary fix till we migrate PL&CC to SC enhancements */
            closeIcon.setColorFilter(ContextCompat.getColor(this@CardInformationHelpActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            closeIcon?.setOnClickListener(this@CardInformationHelpActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeIcon -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}
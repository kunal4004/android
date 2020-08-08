package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.save_card_and_pay_now_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString

class SaveCardAndPayNowFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.save_card_and_pay_now_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
        }

        saveCardPayNowButton?.text = bindString(R.string.pay_now_button_label)
        pmaSaveCardCheckbox?.setOnCheckedChangeListener { _, isChecked ->
            saveCardPayNowButton?.text = when (isChecked) {
                true -> bindString(R.string.save_card_and_pay_now_button_label)
                else -> bindString(R.string.pay_now_button_label)
            }
        }
    }
}
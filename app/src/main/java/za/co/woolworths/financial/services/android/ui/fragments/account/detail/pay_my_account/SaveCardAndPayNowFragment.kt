package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.save_card_and_pay_now_fragment.*
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.util.*

class SaveCardAndPayNowFragment : Fragment(), View.OnClickListener {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    private var navController: NavController? = null
    private var mAddCardResponse: AddCardResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.save_card_and_pay_now_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAddCardResponse = payMyAccountViewModel.getAddCardResponse()

        navController = Navigation.findNavController(view)

        setToolbarItem()
        populateField(payMyAccountViewModel.getAddCardResponse())
        onSaveCheckChangeListener()

        saveCardPayNowButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@SaveCardAndPayNowFragment)
        }
    }

    private fun setToolbarItem() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
        }
    }

    private fun onSaveCheckChangeListener() {
        saveCardPayNowButton?.text = bindString(R.string.pay_now_button_label)
        pmaSaveCardCheckbox?.setOnCheckedChangeListener { _, isChecked ->
            saveCardPayNowButton?.text = when (isChecked) {
                true -> bindString(R.string.save_card_and_pay_now_button_label)
                else -> bindString(R.string.pay_now_button_label)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateField(cardDetail: AddCardResponse?) {
        cardDetail?.card?.apply {
            nameOnCardValueTextView?.text = KotlinUtils.capitaliseFirstLetter(name_card.toLowerCase(Locale.getDefault()))
            expiryDateValueTextView?.text = "$exp_month / $exp_year"
            cardNumberValueTextView?.text = "**** **** **** $number"
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveCardPayNowButton -> {
                mAddCardResponse?.saveChecked = pmaSaveCardCheckbox.isChecked
                mAddCardResponse?.let { item -> payMyAccountViewModel.setAddCardResponse(item) }
                val navigateToProcessPayment = SaveCardAndPayNowFragmentDirections.actionSaveCardAndPayNowFragmentToPMAProcessRequestFragment()
                val options = NavOptions.Builder().setPopUpTo(R.id.saveCardAndPayNowFragment, true).build()
                navController?.navigate(navigateToProcessPayment, options)
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryBoardingLayoutBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation.CreditCardActivationAvailabilityDialogFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import java.util.*

class CreditCardDeliveryBoardingFragment : BaseFragmentBinding<CreditCardDeliveryBoardingLayoutBinding>(CreditCardDeliveryBoardingLayoutBinding::inflate) {

    private var navController: NavController? = null
    var bundle: Bundle? = null
    var accountBinNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BundleKeysConstants.BUNDLE)
        bundle?.apply {
            accountBinNumber = getString(BundleKeysConstants.ACCOUNTBI_NNUMBER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.apply {
            init()
            setupToolbar()
            setUpDeliveryNow.setOnClickListener {
                (activity as? CreditCardDeliveryActivity)?.mFirebaseCreditCardDeliveryEvent?.forMyAccountCreditCardDelivery()
                navController?.navigate(
                    R.id.action_to_creditCardDeliveryRecipientDetailsFragment,
                    bundleOf("bundle" to bundle)
                )
            }
            activateNow.setOnClickListener {
                //Jira OWT-243
                val calendar: Calendar = Calendar.getInstance()
                val day: Int = calendar.get(Calendar.DAY_OF_WEEK)
                if (day != Calendar.SUNDAY && Utils.isCreditCardActivationEndpointAvailable()) {
                    activity?.apply {
                        Utils.makeCall(AppConfigSingleton.creditCardDelivery?.callCenterNumber)
                    } } else {
                        activity?.supportFragmentManager?.let { CreditCardActivationAvailabilityDialogFragment.newInstance(accountBinNumber).show(it, CreditCardActivationAvailabilityDialogFragment::class.java.simpleName)
                    }
                }
            }
        }
    }

    private fun CreditCardDeliveryBoardingLayoutBinding.init() {
        var creditCardName: String = bindString(R.string.black_credit_card_title)
        val formattedCardDeliveryFee: String = AppConfigSingleton.creditCardDelivery?.formattedCardDeliveryFee
                ?: ""
        when {
            accountBinNumber.equals(Utils.GOLD_CARD, true) -> {
                imgCreditCard?.setImageDrawable(bindDrawable(R.drawable.w_gold_credit_card))
                creditCardName = bindString(R.string.gold_credit_card_title)
                descriptionNoteTextView?.visibility = View.VISIBLE
                descriptionNoteTextView?.text = KotlinUtils.highlightText(bindString(R.string.setup_credit_card_delivery_desc_please_note, bindString(R.string.goldCreditCard_title_small), formattedCardDeliveryFee), mutableListOf("Please note:", "R${formattedCardDeliveryFee}"))
            }
            accountBinNumber.equals(Utils.SILVER_CARD, true) -> {
                imgCreditCard?.setImageDrawable(bindDrawable(R.drawable.w_silver_credit_card))
                creditCardName = bindString(R.string.silver_credit_card_title)

                descriptionNoteTextView?.visibility = View.VISIBLE
                descriptionNoteTextView?.text = KotlinUtils.highlightText(bindString(R.string.setup_credit_card_delivery_desc_please_note, bindString(R.string.silver_credit_card), formattedCardDeliveryFee), mutableListOf("Please note:", "R${formattedCardDeliveryFee}"))
            }
            accountBinNumber.equals(Utils.BLACK_CARD, true) -> {
                imgCreditCard?.setImageDrawable(bindDrawable(R.drawable.w_black_credit_card))
                creditCardName = bindString(R.string.black_credit_card_title)
            }
        }
        titleTextView?.text = bindString(R.string.setup_credit_card_delivery_title, creditCardName)
    }

    private fun setupToolbar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle(bindString(R.string.my_card))
                changeToolbarBackground(R.color.grey_bg)
            }
        }
    }
}
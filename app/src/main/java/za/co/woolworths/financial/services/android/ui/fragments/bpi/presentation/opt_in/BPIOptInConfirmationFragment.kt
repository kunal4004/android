package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_opt_in_confirmation_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.util.Utils
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod

import android.text.style.ClickableSpan
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.util.HashMap

import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel

class BPIOptInConfirmationFragment : Fragment() {

    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_opt_in_confirmation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickableDescriptionText()
        listener()
    }

    private fun setClickableDescriptionText() {
        val clickableSpanText = SpannableString(bindString(R.string.bpi_confirm_opt_in_desc_call_services))
        clickableSpanText.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Utils.makeCall(bindString(R.string.bpi_confirm_opt_in_desc_call_services))
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = bindColor(R.color.description_color)
                }
            }, 0, clickableSpanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val numberText = bindString(R.string.bpi_confirm_opt_in_desc_call_services)
        val descriptionText = SpannableString(bindString(R.string.bpi_confirm_opt_in_desc2))
        descriptionText.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Utils.makeCall(numberText)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = bindColor(R.color.description_color)
                }
            }, descriptionText.indexOf(numberText), descriptionText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        optInDescription2TextView?.apply {
            text = descriptionText
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun listener() {
        confirmOptInButton?.setOnClickListener {

            arguments?.getString(BalanceProtectionInsuranceActivity.BPI_PRODUCT_GROUP_CODE)
                .let { productGroupCode ->
                    var bpiTaggingEventCode: String? = null
                    val arguments: MutableMap<String, String> = HashMap()

                    when (productGroupCode) {
                        AccountsProductGroupCode.CREDIT_CARD.groupCode -> {
                            bpiTaggingEventCode =
                                FirebaseManagerAnalyticsProperties.CC_BPI_OPT_IN_CONFIRM
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                                FirebaseManagerAnalyticsProperties.PropertyValues.CC_BPI_OPT_IN_CONFIRM_VALUE
                        }
                        AccountsProductGroupCode.STORE_CARD.groupCode -> {
                            bpiTaggingEventCode =
                                FirebaseManagerAnalyticsProperties.SC_BPI_OPT_IN_CONFIRM
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                                FirebaseManagerAnalyticsProperties.PropertyValues.SC_BPI_OPT_IN_CONFIRM_VALUE
                        }
                        AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> {
                            bpiTaggingEventCode =
                                FirebaseManagerAnalyticsProperties.PL_BPI_OPT_IN_CONFIRM
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                                FirebaseManagerAnalyticsProperties.PropertyValues.PL_BPI_OPT_IN_CONFIRM_VALUE
                        }
                    }

                    bpiTaggingEventCode?.let { Utils.triggerFireBaseEvents(it, arguments, activity) }
                }

            val bundle = Bundle()
            val productOfferingId = bpiViewModel?.mAccount?.productOfferingId?.toString() ?: ""
            bundle.putString("otpMethodType", OTPMethodType.SMS.name)
            bundle.putString("productOfferingId", productOfferingId)
            view?.findNavController()?.navigate(R.id.action_BPIOptInConfirmationFragment_to_sendOtpFragment, bundleOf("bundle" to bundle))
        }
    }
}
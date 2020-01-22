package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.link_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.CreditCardTextWatcher
import android.view.inputmethod.EditorInfo
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Parcelable
import android.text.Editable
import cards.pay.paycardsrecognizer.sdk.Card
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class ICREnterCardNumberFragment : MyCardExtension() {

    private var shouldDisableUINavigation = false
    private val mMCSInstantStoreCard = WoolworthsApplication.getInstantCardReplacement()

    companion object {
        const val REQUEST_CODE_SCAN_CARD = 1
        fun newInstance() = ICREnterCardNumberFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.link_card_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputTextWatcher()
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_LINK_START)
        navigateToEnterOTPFragmentImageView?.setOnClickListener {
            if (shouldDisableUINavigation) return@setOnClickListener
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_LINK_CARD)
            navigateToOTPScreen()
        }

        navigateToEnterOTPFragmentImageView?.isEnabled = false

        uniqueIdsForEnterCartNumberScreen()
    }

    private fun uniqueIdsForEnterCartNumberScreen() {
        activity?.resources?.apply {
            tvLinkNewCardTitle?.contentDescription = getString(R.string.label_linkICR)
            tvLinkNewCardDesc?.contentDescription = getString(R.string.label_linkICRCardDescription)
            cardNumberEditText?.contentDescription = getString(R.string.text_carddetails)
            navigateToEnterOTPFragmentImageView?.contentDescription = getString(R.string.button_next)
            invalidCardNumberLabel?.contentDescription = getString(R.string.invalid_card_number)
        }
    }

    private fun navigateToOTPScreen() {
        if (shouldDisableUINavigation) return
        if (navigateToEnterOTPFragmentImageView?.isEnabled == true) {
            if (NetworkManager().isConnectedToNetwork(activity)) {
                (activity as? InstantStoreCardReplacementActivity)?.setOTPType(OTPMethodType.SMS)
                replaceFragment(
                        fragment = EnterOtpFragment.newInstance(),
                        tag = EnterOtpFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right)
            } else {
                activity?.let { activity -> ErrorHandlerView(activity).showToast() }
            }
        }
    }

    private fun inputTextWatcher() = cardNumberEditText?.apply {
        addTextChangedListener(object : CreditCardTextWatcher(this) {
            override fun afterTextChanged(s: Editable) {
                super.afterTextChanged(s)
                cardNumberEditText?.text?.toString()?.replace(" ", "")?.let { cardNumber -> setupCardNumberField(cardNumber) }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                validCardNumberUI()
            }
        })

        cardNumberEditText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                navigateToOTPScreen()
            }
            false
        }
    }

    private fun setupCardNumberField(cardNumber: String) {
        if (cardNumber.length == 16) {
            val validStoreCardBinsArray = mMCSInstantStoreCard.validStoreCardBins
            val storeCard6DigitBinNumber = cardNumber.substring(0, 6).toInt()
            if (Utils.isValidLuhnNumber(cardNumber) && validStoreCardBinsArray.contains(storeCard6DigitBinNumber)) {
                (activity as? InstantStoreCardReplacementActivity)?.setCardNumber(cardNumber)
                validCardNumberUI()
            } else {
                invalidCardNumberUI()
            }
        } else {
            navigateToEnterOTPFragmentImageView?.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            cardNumberEditText?.apply {
                isFocusable = true
                requestFocus()
                showSoftKeyboard(it, this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN_CARD) {
            when (resultCode) {
                RESULT_OK -> (data?.getParcelableExtra<Parcelable>(ScanCardIntent.RESULT_PAYCARDS_CARD) as? Card)?.cardNumber?.let { cardNumber -> cardNumberEditText?.setText(cardNumber) }
                else -> return
            }
        }
    }
}
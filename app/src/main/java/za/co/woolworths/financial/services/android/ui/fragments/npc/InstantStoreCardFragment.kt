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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.util.Utils

class InstantStoreCardFragment : MyCardExtension() {
    private var shouldDisableUINavigation = false
    private var shouldClearCardNumber = false

    companion object {
        const val REQUEST_CODE_SCAN_CARD = 1
        fun newInstance() = InstantStoreCardFragment()
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
    }

    private fun navigateToOTPScreen() {
        if (shouldDisableUINavigation) return
        if (navigateToEnterOTPFragmentImageView?.isEnabled == true) {
            (activity as? InstantStoreCardReplacementActivity)?.setOTPType(OTPMethodType.SMS)
            replaceFragment(
                    fragment = EnterOtpFragment.newInstance(),
                    tag = EnterOtpFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard,
                    allowStateLoss = true,
                    enterAnimation = R.anim.slide_in_from_right,
                    exitAnimation = R.anim.slide_to_left,
                    popEnterAnimation = R.anim.slide_from_left,
                    popExitAnimation = R.anim.slide_to_right
            )
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

        cardNumberEditText?.setOnFocusChangeListener { v, hasFocus -> cardNumberEditText?.isCursorVisible = hasFocus }

        cardNumberEditText?.setOnTouchListener { v, event ->
            if (shouldClearCardNumber) {
                cardNumberEditText?.text?.clear()
                shouldClearCardNumber = false
            }

            false
        }
    }

    private fun setupCardNumberField(cardNumber: String) {
        if (cardNumber.length == 16) {
            if (Utils.isValidLuhnNumber(cardNumber)) {
                (activity as? InstantStoreCardReplacementActivity)?.setCardNumber(cardNumber)
                shouldClearCardNumber = false
                validCardNumberUI()
            } else {
                shouldClearCardNumber = true
                invalidCardNumberUI()
            }
            hideKeyboard()
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
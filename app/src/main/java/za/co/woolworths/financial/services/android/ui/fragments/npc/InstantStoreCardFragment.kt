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
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Parcelable
import cards.pay.paycardsrecognizer.sdk.Card
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.views.ProgressBarDialog

class InstantStoreCardFragment : MyCardExtension() {

    private var shouldDisableUINavigation = false

    private var progressBarDialog: ProgressBarDialog? = ProgressBarDialog()

    companion object {
        const val REQUEST_CODE_SCAN_CARD = 1
        fun newInstance() = InstantStoreCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.link_card_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputTextWatcher()
        tappedEvent()
    }

    private fun tappedEvent() {
        navigateToEnterOTPFragmentImageView?.setOnClickListener {
            if (shouldDisableUINavigation) return@setOnClickListener
            val cardNumber = cardNumberEditText?.text?.toString()?.replace(" ", "") ?: ""
            (activity as? InstantStoreCardReplacementActivity)?.setCardNumber(cardNumber)
            navigateToOTPScreen()
        }
    }

    private fun navigateToOTPScreen() {
        if (shouldDisableUINavigation) return
        if (navigateToEnterOTPFragmentImageView?.alpha == 1.0f) {
            (activity as? InstantStoreCardReplacementActivity)?.setOTPType(OTPMethodType.SMS)
            makeOTPCall()
        }
    }

    private fun inputTextWatcher() {
        cardNumberEditText?.apply {
            addTextChangedListener(object : CreditCardTextWatcher(this) {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    super.onTextChanged(s, start, before, count)
                    navigateToOTPScreenValidator()
                }
            })
        }

        cardNumberEditText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                navigateToOTPScreen()
            }
            false
        }

        cardNumberEditText?.setOnFocusChangeListener { v, hasFocus ->
            cardNumberEditText?.isCursorVisible = hasFocus
        }
    }

    private fun navigateToOTPScreenValidator() {
        navigateToEnterOTPFragmentImageView?.alpha = if (cardNumberEditText?.length() == 19) 1.0f else 0.5f
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            cardNumberEditText?.apply {
                requestFocus()
                showSoftKeyboard(it, this)
            }
        }
    }

    private fun makeOTPCall() {
        activity?.let { activity ->
            val requestOTP = StoreCardOTPRequest(activity, OTPMethodType.SMS)
            requestOTP.make(object : IOTPLinkStoreCard<LinkNewCardOTP> {
                override fun startLoading() {
                    super.startLoading()
                    shouldDisableUINavigation = true
                    progressBarDialog?.show(activity)
                    cardNumberEditText?.isFocusable = false
                    cardNumberEditText?.isFocusableInTouchMode = false
                }

                override fun loadComplete() {
                    super.loadComplete()
                    shouldDisableUINavigation = false
                    progressBarDialog?.dismissDialog()
                    cardNumberEditText?.isFocusable = true
                    cardNumberEditText?.isFocusableInTouchMode = true
                }

                override fun onSuccessHandler(response: LinkNewCardOTP) {
                    super.onSuccessHandler(response)
                    val otpSentTo = response.otpSentTo
                    (activity as? InstantStoreCardReplacementActivity)?.mDefaultOtpSentTo = otpSentTo
                    otpSentTo?.let { otp ->
                        replaceFragment(
                                fragment = EnterOtpFragment.newInstance(otp),
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
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN_CARD) {
            when (resultCode) {
                RESULT_OK -> {
                    val cardNumber = (data?.getParcelableExtra<Parcelable>(ScanCardIntent.RESULT_PAYCARDS_CARD) as? Card)?.cardNumber
                            ?: ""
                    cardNumberEditText?.setText(cardNumber)
                }
                else -> return
            }
        }
    }
}
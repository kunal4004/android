package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.graphics.Paint
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
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Parcelable
import android.view.View.GONE
import android.view.View.VISIBLE
import cards.pay.paycardsrecognizer.sdk.Card
import za.co.woolworths.financial.services.android.contracts.IStoreCardOTPCallback
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class LinkCardFragment : MyCardExtension() {

    companion object {
        const val REQUEST_CODE_SCAN_CARD = 1
        fun newInstance() = LinkCardFragment().withArgs {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.link_card_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvReplacementCardInfo?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                replaceFragment(
                        fragment = GetReplacementCardFragment.newInstance(),
                        tag = GetReplacementCardFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                )
            }
        }
        inputTextWatcher()
        tappedEvent()

        pbOTPLoader?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)

    }

    private fun tappedEvent() {
        imNavigateToOTPFragment?.setOnClickListener {
            navigateToOTPScreen()
        }

        imCameraIcon?.setOnClickListener {
            activity?.apply {
                val builder = ScanCardIntent.Builder(this)
                builder.setScanCardHolder(true)
                builder.setSaveCard(false)
                builder.setSoundEnabled(true)
                startActivityForResult(builder.build(), REQUEST_CODE_SCAN_CARD)
            }
        }
    }

    private fun navigateToOTPScreen() {
        if (imNavigateToOTPFragment?.alpha == 1.0f) {
            makeOTPCall()
        }
    }

    private fun inputTextWatcher() {
        etCardNumber?.apply {
            addTextChangedListener(object : CreditCardTextWatcher(this) {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    super.onTextChanged(s, start, before, count)
                    imNavigateToOTPFragment?.alpha = if (etCardNumber?.length() == 19) 1.0f else 0.5f
                }
            })
        }

        etCardNumber?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                navigateToOTPScreen()
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            etCardNumber?.apply {
                requestFocus()
                showSoftKeyboard(it, this)
            }
        }
    }

    private fun makeOTPCall() {
        activity?.let { activity ->
            val requestOTP = OTPRequest(activity, OTPMethodType.EMAIL)
            requestOTP.make(object : IStoreCardOTPCallback<LinkNewCardOTP> {
                override fun loadStart() {
                    super.loadStart()
                    pbOTPLoader?.visibility = VISIBLE
                    imCameraIcon?.isEnabled = false
                    etCardNumber?.isFocusable = false
                    etCardNumber?.isFocusableInTouchMode = false
                }

                override fun loadComplete() {
                    super.loadComplete()
                    pbOTPLoader?.visibility = GONE
                    imCameraIcon?.isEnabled = true
                    etCardNumber?.isFocusable = true
                    etCardNumber?.isFocusableInTouchMode = true
                }

                override fun onSuccess(response: LinkNewCardOTP) {
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
                    etCardNumber?.setText(cardNumber)
                }
                else -> return
            }
        }
    }
}
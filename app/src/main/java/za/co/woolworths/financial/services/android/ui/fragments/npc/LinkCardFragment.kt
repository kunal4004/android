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
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent
import android.app.Activity
import android.content.Intent
import android.util.Log


class LinkCardFragment : MyCardExtension() {

    private val TAG = LinkCardFragment::class.java.simpleName

    companion object {
        const val REQUEST_CODE_SCAN_CARD = 1
        fun newInstance() = LinkCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.link_card_fragment, container, false)
    }

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
    }

    private fun tappedEvent() {
        imNavigateToOTPFragment?.setOnClickListener {
            navigateToOTPScreen()
        }

        imCameraIcon?.setOnClickListener {
            activity?.apply {
                val intent = ScanCardIntent.Builder(this).build()
                startActivityForResult(intent, REQUEST_CODE_SCAN_CARD)
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
        OneAppService.getLinkNewCardOTP(OTPMethodType.SMS).enqueue(
                CompletionHandler(object : RequestListener<LinkNewCardOTP> {
                    override fun onSuccess(linkNewCardOTP: LinkNewCardOTP) {
                        with(linkNewCardOTP) {
                            when (this.httpCode) {
                                200 -> replaceFragment(
                                        fragment = EnterOtpFragment.newInstance(),
                                        tag = EnterOtpFragment::class.java.simpleName,
                                        containerViewId = R.id.flMyCard,
                                        allowStateLoss = true,
                                        enterAnimation = R.anim.slide_in_from_right,
                                        exitAnimation = R.anim.slide_to_left,
                                        popEnterAnimation = R.anim.slide_from_left,
                                        popExitAnimation = R.anim.slide_to_right
                                )

                                440 -> activity?.let { activity ->
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE,
                                            response?.stsParams ?: "", activity)
                                }

                                else -> response?.desc?.let { desc ->
                                    val dialog = ErrorDialogFragment.newInstance(desc)
                                    activity?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {

                    }

                }, LinkNewCardOTP::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN_CARD) {
            when (resultCode) {
                Activity.RESULT_OK -> {
//                    val card = data?.getParcelableExtra<Parcelable>(ScanCardIntent.RESULT_PAYCARDS_CARD)
//                    val cardData = ("Card number: " + card + "\n"
//                            + "Card holder: " + card.getCardHolderName() + "\n"
//                            + "Card expiration date: " + card.getExpirationDate())
                    //     Log.i(TAG, "Card info: $cardData")
                }
                Activity.RESULT_CANCELED -> Log.i(TAG, "Scan canceled")
                else -> Log.i(TAG, "Scan failed")
            }
        }
    }

}
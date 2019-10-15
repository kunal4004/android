package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.get_temp_store_card_popup_fragment.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.util.StoreCardAPIRequest
import za.co.woolworths.financial.services.android.util.Utils

class GetTemporaryStoreCardPopupFragment : Fragment(), View.OnClickListener {
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null

    companion object {
        fun newInstance(storeCardDetail: String?) = GetTemporaryStoreCardPopupFragment().withArgs {
            putString(MyCardDetailActivity.STORE_CARD_DETAIL, storeCardDetail)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "")

            activity?.let {
                Utils.updateStatusBarBackground(it, R.color.grey_bg)
                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.get_temp_store_card_popup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTempStoreCardButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.getTempStoreCardButton -> {
                requestGetOTP()
            }
        }
    }

    private fun requestGetOTP() {
        //showPayWithCardProgressBar(View.VISIBLE)
        StoreCardAPIRequest().getOTP(OTPMethodType.SMS, object : RequestListener<LinkNewCardOTP> {
            override fun onSuccess(response: LinkNewCardOTP?) {
                //showPayWithCardProgressBar(View.GONE)
                when (response?.httpCode) {
                    200 -> {
                        navigateToOTPFragment(response.otpSentTo)
                    }
                    440 -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                // showPayWithCardProgressBar(View.GONE)
            }
        })
    }

    private fun requestLinkCard(otp: String = "") {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RequestOTPActivity.OTP_REQUEST_CODE) {
            val otp = data?.getStringExtra(RequestOTPActivity.OTP_VALUE)
            otp?.let { requestLinkCard(it) }
        }
    }

    private fun navigateToOTPActivity(otpSentTo: String?) {
        otpSentTo?.let { otpSentTo ->
            activity?.apply {
                val intent = Intent(this, RequestOTPActivity::class.java)
                intent.putExtra(RequestOTPActivity.OTP_SENT_TO, otpSentTo)
                startActivityForResult(intent, RequestOTPActivity.OTP_REQUEST_CODE)
            }
        }
    }

    fun navigateToOTPFragment(otpSentTo: String?) {
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
}
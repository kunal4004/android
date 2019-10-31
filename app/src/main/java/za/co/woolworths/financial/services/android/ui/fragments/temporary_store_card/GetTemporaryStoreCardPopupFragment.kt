package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.get_temp_store_card_popup_fragment.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.LinkStoreCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
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
                when (mStoreCardsResponse?.oneTimePinRequired?.linkVirtualStoreCard) {
                    true -> requestGetOTP()
                    else -> navigateToLinkCardFragment()
                }
            }
        }
    }

    private fun requestGetOTP() {
        showTempStoreCardProgressBar(View.VISIBLE)
        StoreCardAPIRequest().getOTP(OTPMethodType.SMS, object : RequestListener<LinkNewCardOTP> {
            override fun onSuccess(response: LinkNewCardOTP?) {
                showTempStoreCardProgressBar(View.GONE)
                when (response?.httpCode) {
                    200 ->navigateToOTPFragment(response.otpSentTo)
                    440 -> activity?.let { activity -> response.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response?.stsParams?: "", activity) } }
                    else -> showErrorDialog(response?.response?.desc ?: getString(R.string.general_error_desc))
                }
            }

            override fun onFailure(error: Throwable?) {
                showTempStoreCardProgressBar(View.GONE)
                showErrorDialog(getString(R.string.general_error_desc))
            }
        })
    }

    private fun navigateToLinkCardFragment() {
        replaceFragment(
                fragment = LinkStoreCardFragment.newInstance(),
                tag = LinkStoreCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    fun navigateToOTPFragment(otpSentTo: String?) {
        otpSentTo?.let { otp ->
            (activity as? GetTemporaryStoreCardPopupActivity)?.mDefaultOtpSentTo = otpSentTo
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

    private fun showTempStoreCardProgressBar(state: Int) {
        activity?.apply {
            getTempStoreCardProgressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            getTempStoreCardProgressBar.visibility = state
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = ErrorDialogFragment.newInstance(errorMessage)
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }
}
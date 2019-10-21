package za.co.woolworths.financial.services.android.ui.fragments.npc


import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ResendOTPLoaderFragment : Fragment(), IProgressAnimationState {

    private var mOtpMethodType: OTPMethodType? = null
    private var storeCardCallback: IOTPLinkStoreCard<LinkNewCardOTP>? = null

    companion object {
        private const val OTP_METHOD_TYPE = "OTP_METHOD_TYPE"
        fun newInstance(otpMethodType: OTPMethodType, linkCard: IOTPLinkStoreCard<LinkNewCardOTP>?) = ResendOTPLoaderFragment().apply {
            storeCardCallback = linkCard
            withArgs {
                putSerializable(OTP_METHOD_TYPE, otpMethodType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? MyCardActivityExtension)?.showBackIcon()
        arguments?.apply {
            mOtpMethodType = getSerializable(OTP_METHOD_TYPE) as? OTPMethodType
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.resend_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator)

        activity?.let { activity ->
            val requestOTP = mOtpMethodType?.let { type -> StoreCardOTPRequest(activity, type) }
            requestOTP?.make(object : IOTPLinkStoreCard<LinkNewCardOTP> {

                override fun onSuccessHandler(response: LinkNewCardOTP) {
                    super.onSuccessHandler(response)
                    if (!isAdded) return
                    storeCardCallback?.navigateToEnterOTPScreen(response)
                    (activity as? AppCompatActivity)?.onBackPressed()
                }

                override fun onFailureHandler() {
                    super.onFailureHandler()
                    if (!isAdded) return
                    (activity as? AppCompatActivity)?.onBackPressed()
                }

            })
        }
    }

    override fun onAnimationEnd(cardIsBlocked: Boolean) {}

    override fun onDestroy() {
        super.onDestroy()
        activity?.supportFragmentManager?.apply {
            findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as? MyCardActivityExtension)?.hideBackIcon()
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
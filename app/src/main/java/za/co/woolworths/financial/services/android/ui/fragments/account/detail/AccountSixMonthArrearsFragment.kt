package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_six_month_arrears_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOffering
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class AccountSixMonthArrearsFragment : Fragment() {

    private var mProductOffering: ProductOffering? = null
    private var mApplyNowAccountKeyPair: Pair<Int, Int>? = null
    private val myAccountsRemoteApiViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE,"")
        mApplyNowAccountKeyPair = Gson().fromJson(account, object : TypeToken<Pair<Int, Int>>() {}.type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_six_month_arrears_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mProductOffering = myAccountsRemoteApiViewModel.getProductOffering()
        hideCardTextViews()
        setTitleAndCardTypeAndButton()
        callTheCallCenterButton?.setOnClickListener { Utils.makeCall("0861502020") }
        callTheCallCenterUnderlinedButton?.setOnClickListener { Utils.makeCall("0861502020") }
        viewTreatmentPlansButton?.setOnClickListener {
            activity?.apply {
                val arguments = HashMap<String, String>()
                arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD_ACTION
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD,
                    arguments,
                    this)
                when (AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.renderMode){
                    AvailableFundFragment.NATIVE_BROWSER ->
                        KotlinUtils.openUrlInPhoneBrowser(
                            AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.creditCard?.collectionsUrl, this)

                    else ->
                        KotlinUtils.openLinkInInternalWebView(this,
                            AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.creditCard?.collectionsUrl,
                            true,
                            AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.creditCard?.exitUrl)
                }
            }
        }
        navigateBackImageButton?.setOnClickListener { activity?.onBackPressed() }

        cardDetailImageShimmerFrameLayout?.setShimmer(null)
        myCardTextViewShimmerFrameLayout?.setShimmer(null)
        tempFreezeTextViewShimmerFrameLayout?.setShimmer(null)
        bottomView?.visibility = INVISIBLE
    }

    private fun hideCardTextViews() {
        context?.let { color -> ContextCompat.getColor(color, R.color.white) }?.let { color -> includeAccountDetailHeaderView?.setBackgroundColor(color) }
        myCardTextView?.visibility = GONE
        myCardDetailTextView?.visibility = GONE
        userNameTextView?.visibility = GONE
        imLogoIncreaseLimit?.visibility = GONE
        manageMyCardTextView?.visibility = GONE
        manageMyCardImageView?.visibility = GONE
        manageCardDivider?.background = null
        includeManageMyCard?.layoutParams?.apply {
            height = 0
        }
    }

    private fun setTitleAndCardTypeAndButton() {
        mApplyNowAccountKeyPair?.first?.let { resourceId -> cardDetailImageView?.setImageResource(resourceId) }
        mApplyNowAccountKeyPair?.second?.let { resourceId ->
            toolbarTitleTextView?.text = bindString(resourceId)

            if(mProductOffering?.isViewTreatmentPlanSupported() == true){

                arrearsDescTextView?.text = bindString(R.string.account_arrears_cc_description)
                callTheCallCenterButton?.visibility = GONE
                viewTreatmentPlansButton?.visibility = VISIBLE
                callTheCallCenterUnderlinedButton?.apply {
                    paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    visibility = VISIBLE
                }
            }
            else{
                arrearsDescTextView?.text = activity?.resources?.getString(R.string.account_arrears_description)
                callTheCallCenterButton?.visibility = VISIBLE
                viewTreatmentPlansButton?.visibility = GONE
                callTheCallCenterUnderlinedButton?.visibility = GONE
            }
        }
    }
}
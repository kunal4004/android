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
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_six_month_arrears_fragment.*
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils

class AccountSixMonthArrearsFragment : Fragment() {

    private var mApplyNowAccountKeyPair: Pair<Int, Int>? = null
    private var isViewTreatmentPlanSupported: Boolean = false

    companion object {
        const val IS_VIEW_TREATMENT_PLAN = "IS_VIEW_TREATMENT_PLAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE,"")
        isViewTreatmentPlanSupported = arguments?.getBoolean(IS_VIEW_TREATMENT_PLAN,false) ?: false
        mApplyNowAccountKeyPair = Gson().fromJson(account, object : TypeToken<Pair<Int, Int>>() {}.type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_six_month_arrears_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideCardTextViews()
        setTitleAndCardTypeAndButton()

        callTheCallCenterButton?.setOnClickListener { Utils.makeCall("0861502020") }
        callTheCallCenterUnderlinedButton?.setOnClickListener { Utils.makeCall("0861502020") }
        viewTreatmentPlansButton?.setOnClickListener {
            val outSystemBuilder = OutSystemBuilder(activity, ProductGroupCode.CC)
            outSystemBuilder.build()
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
            if(isViewTreatmentPlanSupported && (resourceId == R.string.black_credit_card_title ||
                        resourceId == R.string.gold_credit_card_title ||
                        resourceId == R.string.silver_credit_card_title)){
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
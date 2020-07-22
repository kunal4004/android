package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_six_month_arrears_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.util.Utils

class AccountSixMonthArrearsFragment : Fragment() {

    private var mApplyNowAccountKeyPair: Pair<Int, Int>? = null

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
        hideCardTextViews()
        setTitleAndCardType()
        callTheCallCenterButton?.setOnClickListener { Utils.makeCall("0861502020") }
        navigateBackImageButton?.setOnClickListener { activity?.onBackPressed() }
    }

    private fun hideCardTextViews() {
        context?.let { color -> ContextCompat.getColor(color, R.color.white) }?.let { color -> includeAccountDetailHeaderView?.setBackgroundColor(color) }
        myCardTextView?.visibility = GONE
        myCardDetailTextView?.visibility = GONE
        userNameTextView?.visibility = GONE
    }

    private fun setTitleAndCardType() {
        mApplyNowAccountKeyPair?.first?.let { resourceId -> cardDetailImageView?.setImageResource(resourceId) }
        toolbarTitleTextView?.text = mApplyNowAccountKeyPair?.second?.let { resourceId -> activity?.resources?.getString(resourceId) }
    }
}
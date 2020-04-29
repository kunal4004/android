package za.co.woolworths.financial.services.android.ui.fragments.faq

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.faq_fragment.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.ISelectQuestionListener
import za.co.woolworths.financial.services.android.models.dto.FAQ
import za.co.woolworths.financial.services.android.models.dto.FAQDetail
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.getFAQ
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.FAQAdapter
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class FAQFragment : Fragment(), ISelectQuestionListener {

    var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    private var mFAQRequest: Call<FAQ>? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mFAQAdapter: FAQAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.faq_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        mErrorHandlerView = ErrorHandlerView(activity, no_connection_layout)

        executeFAQRequest()
        btnRetry?.setOnClickListener {
            if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                executeFAQRequest()
            }
        }
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            setTitle(getString(R.string.drawer_faq))
            showToolbar()
        }

        (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.drawer_faq))

    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.FAQ_LIST)
    }


    private fun executeFAQRequest() {
        mErrorHandlerView?.hideErrorHandler()
        progressCreditLimit?.visibility = View.VISIBLE
        mFAQRequest = faqRequest()
    }

    fun faqSuccessResponse(list: List<FAQDetail>) {
        if (list.isNotEmpty()) {
            mFAQAdapter = FAQAdapter(list, this)
            val mLayoutManager = LinearLayoutManager(activity)
            mLayoutManager.orientation = LinearLayoutManager.VERTICAL
            faqList?.layoutManager = mLayoutManager
            faqList?.isNestedScrollingEnabled = false
            faqList?.adapter = mFAQAdapter
            textNotFound?.visibility = View.GONE
            faqList?.visibility = View.VISIBLE
        } else {
            textNotFound?.visibility = View.VISIBLE
            faqList?.visibility = View.GONE
        }
        progressCreditLimit?.visibility = View.GONE
    }

    fun unhandledResponseCode(response: Response) {
        progressCreditLimit?.visibility = View.GONE
        response.desc?.let { Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, it) }

    }

    fun failureResponseHandler(errorMessage: String) {
        activity?.runOnUiThread { mErrorHandlerView?.networkFailureHandler(errorMessage) }
        progressCreditLimit?.visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        cancelRetrofitRequest(mFAQRequest)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupToolbar()
            mFAQAdapter?.resetIndex()
        }
    }

    override fun onQuestionSelected(faqDetail: FAQDetail?) {
        val bundle = Bundle()
        bundle.putString("question", faqDetail?.question)
        bundle.putString("answer", faqDetail?.answer)
        val faqDetailFragment =
                FAQDetailFragment()
        faqDetailFragment.arguments = bundle
        mBottomNavigator?.pushFragment(faqDetailFragment)
        (activity as? MyAccountActivity)?.replaceFragment(faqDetailFragment)
    }

    private fun faqRequest(): Call<FAQ>? {
        val faqCall = getFAQ()
        faqCall.enqueue(CompletionHandler(object : IResponseListener<FAQ> {
            override fun onSuccess(faq: FAQ) {
                when (faq.httpCode) {
                    200 -> {
                        val faqList = faq.faqs
                        if (faqList != null) {
                            faqSuccessResponse(faqList)
                        }
                    }
                    else -> if (faq.response != null) {
                        unhandledResponseCode(faq.response)
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                error.message?.let { failureResponseHandler(it) }
            }
        }, FAQ::class.java))
        return faqCall
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.cancel_order_failure_layout.*
import kotlinx.android.synthetic.main.cancel_order_progress_fragment.*
import kotlinx.android.synthetic.main.npc_processing_request_layout.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager

class CancelOrderProgressFragment : Fragment(), IProgressAnimationState, View.OnClickListener {

    private var orderId: String? = null
    private var commarceItemList: ArrayList<CommerceItem>? = null
    private var closeButton:View? = null
    private var orderItemTotal: Double? = 0.0
    private var orderShippingTotal: Double? = 0.0

    companion object {
        const val ORDER_ID = "ORDER_ID"
        const val REQUEST_CODE_CANCEL_ORDER = 1976
        const val RESULT_CODE_CANCEL_ORDER_SUCCESS = 1970

        fun getInstance(orderId: String, orderItemList: ArrayList<CommerceItem>?, orderItemTotal: Double, orderShippingTotal: Double) = CancelOrderProgressFragment().withArgs {
            putString(ORDER_ID, orderId)
            putSerializable(AppConstant.ORDER_ITEM_LIST, orderItemList)
            putSerializable(AppConstant.ORDER_ITEM_TOTAL, orderItemTotal)
            putSerializable(AppConstant.ORDER_SHIPPING_TOTAL, orderShippingTotal)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cancel_order_progress_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            orderId = getString(ORDER_ID, "")
            commarceItemList = getSerializable(AppConstant.ORDER_ITEM_LIST) as ArrayList<CommerceItem>?
            orderItemTotal = getDouble(AppConstant.ORDER_ITEM_TOTAL, 0.0)
            orderShippingTotal = getDouble(AppConstant.ORDER_SHIPPING_TOTAL, 0.0)
        }
        closeButton = activity?.findViewById(R.id.btnClose)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderId?.let { requestCancelOrder(it) }
        retry?.setOnClickListener(this)
        callTheCallCenter?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@CancelOrderProgressFragment)
        }

        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        closeButton?.setOnClickListener {
            (activity as? CancelOrderProgressActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CLOSE_FAILURE_CANCEL)
            activity?.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.retry -> orderId?.let { retryCancelOrder() }
            R.id.callTheCallCenter -> activity?.apply {
                (activity as? CancelOrderProgressActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_FAILURE_CALL_CENTRE)
                Utils.makeCall("0861 50 20 20") }
        }
    }

    private fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    private fun retryCancelOrder() {
        closeButton?.visibility = View.GONE
        (activity as? CancelOrderProgressActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_FAILURE_RETRY)
        getProgressState()?.restartSpinning()
        orderId?.let { requestCancelOrder(it) }
    }

    private fun requestCancelOrder(orderId: String) {
        cancelOrderFailureView?.visibility = View.GONE
        cancelOrderProcessingLayout?.visibility = View.VISIBLE

        val orderDetailRequest = OneAppService.queryServiceCancelOrder(orderId)
        orderDetailRequest.enqueue(CompletionHandler(object : IResponseListener<CancelOrderResponse> {
            override fun onSuccess(cancelOrderResponse: CancelOrderResponse?) {
                cancelOrderResponse?.apply {
                    when (httpCode) {
                        200 -> onCancelOrderSuccess()
                        else -> onCancelOrderFailure()
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                onCancelOrderFailure()
            }

        }, CancelOrderResponse::class.java))

    }

    fun onCancelOrderSuccess() {
        setEventForCancelOrderForRefund()
        getProgressState()?.animateSuccessEnd(true)
        processRequestDescriptionTextView.visibility = View.GONE
        processRequestTitleTextView?.text = bindString(R.string.cancel_order_success_title)
        (activity as? CancelOrderProgressActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_API_SUCCESS)
    }

    private fun setEventForCancelOrderForRefund() {

        if(commarceItemList?.isNullOrEmpty() == true) {
            return
        }

        val cancelOrderParams = Bundle()

        cancelOrderParams.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )

        cancelOrderParams.putString(
            FirebaseAnalytics.Param.TRANSACTION_ID,
            orderId
        )


        orderItemTotal?.let {
            cancelOrderParams.putDouble(
                FirebaseAnalytics.Param.VALUE,
                it
            )
        }

        orderShippingTotal?.let {
            cancelOrderParams.putDouble(
                FirebaseAnalytics.Param.SHIPPING,
                it
            )
        }

        cancelOrderParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.REFUND_TYPE,
            FirebaseManagerAnalyticsProperties.PropertyValues.DASH_CANCELLED_ORDER
        )

        cancelOrderParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.AFFILIATION,
            FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE
        )

        for ( commarceItem in commarceItemList!!) {
            val cancelOrderItem = Bundle()
            cancelOrderItem.putString(FirebaseAnalytics.Param.ITEM_ID,
                commarceItem.commerceItemInfo.productId)

            cancelOrderItem.putString(FirebaseAnalytics.Param.ITEM_NAME,
                commarceItem.commerceItemInfo.productDisplayName)

            cancelOrderItem.putDouble(FirebaseAnalytics.Param.PRICE,
                commarceItem.priceInfo.amount)

            cancelOrderItem.putInt(FirebaseAnalytics.Param.QUANTITY,
                commarceItem.commerceItemInfo.quantity)

            cancelOrderParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(cancelOrderItem))
        }

        AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.REFUND, cancelOrderParams)
    }


    fun onCancelOrderFailure() {
        closeButton?.visibility = View.VISIBLE
        getProgressState()?.animateSuccessEnd(false)
        cancelOrderProcessingLayout?.visibility = View.GONE
        cancelOrderFailureView?.visibility = View.VISIBLE
        (activity as? CancelOrderProgressActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_API_FAILURE)
    }

    override fun onAnimationEnd(cardIsBlocked: Boolean) {
        if (cardIsBlocked) {
            Handler().postDelayed({
                activity?.apply {
                    setResult(RESULT_CODE_CANCEL_ORDER_SUCCESS)
                    finish()
                }
            }, 2000)
        }
    }
}
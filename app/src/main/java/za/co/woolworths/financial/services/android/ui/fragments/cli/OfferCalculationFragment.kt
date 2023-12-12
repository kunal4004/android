package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OfferCalculationFragmentBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigMaritalStatus
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.models.service.event.LoadState
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.fragments.cli.CreditLimitDecreaseConfirmationFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.controller.CLIFragment
import za.co.woolworths.financial.services.android.util.controller.EventStatus
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class OfferCalculationFragment : CLIFragment(R.layout.offer_calculation_fragment), View.OnClickListener, NetworkChangeListener {

    private var binding: OfferCalculationFragmentBinding? = null
    private var declineOfferInterface: DeclineOfferInterface? = null
    private var mHashIncomeDetail: HashMap<String, String>? = null
    private var mHashExpenseDetail: HashMap<String, String>? = null
    private var maritalStatus: ConfigMaritalStatus? = null
    private var mCurrentCredit = 0
    private var mObjOffer: OfferActive? = null
    private var mCliPhase2Activity: CLIPhase2Activity? = null
    private var mConnectionBroadcast: BroadcastReceiver? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var editorWasShown = false
    private var woolworthsApplication: WoolworthsApplication? = null
    private var mAlreadyLoaded = false
    private var mCLiId : Int? = 0
    private var mGlobalState: WGlobalState? = null
    private var mEventStatus: EventStatus? = null
    private var loadState: LoadState? = null
    private val disposables = CompositeDisposable()
    private var mCreditRequestMax = 0
    private var currentCredit = 0
    private var mNewCLIAmount = 0
    private var cliUpdateApplication: Call<OfferActive>? = null
    private var createOfferTask: Call<OfferActive>? = null
    private var cliOfferDecision: Call<OfferActive>? = null
    private var cliAcceptOfferDecision: Call<OfferActive>? = null

    private enum class LATEST_BACKGROUND_CALL {
        CREATE_OFFER, DECLINE_OFFER, UPDATE_APPLICATION, ACCEPT_OFFER
    }

    private var latest_background_call: LATEST_BACKGROUND_CALL? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = OfferCalculationFragmentBinding.bind(view)
        latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER)

        if (savedInstanceState == null && !mAlreadyLoaded) {
            val activity: Activity? = activity
            if (activity != null) maritalStatus = (activity as CLIPhase2Activity).getMaritalStatus()
            mAlreadyLoaded = true
            loadState = LoadState()
            try {
                declineOfferInterface = getActivity() as DeclineOfferInterface?
            } catch (ignored: IllegalArgumentException) {
            }
            mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this)
            woolworthsApplication = getActivity()?.application as? WoolworthsApplication
            mGlobalState = woolworthsApplication?.wGlobalState
            init()
            seekBar()
            val bundle = this.arguments
            if (!editorWasShown) {
                onLoad()
                var incomeDetail: Serializable? = null
                var expenseDetail: Serializable? = null
                if (bundle != null) {
                    incomeDetail = bundle.getSerializable(IncreaseLimitController.INCOME_DETAILS)
                    expenseDetail = bundle.getSerializable(IncreaseLimitController.EXPENSE_DETAILS)
                }
                binding?.tvSlideToEditSeekInfo?.let { setInvisibleView(it) }
                mCliStepIndicatorListener?.onStepSelected(3)
                mObjOffer = (activity as? CLIPhase2Activity?)?.offerActiveObject()
                mCLiId = mObjOffer?.cliId
                if (incomeDetail != null) {
                    mHashIncomeDetail = incomeDetail as HashMap<String, String>?
                }
                if (expenseDetail != null) {
                    mHashExpenseDetail = expenseDetail as HashMap<String, String>?
                    if (activity is CLIPhase2Activity) {
                        mEventStatus = activity.eventStatus
                        if (mEventStatus == null) {
                            mEventStatus = EventStatus.NONE
                        }
                        cliApplicationRequest(mEventStatus)
                    }
                }
            }
            woolworthsApplication?.let {
                it.bus()
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { `object` ->
                        if (`object` is CLIOfferDecision) {
                            mGlobalState?.declineDecision?.let {
                                cliDelcineOfferRequest(it)
                            }
                        } else if (`object` is BusStation) {
                            if (!TextUtils.isEmpty(`object`.string) && `object`.string.equals(
                                    getString(
                                        R.string.decline
                                    ), ignoreCase = true
                                )
                            ) {
                                finishActivity()
                            } else if (`object`.number != null) {
                                binding?.sbSlideAmount?.post {
                                    mNewCLIAmount = `object`.number
                                    var drawnDownAmount = `object`.drawnDownAmount
                                    drawnDownAmount -= drawnDownAmount % 100
                                    binding?.sbSlideAmount?.progress = mNewCLIAmount
                                    // Parse minimum credit amount if amount received is of negative type
                                    if (mNewCLIAmount <= 0 || drawnDownAmount < currentCredit) {
                                        var mCurrentCredit = currentCredit
                                        mCurrentCredit -= mCurrentCredit % 100
                                        openCreditLimitDecreaseFragmentDialog(mCurrentCredit)
                                    } else {
                                        if (drawnDownAmount in ((currentCredit + 1) until mCreditRequestMax)) {
                                            openCreditLimitDecreaseFragmentDialog(drawnDownAmount)
                                        }
                                    }
                                }
                            }
                        } else if (`object` is CustomPopUpWindow) {
                            mCliPhase2Activity?.performClicked()
                        }
                    }?.let { disposables.add(it) }
            }
        }
    }

    private fun seekBar() {
        binding?.apply {
            sbSlideAmount.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (mCreditRequestMax > mCurrentCredit) {
                        openCreditLimitDecreaseFragmentDialog(mNewCLIAmount)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    if (mCreditRequestMax < mCurrentCredit) {
                        val amount = formatAmount(mCreditRequestMax)
                        tvSlideToEditAmount.setText(amount)
                        tvCurrentCreditLimitAmount.setText(formatAmount(currentCredit - INCREASE_PROGRESS_BY))
                        tvNewCreditLimitAmount.setText(amount)
                        mGlobalState?.creditLimit = amount
                        tvAdditionalCreditLimitAmount.setText(
                            additionalAmountSignSum(
                                calculateAdditionalAmount(
                                    mCurrentCredit,
                                    tvNewCreditLimitAmount.text.toString()
                                )
                            )
                        )
                    }
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    var progress = progress
                    if (mCreditRequestMax <= mCurrentCredit) {
                        seekBar.progress = 0
                    } else {
                        binding?.apply {
                            progress /= INCREASE_PROGRESS_BY
                            progress *= INCREASE_PROGRESS_BY
                            mNewCLIAmount = mCurrentCredit + progress
                            tvSlideToEditAmount.setText(formatAmount(mNewCLIAmount))
                            val amount = tvSlideToEditAmount.text.toString()
                            tvNewCreditLimitAmount.setText(amount)
                            mGlobalState?.creditLimit = amount
                            tvAdditionalCreditLimitAmount.setText(
                                additionalAmountSignSum(
                                    calculateAdditionalAmount(
                                        currentCredit,
                                        tvNewCreditLimitAmount.text.toString()
                                    )
                                )
                            )
                        }
                    }
                }
            })
        }
    }

    private fun cliCreateApplication(createOfferRequest: CreateOfferRequest) {
        onLoad()
        showView(binding?.includeCliNextButton?.llNextButtonLayout)
        createOfferTask = OneAppService().cliCreateApplication(createOfferRequest)
        createOfferTask?.enqueue(
            CompletionHandler(
                object : IResponseListener<OfferActive> {
                    override fun onSuccess(offerActive: OfferActive?) {
                        when (offerActive?.httpCode) {
                            200 -> {
                                enableDeclineButton()
                                displayApplication(offerActive)
                            }
                            440 -> SessionUtilities.getInstance().setSessionState(
                                SessionDao.SESSION_STATE.INACTIVE,
                                offerActive.response.stsParams,
                                activity
                            )
                            else -> displayMessageError(offerActive)
                        }
                        loadSuccess()
                        onLoadComplete()
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        if (activity != null && error != null) {
                            activity.runOnUiThread(Runnable {
                                latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER)
                                hideView(binding?.includeCliNextButton?.llNextButtonLayout)
                                loadFailure()
                                hideDeclineButton()
                                mErrorHandlerView?.responseError(view, error.message)
                            })
                        }
                    }
                }, OfferActive::class.java
            )
        )
    }

    private fun cliUpdateApplication(createOfferRequest: CreateOfferRequest, cliId: String) {
        onLoad()
        showView(binding?.includeCliNextButton?.llNextButtonLayout)
        cliUpdateApplication = OneAppService().cliUpdateApplication(createOfferRequest, cliId)
        cliUpdateApplication?.enqueue(
            CompletionHandler(
                object : IResponseListener<OfferActive> {
                    override fun onSuccess(offerActive: OfferActive?) {
                        mObjOffer = offerActive
                        when (mObjOffer?.httpCode) {
                            200 -> {
                                enableDeclineButton()
                                displayApplication(mObjOffer)
                            }
                            440 -> SessionUtilities.getInstance().setSessionState(
                                SessionDao.SESSION_STATE.INACTIVE,
                                mObjOffer?.response?.stsParams,
                                activity
                            )
                            else -> displayMessageError(mObjOffer)
                        }
                        onLoadComplete()
                        loadSuccess()
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        if (activity != null && error != null) {
                            activity.runOnUiThread(Runnable {
                                latestBackgroundTask(LATEST_BACKGROUND_CALL.UPDATE_APPLICATION)
                                loadFailure()
                                hideView(binding?.includeCliNextButton?.llNextButtonLayout)
                                hideDeclineButton()
                                mErrorHandlerView?.responseError(view, error.message)
                            })
                        }
                    }
                }, OfferActive::class.java
            )
        )
    }

    private fun cliDelcineOfferRequest(createOfferDecision: CLIOfferDecision) {
        declineOfferInterface?.onLoad()
        cliOfferDecision = OneAppService().createOfferDecision(createOfferDecision, mObjOffer?.cliId.toString())
        cliOfferDecision?.enqueue(
            CompletionHandler(
                object : IResponseListener<OfferActive> {
                    override fun onSuccess(response: OfferActive?) {
                        when (mObjOffer?.httpCode) {
                            200 -> finishActivity()
                            440 -> SessionUtilities.getInstance().setSessionState(
                                SessionDao.SESSION_STATE.INACTIVE,
                                mObjOffer?.response?.stsParams,
                                activity
                            )
                            else -> mObjOffer?.response?.let { showErrorMessage(activity, it) }
                        }
                        loadSuccess()
                        declineOfferInterface?.onLoadComplete()
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        activity?.runOnUiThread {
                            latestBackgroundTask(LATEST_BACKGROUND_CALL.DECLINE_OFFER)
                            loadFailure()
                            declineOfferInterface?.onLoadComplete()
                        }
                    }
                }, OfferActive::class.java
            )
        )
    }

    private fun init() {
        binding?.apply {
            mCliPhase2Activity = activity as CLIPhase2Activity?
            mErrorHandlerView = ErrorHandlerView(activity, includeNoConnectionLayout.noConnectionLayout, this@OfferCalculationFragment)
            mErrorHandlerView?.setMargin(includeNoConnectionLayout.noConnectionLayout, 0, 0, 0, 0)
            val increaseLimitController = IncreaseLimitController(activity)
            increaseLimitController.setQuarterHeight(llEmptyLayout)
            showView(includeCliNextButton.llNextButtonLayout)
            disableView(includeCliNextButton.llNextButtonLayout)
            includeCliNextButton.btnContinue.setOnClickListener(this@OfferCalculationFragment)
            tvSlideToEditAmount.setOnClickListener(this@OfferCalculationFragment)
            includeNoConnectionLayout.btnRetry.setOnClickListener(this@OfferCalculationFragment)
            includeCliNextButton.btnContinue.text = activity?.resources?.getString(R.string.accept_offer)
            showView(includeCliNextButton.btnContinue)
            mCliPhase2Activity?.showDeclineOffer()
            try {
                DrawImage(activity).handleGIFImage(imOfferTime)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun additionalAmountSignSum(additionalCreditLimit: Int): String {
        var additionalCreditLimit = additionalCreditLimit
        val strAdditionalCreditLimit: String
        when (sign(additionalCreditLimit.toFloat()).toInt()) {
            0 -> strAdditionalCreditLimit = formatAmount(additionalCreditLimit)
            1 -> strAdditionalCreditLimit = "+ " + formatAmount(additionalCreditLimit)
            -1 -> {
                additionalCreditLimit = abs(additionalCreditLimit)
                strAdditionalCreditLimit = "- " + formatAmount(additionalCreditLimit)
            }
            else -> {
                additionalCreditLimit = abs(additionalCreditLimit)
                strAdditionalCreditLimit = formatAmount(additionalCreditLimit)
            }
        }
        return strAdditionalCreditLimit
    }

    private fun formatAmount(amount: Int): String {
        return WFormatter.amountFormat(amount * 100)
    }

    private fun onLoad() {
        binding?.apply {
            setBackgroundColor(llEmptyLayout, R.color.white)
            setBackgroundColor(includeCliNextButton.llNextButtonLayout, R.color.white)
            setBackgroundColor(flTopLayout, R.color.white)
            disableView(includeCliNextButton.relNextButton)
            disableView(includeCliNextButton.btnContinue)
            getCLIText(tvCalculatingYourOffer, R.string.calculating_your_offer)
            getCLIText(tvLoadTime, R.string.amount_of_time_info)
            hideView(tvCurrentCreditLimitAmount)
            hideView(tvAdditionalCreditLimitAmount)
            hideView(llSlideToEditContainer)
            hideView(tvNewCreditLimitAmount)
            showView(cpCurrentCreditLimit)
            showView(cpAdditionalCreditLimit)
            showView(cpNewCreditAmount)
            showView(flCircularProgressSpinner)
            showView(includeCliNextButton.llNextButtonLayout)
            progressColorFilter(cpCurrentCreditLimit, Color.BLACK)
            progressColorFilter(cpAdditionalCreditLimit, Color.BLACK)
            progressColorFilter(cpNewCreditAmount, Color.BLACK)
            mCliPhase2Activity?.disableDeclineButton()
            hideDeclineButton()
        }
    }

    private fun onLoadComplete() {
        binding?.apply {
            setBackgroundColor(llEmptyLayout, R.color.default_background)
            setBackgroundColor(flTopLayout, R.color.default_background)
            setBackgroundColor(includeCliNextButton.llNextButtonLayout, R.color.default_background)
            enableView(includeCliNextButton.relNextButton)
            getCLIText(tvCalculatingYourOffer, R.string.pre_approved_for_title)
            getCLIText(tvLoadTime, R.string.subject_to_proof_of_income)
            showView(tvCurrentCreditLimitAmount)
            showView(tvAdditionalCreditLimitAmount)
            showView(llSlideToEditContainer)
            showView(tvNewCreditLimitAmount)
            hideView(cpCurrentCreditLimit)
            hideView(cpAdditionalCreditLimit)
            hideView(cpNewCreditAmount)
            hideView(flCircularProgressSpinner)
            enableView(includeCliNextButton.btnContinue)
            enableView(includeCliNextButton.llNextButtonLayout)
            showDeclineButton()
        }
    }

    private fun getCLIText(wTextView: WTextView?, id: Int) {
        wTextView?.setText(activity?.resources?.getString(id))
    }

    private fun disableView(v: View?) {
        v?.isEnabled = false
    }

    private fun enableView(v: View?) {
        v?.isEnabled = true
    }

    private fun hideView(v: View?) {
        v?.visibility = View.GONE
    }

    private fun showView(v: View?) {
        v?.visibility = View.VISIBLE
    }

    private fun onAcceptOfferLoad() {
        binding?.apply {
            showView(includeCliNextButton.mWoolworthsProgressBar)
            includeCliNextButton.mWoolworthsProgressBar?.indeterminateDrawable?.setColorFilter(
                Color.WHITE,
                PorterDuff.Mode.MULTIPLY
            )
            hideView(includeCliNextButton.btnContinue)
        }
    }

    private fun onAcceptOfferCompleted() {
        binding?.apply {
            hideView(includeCliNextButton.mWoolworthsProgressBar)
            showView(includeCliNextButton.btnContinue)
            includeCliNextButton.btnContinue.setTextColor(Color.WHITE)
            includeCliNextButton.btnContinue.contentDescription = getString(R.string.accept_offer)
        }
    }

    private fun setBackgroundColor(v: View?, id: Int) {
        v?.setBackgroundColor(ContextCompat.getColor(requireContext(), id))
    }

    private fun calculateAdditionalAmount(currentCreditLimit: Int, newCreditLimit: String): Int {
        val mNewCreditLimit = Utils.numericFieldOnly(newCreditLimit)
        return mNewCreditLimit - currentCreditLimit + INCREASE_PROGRESS_BY
    }

    private fun progressColorFilter(progressBar: ProgressBar?, color: Int) {
        progressBar?.isIndeterminate = true
        progressBar?.indeterminateDrawable?.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    @SuppressLint("CommitTransaction")
    override fun onClick(v: View) {
        MultiClickPreventer.preventMultiClick(v)
        when (v.id) {
            R.id.btnContinue -> {
                val activity: Activity? = activity
                if (activity != null) {
                    val firebaseEvent = (activity as CLIPhase2Activity).getFirebaseEvent()
                    firebaseEvent?.forAcceptOffer()
                }
                onAcceptOfferLoad()
                val newCreditLimitAmount = Utils.numericFieldOnly(
                    binding?.tvNewCreditLimitAmount?.text.toString()
                )
                val createOfferDecision = CLIOfferDecision(
                    WoolworthsApplication.getProductOfferingId(),
                    newCreditLimitAmount,
                    true
                )
                cliAcceptOfferDecision = OneAppService().createOfferDecision(createOfferDecision, mCLiId.toString())
                cliAcceptOfferDecision?.enqueue(
                    CompletionHandler(
                        object : IResponseListener<OfferActive> {
                            override fun onSuccess(offerActive: OfferActive?) {
                                mObjOffer = offerActive
                                when (offerActive?.httpCode) {
                                    200 -> {
                                        val nextStep = mObjOffer?.nextStep
                                        if (nextStep?.lowercase(Locale.getDefault()).equals(
                                                getString(R.string.status_poi_required),
                                                ignoreCase = true
                                            )
                                        ) {
                                            val bundle = Bundle()
                                            bundle.putString(
                                                "OFFER_ACTIVE_PAYLOAD",
                                                Utils.objectToJson(mObjOffer)
                                            )
                                            val documentFragment = DocumentFragment()
                                            documentFragment.arguments = bundle
                                            documentFragment.setStepIndicatorListener(
                                                mCliStepIndicatorListener
                                            )
                                            val fragmentUtils = FragmentUtils()
                                            fragmentUtils.nextFragment(
                                                activity as AppCompatActivity?,
                                                fragmentManager?.beginTransaction(),
                                                documentFragment,
                                                R.id.cli_steps_container
                                            )
                                        } else if (nextStep?.lowercase(Locale.getDefault()).equals(
                                                getString(R.string.status_complete),
                                                ignoreCase = true
                                            ) && mObjOffer?.cliStatus.equals(
                                                getString(
                                                    R.string.cli_status_concluded
                                                ), ignoreCase = true
                                            )
                                        ) {
                                            val processCompleteNoPOIFragment =
                                                ProcessCompleteNoPOIFragment()
                                            processCompleteNoPOIFragment.setStepIndicatorListener(
                                                mCliStepIndicatorListener
                                            )
                                            val fragmentUtils = FragmentUtils()
                                            fragmentUtils.nextFragment(
                                                activity as AppCompatActivity?,
                                                fragmentManager?.beginTransaction(),
                                                processCompleteNoPOIFragment,
                                                R.id.cli_steps_container
                                            )
                                            hideDeclineButton()
                                        } else {
                                            finishActivity()
                                        }
                                    }
                                    440 -> SessionUtilities.getInstance().setSessionState(
                                        SessionDao.SESSION_STATE.INACTIVE,
                                        mObjOffer?.response?.stsParams,
                                        getActivity()
                                    )
                                    else -> {
                                        val response = mObjOffer?.response
                                        if (response != null) {
                                            showErrorMessage(getActivity(), response)
                                        }
                                    }
                                }
                                loadSuccess()
                                onAcceptOfferCompleted()
                            }

                            override fun onFailure(error: Throwable?) {
                                val activity: Activity? = getActivity()
                                activity?.runOnUiThread {
                                    latestBackgroundTask(LATEST_BACKGROUND_CALL.ACCEPT_OFFER)
                                    loadFailure()
                                    onAcceptOfferCompleted()
                                }
                            }
                        }, OfferActive::class.java
                    )
                )
            }
            R.id.tvSlideToEditAmount -> try {
                val args = Bundle()
                val slideAmount = binding?.tvSlideToEditAmount?.text.toString()
                args.putInt("slideAmount", Utils.numericFieldOnly(slideAmount))
                args.putInt("currentCredit", mCurrentCredit)
                args.putInt("creditRequestMax", mCreditRequestMax)
                mCliPhase2Activity?.actionBarBackIcon()
                val editAmountFragment = EditSlideAmountFragment()
                editAmountFragment.arguments = args
                editAmountFragment.setStepIndicatorListener(mCliStepIndicatorListener)
                val ftils = FragmentUtils(false)
                ftils.nextFragment(
                    this@OfferCalculationFragment.activity as AppCompatActivity?,
                    fragmentManager?.beginTransaction(),
                    editAmountFragment,
                    R.id.cli_steps_container
                )
                editorWasShown = true
            } catch (ignored: NullPointerException) {
            }
            R.id.btnRetry -> if (NetworkManager.getInstance().isConnectedToNetwork(
                    activity
                )
            ) {
                showView(binding?.includeCliNextButton?.llNextButtonLayout)
                mErrorHandlerView?.hideErrorHandlerLayout()
                cliApplicationRequest(mEventStatus)
            }
        }
    }

    private fun createOffer(
        hashIncomeDetail: HashMap<String, String>?,
        hashExpenseDetail: HashMap<String, String>?,
        maritalStatus: ConfigMaritalStatus?
    ): CreateOfferRequest {
        return CreateOfferRequest(
            WoolworthsApplication.getProductOfferingId(),
            roundOffCentValues(hashIncomeDetail?.get("GROSS_MONTHLY_INCOME")),
            roundOffCentValues(hashIncomeDetail?.get("NET_MONTHLY_INCOME")),
            roundOffCentValues(hashIncomeDetail?.get("ADDITIONAL_MONTHLY_INCOME")),
            roundOffCentValues(hashExpenseDetail?.get("MORTGAGE_PAYMENTS")),
            roundOffCentValues(hashExpenseDetail?.get("RENTAL_PAYMENTS")),
            roundOffCentValues(hashExpenseDetail?.get("MAINTENANCE_EXPENSES")),
            roundOffCentValues(hashExpenseDetail?.get("MONTHLY_CREDIT_EXPENSES")),
            roundOffCentValues(hashExpenseDetail?.get("OTHER_EXPENSES")),
            maritalStatus?.statusId ?: 0
        )
    }

    fun displayApplication(mObjOffer: OfferActive?) {
        binding.apply {
            if (mObjOffer != null) {
                showDeclineButton()
                when (mObjOffer.httpCode) {
                    200 -> {
                        enableDeclineButton()
                        val offer = mObjOffer.offer
                        mCurrentCredit = offer.currCredit + INCREASE_PROGRESS_BY
                        currentCredit = mCurrentCredit
                        mCurrentCredit -= mCurrentCredit % 100
                        val nextStep = mObjOffer.nextStep
                        if (nextStep.lowercase(Locale.getDefault()).equals(
                                getString(R.string.status_offer).lowercase(
                                    Locale.getDefault()
                                ), ignoreCase = true
                            )
                            || nextStep.lowercase(Locale.getDefault()).equals(
                                getString(R.string.status_poi_required).lowercase(
                                    Locale.getDefault()
                                ), ignoreCase = true
                            )
                            || nextStep.lowercase(Locale.getDefault()).equals(
                                getString(R.string.status_complete),
                                ignoreCase = true
                            ) && mObjOffer.cliStatus.equals(
                                getString(
                                    R.string.cli_status_concluded
                                ), ignoreCase = true
                            )
                        ) {
                            mCreditRequestMax = offer.creditRequestMax
                            val mDifferenceCreditLimit = mCreditRequestMax - mCurrentCredit
                            mCLiId = mObjOffer.cliId
                            binding?.apply {
                                if (mCreditRequestMax <= mCurrentCredit) {
                                    sbSlideAmount.max = 0
                                    val amount = formatAmount(0)
                                    tvSlideToEditAmount.text = amount
                                    tvCurrentCreditLimitAmount.text = amount
                                    tvNewCreditLimitAmount.text = amount
                                    mGlobalState?.creditLimit = amount
                                    tvAdditionalCreditLimitAmount.text = amount
                                } else {
                                    tvCurrentCreditLimitAmount.setText(formatAmount(currentCredit - INCREASE_PROGRESS_BY))
                                    sbSlideAmount.max = mDifferenceCreditLimit
                                    sbSlideAmount.incrementProgressBy(INCREASE_PROGRESS_BY)
                                    animSeekBarToMaximum()
                                    tvNewCreditLimitAmount.text = tvSlideToEditAmount.text.toString()
                                    tvAdditionalCreditLimitAmount.text =
                                        additionalAmountSignSum(
                                            calculateAdditionalAmount(
                                                currentCredit,
                                                tvNewCreditLimitAmount.text.toString()
                                            )
                                        )

                                    val newCreditLimitAmount = Utils.numericFieldOnly(
                                        tvNewCreditLimitAmount.text.toString()
                                    )
                                    mGlobalState?.setDecisionDeclineOffer(
                                        CLIOfferDecision(
                                            WoolworthsApplication.getProductOfferingId(),
                                            newCreditLimitAmount,
                                            false
                                        )
                                    )
                                }
                            }
                            onLoadComplete()
                        } else if (nextStep.lowercase(Locale.getDefault())
                                .equals(getString(R.string.status_decline), ignoreCase = true)
                        ) {
                            declineMessage()
                        } else {
                            displayMessageError(mObjOffer)
                        }
                    }
                    440 -> SessionUtilities.getInstance().setSessionState(
                        SessionDao.SESSION_STATE.INACTIVE,
                        mObjOffer.response.stsParams,
                        activity
                    )
                    else -> displayMessageError(mObjOffer)
                }
            }
        }
    }

    private fun displayMessageError(offerActive: OfferActive?) {
        if (mCliPhase2Activity != null) mCliPhase2Activity?.hideCloseIcon()
        onLoadComplete()
        if (offerActive != null) {
            val response = offerActive.response
            if (response != null) {
                Utils.displayValidationMessage(
                    activity, CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR,response.desc.ifEmpty { getString(R.string.unfortunately_something_went_wrong) }
                )
            }
        }
    }

    private fun declineMessage() {
        if (mCliPhase2Activity != null) mCliPhase2Activity?.hideCloseIcon()
        onLoadComplete()
        Utils.displayValidationMessage(
            activity, CustomPopUpWindow.MODAL_LAYOUT.CLI_DECLINE, getString(
                R.string.cli_declined_popup_title
            ), getString(R.string.cli_declined_popup_description), false
        )
    }

    fun setInvisibleView(invisibleView: View?) {
        invisibleView?.visibility = View.INVISIBLE
    }

    private fun latestBackgroundTask(latest_background_call: LATEST_BACKGROUND_CALL) {
        this.latest_background_call = latest_background_call
    }

    override fun onPause() {
        super.onPause()
        val activity: Activity? = activity
        activity?.unregisterReceiver(mConnectionBroadcast)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_OFFER)
        if (mCliPhase2Activity != null) {
            mCliPhase2Activity?.actionBarCloseIcon()
        }
        val activity: Activity? = activity
        activity?.registerReceiver(
            mConnectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    override fun onConnectionChanged() {
        retryConnect()
    }

    private fun retryConnect() {
        val activity: Activity? = activity
        activity?.runOnUiThread {
            if (NetworkManager.getInstance()
                    .isConnectedToNetwork(getActivity())
            ) {
                if (loadState?.onLoanCompleted() == false) {
                    if (latest_background_call != null) {
                        when (latest_background_call) {
                            LATEST_BACKGROUND_CALL.DECLINE_OFFER -> woolworthsApplication
                                ?.bus()
                                ?.send(CLIOfferDecision())
                            LATEST_BACKGROUND_CALL.ACCEPT_OFFER -> binding?.includeCliNextButton?.btnContinue?.performClick()
                            else -> {}
                        }
                    }
                }
            } else {
                when (latest_background_call) {
                    LATEST_BACKGROUND_CALL.DECLINE_OFFER -> mErrorHandlerView?.showToast()
                    LATEST_BACKGROUND_CALL.ACCEPT_OFFER -> mErrorHandlerView?.showToast()
                    else -> {}
                }
            }
        }
    }

    fun animSeekBarToMaximum() {
        binding?.apply {
            val anim = ValueAnimator.ofInt(0, sbSlideAmount.max)
            anim.duration = SLIDE_ANIM_DURATION.toLong()
            anim.addUpdateListener { animation ->
                val animProgress = animation.animatedValue as Int
                sbSlideAmount.progress = animProgress
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val activity: Activity? = activity
                    if (activity != null) {
                        Utils.showOneTimeTooltip(
                            activity,
                            SessionDao.KEY.CLI_SLIDE_EDIT_AMOUNT_TOOLTIP,
                            sbSlideAmount,
                            getString(
                                R.string.slide_to_edit_amount
                            )
                        )
                    }
                }
            })
            anim.start()
        }
    }

    private fun cliApplicationRequest(eventStatus: EventStatus?) {
        when (eventStatus) {
            EventStatus.CREATE_APPLICATION -> cliCreateApplication(
                createOffer(
                    mHashIncomeDetail,
                    mHashExpenseDetail,
                    maritalStatus
                )
            )
            EventStatus.UPDATE_APPLICATION -> cliUpdateApplication(
                createOffer(
                    mHashIncomeDetail,
                    mHashExpenseDetail,
                    maritalStatus
                ), mCLiId.toString()
            )
            else -> displayApplication(mObjOffer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposables.isDisposed) disposables.clear()
        cancelRequest(cliUpdateApplication)
        cancelRequest(createOfferTask)
        cancelRequest(cliAcceptOfferDecision)
        cancelRequest(cliOfferDecision)
    }

    fun finishActivity() {
        woolworthsApplication?.bus()?.send(BusStation(true))
        activity?.apply {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    private fun loadSuccess() {
        loadState?.setLoadComplete(true)
    }

    private fun loadFailure() {
        loadState?.setLoadComplete(false)
    }

    private fun showDeclineButton() {
        mCliPhase2Activity?.showDeclineOffer()
    }

    private fun hideDeclineButton() {
        mCliPhase2Activity?.hideDeclineOffer()
    }

    private fun cancelRequest(call: Call<*>?) {
        if (call != null && !call.isCanceled) {
            call.cancel()
        }
    }

    private fun enableDeclineButton() {
        mCliPhase2Activity?.enableDeclineButton()
    }

    fun openCreditLimitDecreaseFragmentDialog(slideProgressAmount: Int) {
        /**
         * mNewCLIAmount represents the actual tracked value
         * mCreditRequestMax represents the maximum credit amount allowed
         */
        if (mNewCLIAmount < mCreditRequestMax) {
            val activity = activity
            if (activity == null && !isAdded) return
            val creditLimitDecreaseConfirmationFragment =
                newInstance(formatAmount(slideProgressAmount))
            fragmentManager?.let {
                creditLimitDecreaseConfirmationFragment.show(
                    it,
                    StartupActivity::class.java.simpleName
                )
            }
        }
    }

    private fun showErrorMessage(activity: Activity?, response: Response) {
        Utils.showGeneralErrorDialog(
            activity,
            response.desc.ifEmpty { response.message }
        )
    }

    companion object {
        private const val INCREASE_PROGRESS_BY = 100
        private const val SLIDE_ANIM_DURATION = 1500

        fun roundOffCentValues(s: String?): Int {
            s?.let {
                val length = it.length
                return if (length < 2) it.toInt() else it.substring(0, length - 2).toInt()
            } ?: kotlin.run {
                return 0
            }
        }
    }
}
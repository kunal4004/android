package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.awfs.coordination.R
import com.awfs.coordination.databinding.DocumentFragmentBinding
import com.google.gson.Gson
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.cliEmailResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService.cliUpdateBankDetail
import za.co.woolworths.financial.services.android.models.network.OneAppService.getBankAccountTypes
import za.co.woolworths.financial.services.android.models.network.OneAppService.getDeaBanks
import za.co.woolworths.financial.services.android.models.service.event.LoadState
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.adapters.DocumentAdapter
import za.co.woolworths.financial.services.android.ui.adapters.DocumentsAccountTypeAdapter
import za.co.woolworths.financial.services.android.ui.adapters.DocumentsAccountTypeAdapter.OnAccountTypeClick
import za.co.woolworths.financial.services.android.ui.adapters.POIDocumentSubmitTypeAdapter
import za.co.woolworths.financial.services.android.ui.adapters.POIDocumentSubmitTypeAdapter.OnSubmitType
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.controller.CLIFragment
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController
import java.util.*

class DocumentFragment : CLIFragment(R.layout.document_fragment), DocumentAdapter.OnItemClick, NetworkChangeListener,
    OnAccountTypeClick, View.OnClickListener, OnSubmitType, TextWatcher {

    private lateinit var binding: DocumentFragmentBinding

    private val ANIM_DURATION = 600
    private var deaBankList: DeaBanks? = null
    private var connectionBroadcast: BroadcastReceiver? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var bankAccountTypesList: MutableList<BankAccountType>? = null
    private val otherBank = "Other"
    private var accountTypeAdapter: DocumentsAccountTypeAdapter? = null
    private var documentSubmitTypeAdapter: POIDocumentSubmitTypeAdapter? = null
    var submitType: SubmitType? = null
    private var mDeaBankList: MutableList<Bank>? = null
    var selectedBankType: String? = null
    var selectedAccountType: String? = null
    private var cliGetBankAccountTypes: Call<BankAccountTypes>? = null
    private var cliGetDeaBank: Call<DeaBanks>? = null
    private var cliUpdateBankDetails: Call<UpdateBankDetailResponse>? = null
    private var cliSendEmail: Call<CLIEmailResponse>? = null
    private var loadState: LoadState? = null
    private var activeOfferObj: OfferActive? = null

    enum class NetworkFailureRequest {
        DEA_BANK, ACCOUNT_TYPE
    }

    enum class SubmitType(val type: Int) {
        ACCOUNT_NUMBER(1), DOCUMENTS(2), LATER(3);
    }

    var networkFailureRequest: NetworkFailureRequest? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DocumentFragmentBinding.bind(view)

        deaBankList = DeaBanks()
        if (mCliStepIndicatorListener != null) {
            mCliStepIndicatorListener?.onStepSelected(4)
        }

        connectionBroadcast()
        arguments?.let {
            val offerActive = it.getString("OFFER_ACTIVE_PAYLOAD")
            if (!TextUtils.isEmpty(offerActive)) {
                activeOfferObj = Gson().fromJson(offerActive, OfferActive::class.java)
            }
        }
        val mCliPhase2Activity = activity as CLIPhase2Activity?
        loadState = LoadState()
        mCliPhase2Activity?.actionBarCloseIcon()
        mCliPhase2Activity?.hideDeclineOffer()
        init()
        onLoad(binding.pbDeaBank)
        cliDeaBankRequest()
        loadPOIDocumentsSubmitTypeView()
    }

    private fun connectionBroadcast() {
        connectionBroadcast = Utils.connectionBroadCast(
            activity, this
        )
    }

    private fun onLoad(pBar: ProgressBar?) {
        showView(pBar)
        progressColorFilter(pBar, R.color.black)
    }

    private fun onLoadComplete(v: View?) {
        activity?.runOnUiThread { hideView(v) }
    }

    private fun cliDeaBankRequest() {
        onLoad(binding.pbDeaBank)
        cliGetDeaBank = getDeaBanks()
        cliGetDeaBank?.enqueue(
            CompletionHandler(
                object : IResponseListener<DeaBanks> {
                    override fun onSuccess(deaBanks: DeaBanks?) {
                        when (deaBanks?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                deaBankList = deaBanks
                                mDeaBankList = deaBanks.banks
                                if (mDeaBankList != null) {
                                    val rand = Random()
                                    val n = rand.nextInt(50) + 1
                                    mDeaBankList?.add(Bank(n, otherBank, ""))
                                }
                                selectBankLayoutManager(mDeaBankList)
                            }
                            AppConstant.HTTP_SESSION_TIMEOUT_440 -> SessionUtilities.getInstance()
                                .setSessionState(
                                    SessionDao.SESSION_STATE.INACTIVE,
                                    deaBankList?.response?.stsParams,
                                    activity
                                )
                            else -> mErrorHandlerView?.responseError(view, "")
                        }
                        loadSuccess()
                        onLoadComplete(binding.pbDeaBank)
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error != null) {
                            loadFailure()
                            networkFailureRequest = NetworkFailureRequest.DEA_BANK
                            mErrorHandlerView?.responseError(view, error.message)
                            onLoadComplete(binding.pbDeaBank)
                        }
                    }
                }, DeaBanks::class.java
            )
        )
    }

    fun showProofOfIncomePopup() {
        try {
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.PROOF_OF_INCOME,
                ""
            )
        } catch (ignored: NullPointerException) {
        }
    }

    private fun cliBankAccountTypeRequest() {
        onLoad(binding.pbAccountType)
        if (accountTypeAdapter != null && bankAccountTypesList != null) {
            bankAccountTypesList?.clear()
            accountTypeAdapter?.notifyDataSetChanged()
        }
        cliGetBankAccountTypes = getBankAccountTypes()
        cliGetBankAccountTypes?.enqueue(
            CompletionHandler(
                object : IResponseListener<BankAccountTypes> {
                    override fun onSuccess(bankAccountTypes: BankAccountTypes?) {
                        when (bankAccountTypes?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                bankAccountTypesList = bankAccountTypes.bankAccountTypes
                                loadBankAccountTypesView(bankAccountTypesList)
                            }
                            AppConstant.HTTP_SESSION_TIMEOUT_440 -> SessionUtilities.getInstance()
                                .setSessionState(
                                    SessionDao.SESSION_STATE.INACTIVE,
                                    bankAccountTypes.response.stsParams,
                                    activity
                                )
                            else -> mErrorHandlerView?.responseError(view, "")
                        }
                        onLoadComplete(binding.pbAccountType)
                    }

                    override fun onFailure(error: Throwable?) {
                        if (error != null) {
                            onLoadComplete(binding.pbAccountType)
                            networkFailureRequest = NetworkFailureRequest.ACCOUNT_TYPE
                            mErrorHandlerView?.responseError(view, error.message)
                        }
                    }
                }, BankAccountTypes::class.java
            )
        )
    }

    private fun init() {
        binding.apply {
            (rclSelectYourBank?.itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations =
                false
            mErrorHandlerView = ErrorHandlerView(activity, includeNoConnectionLayout.noConnectionLayout)
            mErrorHandlerView?.setMargin(includeNoConnectionLayout.noConnectionLayout, 0, 0, 0, 0)
            yesPOIFromBank?.setOnClickListener(this@DocumentFragment)
            noPOIFromBank?.setOnClickListener(this@DocumentFragment)
            llAccountNumberLayout.setOnClickListener(this@DocumentFragment)
            etAccountNumber?.addTextChangedListener(this@DocumentFragment)
            addDocuments.setOnClickListener(this@DocumentFragment)
            btnSubmit?.setOnClickListener(this@DocumentFragment)
            includeNoConnectionLayout.btnRetry.setOnClickListener(this@DocumentFragment)
        }
    }

    private fun selectBankLayoutManager(deaBankList: List<Bank>?) {
        val mLayoutManager = LinearLayoutManager(activity)
        val documentAdapter = DocumentAdapter(deaBankList, this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rclSelectYourBank?.layoutManager = mLayoutManager
        binding.rclSelectYourBank?.adapter = documentAdapter
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_BANKS)
    }

    private fun loadBankAccountTypesView(accountTypes: List<BankAccountType>?) {
        val mLayoutManager = LinearLayoutManager(activity)
        accountTypeAdapter = DocumentsAccountTypeAdapter(accountTypes, this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rclSelectAccountType?.layoutManager = mLayoutManager
        binding.rclSelectAccountType?.adapter = accountTypeAdapter
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_BANKS_ACCOUNT_TYPE
        )
    }

    private fun loadPOIDocumentsSubmitTypeView() {
        val mLayoutManager = LinearLayoutManager(activity)
        documentSubmitTypeAdapter = POIDocumentSubmitTypeAdapter(this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
    }

    override fun onItemClick(view: View, position: Int) {
        val selectedBank = mDeaBankList?.get(position)
        if (selectedBank?.bankName.equals(otherBank, ignoreCase = true)) {
            hideView(binding.bankTypeConfirmationLayout)
            invalidateBankTypeSelection()
            scrollUpDocumentSubmitTypeLayout()
        } else {
            val selectedBankName = selectedBank?.bankName ?: ""
            selectedBankType = selectedBank?.bankName ?: ""
            val defaultAccountTypeTitle = getString(R.string.account_type)
            val defaultAccountSavingTitle = getString(R.string.account_saving_title)
            binding.tvCLIAccountTypeTitle?.setText(
                defaultAccountTypeTitle.replace(
                    "###",
                    selectedBankName
                )
            )
            binding.tvAccountSavingTitle?.setText(
                defaultAccountSavingTitle.replace(
                    "###",
                    selectedBankName
                )
            )
            hideView(binding.poiDocumentSubmitTypeLayout)
            invalidateBankTypeSelection()
            scrollUpConfirmationFroPOIFromBankLayout()
        }
    }

    override fun onAccountTypeClick(view: View, position: Int) {
        selectedAccountType = bankAccountTypesList?.get(position)?.accountType
        scrollUpAccountNumberLayout()
    }

    override fun onSubmitTypeSelected(view: View, position: Int) {
        if (position == 1) {
            submitType = SubmitType.LATER
            scrollUpDocumentSubmitTypeLayout()
            showView(binding.rlSubmitCli)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(connectionBroadcast)
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(
            connectionBroadcast,
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
                    if (submitType != null) {
                        binding.btnSubmit?.performClick()
                    }
                }
            } else {
                mErrorHandlerView?.showToast()
                enableSubmitButton()
            }
        }
    }

    private fun progressColorFilter(progressBar: ProgressBar?, color: Int) {
        progressBar?.indeterminateDrawable?.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    private fun hideView(v: View?) {
        if (v?.visibility == View.VISIBLE) v.visibility = View.GONE
    }

    private fun showView(v: View?) {
        v?.visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        binding.apply {
            MultiClickPreventer.preventMultiClick(view)
            val activity = activity ?: return
            when (view.id) {
                R.id.yesPOIFromBank -> {
                    submitType = SubmitType.ACCOUNT_NUMBER
                    noPOIFromBank?.setBackgroundColor(
                        ContextCompat.getColor(
                            activity,
                            android.R.color.transparent
                        )
                    )
                    noPOIFromBank?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.cli_yes_no_button_color
                        )
                    )
                    yesPOIFromBank?.setBackgroundColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.black
                        )
                    )
                    yesPOIFromBank?.setTextColor(ContextCompat.getColor(activity, R.color.white))
                    hideView(poiDocumentSubmitTypeLayout)
                    hideView(rlSubmitCli)
                    if (documentSubmitTypeAdapter != null) documentSubmitTypeAdapter?.clearSelection()
                    scrollUpAccountTypeSelectionLayout()
                }
                R.id.noPOIFromBank -> {
                    submitType = SubmitType.LATER
                    yesPOIFromBank?.setBackgroundColor(
                        ContextCompat.getColor(
                            activity,
                            android.R.color.transparent
                        )
                    )
                    yesPOIFromBank?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.cli_yes_no_button_color
                        )
                    )
                    noPOIFromBank?.setBackgroundColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.black
                        )
                    )
                    noPOIFromBank?.setTextColor(ContextCompat.getColor(activity, R.color.white))
                    resetAccountNumberView()
                    hideView(accountTypeLayout)
                    hideView(accountNumberLayout)
                    if (accountTypeAdapter != null) accountTypeAdapter?.clearSelection()
                    scrollUpDocumentSubmitTypeLayout()
                }
                R.id.llAccountNumberLayout -> IncreaseLimitController.populateExpenseField(
                    etAccountNumber,
                    activity
                )
                R.id.btnSubmit -> onSubmitClick(submitType)
                R.id.uploadDocumentInfo -> {}
                R.id.btnRetry -> if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    mErrorHandlerView?.hideErrorHandler()
                    when (networkFailureRequest) {
                        NetworkFailureRequest.DEA_BANK -> cliDeaBankRequest()
                        NetworkFailureRequest.ACCOUNT_TYPE -> cliBankAccountTypeRequest()
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    fun scrollUpAccountTypeSelectionLayout() {
        binding.apply {
            cliBankAccountTypeRequest()
            hideView(uploadDocumentsLayout)
            showView(accountTypeLayout)
            dynamicLayoutPadding(bankTypeConfirmationLayout, true)
            dynamicLayoutPadding(poiDocumentSubmitTypeLayout, true)
            dynamicLayoutPadding(accountTypeLayout, false)
            nestedScrollview?.post {
                ObjectAnimator.ofInt(
                    nestedScrollview,
                    "scrollY",
                    accountTypeLayout.top
                ).setDuration(ANIM_DURATION.toLong()).start()
            }
        }
    }

    fun scrollUpConfirmationFroPOIFromBankLayout() {
        binding.apply {
            showView(bankTypeConfirmationLayout)
            dynamicLayoutPadding(bankTypeConfirmationLayout, false)
            nestedScrollview?.post {
                ObjectAnimator.ofInt(
                    nestedScrollview,
                    "scrollY",
                    bankTypeConfirmationLayout.top
                ).setDuration(ANIM_DURATION.toLong()).start()
            }
            Utils.setScreenName(
                activity,
                FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_BANKS_AUTHORIZATION
            )
        }
    }

    fun scrollUpDocumentSubmitTypeLayout() {
        binding.apply {
            submitType = SubmitType.LATER
            hideView(uploadDocumentsLayout)
            showView(poiDocumentSubmitTypeLayout)
            dynamicLayoutPadding(bankTypeConfirmationLayout, true)
            dynamicLayoutPadding(poiDocumentSubmitTypeLayout, false)
            nestedScrollview.post {
                ObjectAnimator.ofInt(
                    nestedScrollview,
                    "scrollY",
                    poiDocumentSubmitTypeLayout.top
                ).setDuration(ANIM_DURATION.toLong()).start()
            }
            showView(rlSubmitCli)
            showProofOfIncomePopup()
            setButtonProceed()
        }
    }

    fun scrollUpAccountNumberLayout() {
        binding.apply {
            resetAccountNumberView()
            hideView(uploadDocumentsLayout)
            showView(accountNumberLayout)
            dynamicLayoutPadding(accountTypeLayout, true)
            dynamicLayoutPadding(accountNumberLayout, false)
            nestedScrollview.post {
                ObjectAnimator.ofInt(
                    nestedScrollview,
                    "scrollY",
                    accountNumberLayout.top
                ).setDuration(ANIM_DURATION.toLong()).start()
            }
            setButtonSubmit()
            Utils.setScreenName(
                activity,
                FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_BANK_ACC_NUMBER
            )
        }
    }

    fun invalidateBankTypeSelection() {
        binding.apply {
            yesPOIFromBank?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            yesPOIFromBank?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cli_yes_no_button_color
                )
            )
            noPOIFromBank?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            noPOIFromBank?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cli_yes_no_button_color
                )
            )
            hideView(accountTypeLayout)
            hideView(accountNumberLayout)
            hideView(rlSubmitCli)
            hideView(uploadDocumentsLayout)
            resetAccountNumberView()
            if (accountTypeAdapter != null) accountTypeAdapter?.clearSelection()
            if (documentSubmitTypeAdapter != null) documentSubmitTypeAdapter?.clearSelection()
        }
    }

    fun resetAccountNumberView() {
        binding.apply {
            etAccountNumber?.text?.clear()
            hideView(rlSubmitCli)
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable) {
        binding.apply {
            if (Utils.checkCLIAccountNumberValidation(
                    etAccountNumber?.text.toString()
                ) && rlSubmitCli?.visibility == View.GONE
            ) showView(rlSubmitCli) else if (!Utils.checkCLIAccountNumberValidation(
                    etAccountNumber?.text.toString()
                )
            ) hideView(rlSubmitCli)
        }
    }

    fun dynamicLayoutPadding(view: View?, defaultPaddingEnabled: Boolean) {
        val ilc = IncreaseLimitController(activity)
        val paddingPixel = 16
        val density = requireActivity().resources.displayMetrics.density
        val paddingDp = (paddingPixel * density).toInt()
        val screenHeight = ilc.getScreenHeight(activity) / 3
        if (defaultPaddingEnabled) {
            view?.setPadding(0, paddingDp, 0, 0)
        } else {
            view?.setPadding(0, paddingDp, 0, screenHeight)
        }
    }

    fun onSubmitClick(type: SubmitType?) {
        when (type) {
            SubmitType.ACCOUNT_NUMBER -> updateBankDetails()
            SubmitType.LATER -> initSendEmailRequest()
            else -> {}
        }
    }

    fun updateBankDetails() {
        disableSubmitButton()
        val bankDetail = UpdateBankDetail()
        bankDetail.cliOfferID = activeOfferObj?.cliId
        bankDetail.accountType = selectedAccountType
        bankDetail.bankName = selectedBankType
        bankDetail.accountNumber = binding.etAccountNumber?.text.toString().trim { it <= ' ' }
        cliUpdateBankDetails = cliUpdateBankDetail(bankDetail)
        cliUpdateBankDetails?.enqueue(
            CompletionHandler(
                object : IResponseListener<UpdateBankDetailResponse> {
                    override fun onSuccess(updateBankDetailResponse: UpdateBankDetailResponse?) {
                        val activity: Activity? = activity
                        if (activity == null || !isAdded) return
                        val firebaseCreditCardDeliveryEvent =
                            (activity as CLIPhase2Activity).getFirebaseEvent()
                        firebaseCreditCardDeliveryEvent?.forDeaOptin()
                        enableSubmitButton()
                        val processCompleteFragment = ProcessCompleteFragment()
                        moveToProcessCompleteFragment(processCompleteFragment)
                        loadSuccess()
                    }

                    override fun onFailure(error: Throwable?) {
                        loadFailure()
                        enableSubmitButton()
                    }
                }, UpdateBankDetailResponse::class.java
            )
        )
    }

    fun disableSubmitButton() {
        binding.apply {
            Utils.disableEnableChildViews(nestedScrollview, false)
            pbSubmit?.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
            pbSubmit?.visibility = View.VISIBLE
            btnSubmit?.visibility = View.GONE
        }
    }

    fun enableSubmitButton() {
        binding.apply {
            Utils.disableEnableChildViews(nestedScrollview, true)
            pbSubmit?.visibility = View.GONE
            btnSubmit?.visibility = View.VISIBLE
        }
    }

    @SuppressLint("CommitTransaction")
    fun moveToProcessCompleteFragment(fragment: CLIFragment) {
        val activity = activity ?: return
        fragment.setStepIndicatorListener(mCliStepIndicatorListener)
        val fragmentUtils = FragmentUtils()
        val fragmentManager = fragmentManager
        if (fragmentManager != null) fragmentUtils.nextFragment(
            activity as AppCompatActivity,
            fragmentManager.beginTransaction(),
            fragment,
            R.id.cli_steps_container
        )
    }

    fun initSendEmailRequest() {
        disableSubmitButton()
        cliSendEmail = cliEmailResponse()
        cliSendEmail?.enqueue(
            CompletionHandler(
                object : IResponseListener<CLIEmailResponse> {
                    override fun onSuccess(cliEmailResponse: CLIEmailResponse?) {
                        if (cliEmailResponse?.httpCode == AppConstant.HTTP_OK) {
                            val activity: Activity? = activity
                            if (activity == null || !isAdded) return
                            val firebaseCreditCardDeliveryEvent =
                                (activity as CLIPhase2Activity).getFirebaseEvent()
                            firebaseCreditCardDeliveryEvent?.forPOIConfirm()
                            val cliEmailSentFragment = CliEmailSentFragment()
                            moveToProcessCompleteFragment(cliEmailSentFragment)
                        } else {
                            enableSubmitButton()
                        }
                        loadSuccess()
                    }

                    override fun onFailure(error: Throwable?) {
                        loadFailure()
                        enableSubmitButton()
                    }
                }, CLIEmailResponse::class.java
            )
        )
    }

    private fun cancelRequest(httpAsyncTask: Call<*>?) {
        if (httpAsyncTask != null) {
            if (!httpAsyncTask.isCanceled) {
                httpAsyncTask.cancel()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest(cliGetBankAccountTypes)
        cancelRequest(cliGetDeaBank)
        cancelRequest(cliUpdateBankDetails)
        cancelRequest(cliSendEmail)
    }

    private fun loadSuccess() {
        loadState?.setLoadComplete(true)
    }

    private fun loadFailure() {
        loadState?.setLoadComplete(false)
    }

    private fun setButtonProceed() {
        binding.apply {
            btnSubmit?.text = getString(R.string.proceed)
            rlSubmitCli?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun setButtonSubmit() {
        binding.apply {
            btnSubmit?.text = getString(R.string.submit)
            rlSubmitCli?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.recent_search_bg
                )
            )
        }
    }
}
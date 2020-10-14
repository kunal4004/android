package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.pma_update_payment_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import java.util.*

class PaymentMethodExistDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var previousCardNumber: String? = null

    private var root: View? = null
    private var navController: NavController? = null

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogWithoutAnimation
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (root == null)
            root = inflater.inflate(R.layout.pma_update_payment_fragment, container, false)
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is PayMyAccountActivity)
            navController = NavHostFragment.findNavController(this)

        val overdueAmount = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(payMyAccountViewModel.getAccount()?.amountOverdue ?: 0), 1))
        setupListener()

        ShimmerAnimationManager.initShimmer(changeTextViewShimmerLayout)
        ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)

        payMyAccountViewModel.paymentAmountCard.observe(viewLifecycleOwner, { card ->
            if (!isAdded) return@observe
            // set amount amounted
            val amountEntered = card?.amountEntered
            pmaAmountOutstandingTextView?.text = if (amountEntered.isNullOrEmpty() || amountEntered == DEFAULT_RAND_CURRENCY) overdueAmount else amountEntered
            pmaConfirmPaymentButton?.isEnabled = cvvEditTextInput?.length() ?: 0 > 2 && (pmaAmountOutstandingTextView?.text?.toString() != DEFAULT_RAND_CURRENCY)

            //Disable change button when amount is R0.00
            changeTextView?.isEnabled = pmaAmountOutstandingTextView?.text?.toString() != DEFAULT_RAND_CURRENCY

            // set payment method
            initPaymentMethod()

            // Dismiss popup if payment method list is empty
            if (card?.paymentMethodList?.isEmpty() == true) {
                dismiss()
            }
        })

        initPaymentMethod()
    }

    private fun setupListener() {
        cvvEditTextInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                pmaConfirmPaymentButton?.isEnabled = s.length > 2 && (pmaAmountOutstandingTextView?.text?.toString() != DEFAULT_RAND_CURRENCY)
                if (s.length == 3) {
                    try {
                        val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm?.hideSoftInputFromWindow(cvvEditTextInput.windowToken, 0)
                    } catch (ex: Exception) {
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initPaymentMethod() {
        val paymentMethod = payMyAccountViewModel.getSelectedPaymentMethodCard()
        paymentMethod?.apply {
            cardNumberItemTextView?.text = cardNumber
            changeTextView.text = if (cardExpired) {
                cardExpiredTagTextView?.visibility = VISIBLE
                bindString(R.string.add_card_label)
            } else {
                cardExpiredTagTextView?.visibility = GONE
                bindString(R.string.change_label)
            }

            cardItemImageView?.setImageResource(when (vendor.toLowerCase(Locale.getDefault())) {
                "visa" -> R.drawable.card_visa
                "mastercard" -> R.drawable.card_mastercard
                else -> R.drawable.card_visa_grey
            })
            if (previousCardNumber != cardNumber) {
                cvvEditTextInput?.text?.clear()
            }
            previousCardNumber = cardNumber

        }

        with(pmaConfirmPaymentButton) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@PaymentMethodExistDialogFragment)
        }

        with(changeCardHorizontalView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@PaymentMethodExistDialogFragment)
        }

        with(changeTextView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@PaymentMethodExistDialogFragment)
        }

        with(editAmountImageView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@PaymentMethodExistDialogFragment)
        }


    }

    @SuppressLint("DefaultLocale")
    override fun onClick(v: View?) {
        KotlinUtils.avoidDoubleClicks(v)
        val paymentCard = payMyAccountViewModel.getCardDetail()
        val paymentMethodArgs = payMyAccountViewModel.getPaymentMethodListInStringFormat()
        val account = Gson().toJson(paymentCard?.account)
        val cardInfo = Gson().toJson(paymentCard)
        if (activity is PayMyAccountActivity) {
            when (v?.id) {
                R.id.editAmountImageView -> {
                    sendFirebaseEvent()
                    ScreenManager.presentPayMyAccountActivity(activity, account, paymentMethodArgs, null, cardInfo, true, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
                }

                R.id.changeTextView -> {
                    ScreenManager.presentPayMyAccountActivity(activity, account, paymentMethodArgs, null, cardInfo, true, PayMyAccountStartDestinationType.MANAGE_CARD)
                }

                R.id.pmaConfirmPaymentButton -> {
                    cvvEditTextInput?.text?.toString()?.let { cvvNumber -> payMyAccountViewModel.setCVVNumber(cvvNumber) }
                    val cardResponse = payMyAccountViewModel.createCard()
                    val cardVendorDirections = PaymentMethodExistDialogFragmentDirections.actionDisplayVendorCardDetailFragmentToPMAProcessRequestFragment(payMyAccountViewModel.getAccount(), cardResponse.second)
                    navController?.navigate(cardVendorDirections)
                }
            }
        } else {
            when (v?.id) {
                R.id.editAmountImageView -> {
                    sendFirebaseEvent()
                    ScreenManager.presentPayMyAccountActivity(activity, account, paymentMethodArgs, null, cardInfo, true, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
                }
                R.id.changeTextView -> {
                    if (changeTextView.text.toString().toLowerCase() == bindString(R.string.add_card_label).toLowerCase()) {
                        ScreenManager.presentPayMyAccountActivity(activity, account, paymentMethodArgs, null, cardInfo, true, PayMyAccountStartDestinationType.ADD_NEW_CARD)
                    } else {
                        ScreenManager.presentPayMyAccountActivity(activity, account, paymentMethodArgs, null, cardInfo, true, PayMyAccountStartDestinationType.MANAGE_CARD)
                    }
                }
                R.id.pmaConfirmPaymentButton -> {
                    val cvv = cvvEditTextInput?.text?.toString() ?: "0"
                    with(payMyAccountViewModel) {
                        setCVVNumber(cvv)
                        val (accounts, cardResponse) = createCard()
                        ScreenManager.presentPayMyAccountActivity(activity, Gson().toJson(accounts?.second), paymentMethodArgs, Gson().toJson(cardResponse), cardInfo, PayMyAccountStartDestinationType.SECURE_3D)
                    }
                    dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        queryGetPaymentMethod()
    }

    private fun sendFirebaseEvent() {
        when (payMyAccountViewModel.getAccount()?.productGroupCode?.toLowerCase(Locale.getDefault())) {
            "sc" -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_SC_AMTEDIT)
            "cc" -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_CC_AMTEDIT)
            "pl" -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_PL_AMTEDIT)
        }
    }

    private fun queryGetPaymentMethod() {
        if (!isAdded) return
        ShimmerAnimationManager.startProgress(changeTextViewShimmerLayout)
        payMyAccountViewModel.queryServicePayUPaymentMethod({
            ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)
        }, {
            ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)
        }, {
            ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)
        }, {
            ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)
        })
    }

    companion object {
        const val DEFAULT_RAND_CURRENCY = "R 0.00"
    }
}
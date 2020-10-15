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
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.pma_update_payment_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The dialog is being used by ProductLanding and PayMyAccount Activity
        if (activity is PayMyAccountActivity)
            navController = NavHostFragment.findNavController(this)

        setupListener()

        // Required to stop playing shimmering by default when fragment is visible
        ShimmerAnimationManager.initShimmer(changeTextViewShimmerLayout)
        ShimmerAnimationManager.stopProgress(changeTextViewShimmerLayout)

        with(payMyAccountViewModel) {
            paymentAmountCard.observe(viewLifecycleOwner, { card ->
                if (!isAdded) return@observe

                // Update amount entered
                pmaAmountEnteredTextView?.text = updateAmountEntered(card?.amountEntered)

                // Enable/Disable confirm payment button
                pmaConfirmPaymentButton?.isEnabled = isConfirmPaymentButtonEnabled(cvvEditTextInput.length(), pmaAmountEnteredTextView?.text?.toString())

                //Disable change button when amount is R0.00
                changeTextView?.isEnabled = isChangeIconEnabled(pmaAmountEnteredTextView?.text?.toString())

                // set payment method
                initPaymentMethod()

                // Dismiss popup when payment method list is empty
                if (isPaymentListEmpty(card?.paymentMethodList)) {
                    dismiss()
                }
            })
        }

        initPaymentMethod()
    }

    private fun setupListener() {
        cvvEditTextInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                with(payMyAccountViewModel) {
                    pmaConfirmPaymentButton?.isEnabled = isConfirmPaymentButtonEnabled(s.length, pmaAmountEnteredTextView?.text?.toString())
                    if (isMaxCVVLength(s.length)) {
                        hideKeyboard()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun hideKeyboard() {
        try {
            val imm: InputMethodManager? = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(cvvEditTextInput.windowToken, 0)
        } catch (ex: Exception) {
            Crashlytics.log(ex.message)
        }
    }

    private fun initPaymentMethod() {

        with(payMyAccountViewModel) {

            if (isSelectedCardExpired()) {
                cardExpiredTagTextView?.visibility = VISIBLE
                cardExpiredTagTextView?.text = bindString(R.string.add_card_label)
            } else {
                cardExpiredTagTextView?.visibility = GONE
                cardExpiredTagTextView?.text = bindString(R.string.change_label)
            }

            with(getSelectedPaymentMethodCard()) {
                cardNumberItemTextView?.text = this?.cardNumber
                cardItemImageView?.setImageResource(getVendorCardDrawableId(this?.vendor))

                if (previousCardNumber != this?.cardNumber) {
                    cvvEditTextInput?.text?.clear()
                }
                previousCardNumber = this?.cardNumber

            }
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
                    payMyAccountViewModel.triggerFirebaseEventForEditAmount()
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
                    payMyAccountViewModel.triggerFirebaseEventForEditAmount()
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
}
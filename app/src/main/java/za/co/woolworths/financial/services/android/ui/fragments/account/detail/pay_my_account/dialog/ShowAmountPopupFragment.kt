package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PmaUpdatePaymentFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

class ShowAmountPopupFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: PmaUpdatePaymentFragmentBinding
    private var previousCardNumber: String? = null

    private var navController: NavController? = null
    private val changeCardLabel = bindString(R.string.change_label)
    private val addCardLabel = bindString(R.string.add_card_label)

    companion object {
        const val ONE_RAND = "R1.00"
        const val RAND_AMOUNT_ZERO = "R 0.00"
    }

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogWithoutAnimation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PmaUpdatePaymentFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun setNoteTextsFromConfigIfAvailable() {
        binding.apply {
            AppConfigSingleton.mPayMyAccount?.enterPaymentAmountDialogFooterNote?.let {
                pmaProcessDelayNoteTextView.text = bindString(R.string.pma_payment_delay_dynamic_label, it)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The dialog is being used by ProductLanding and PayMyAccount Activity
        if (activity is PayMyAccountActivity)
            navController = NavHostFragment.findNavController(this)

        with(binding) {
            setNoteTextsFromConfigIfAvailable()
            setupListener()

            with(payMyAccountViewModel) {
                pmaCardPopupModel.observe(viewLifecycleOwner) { card ->
                    if (!isAdded) return@observe

                    card?.amountEntered =
                        if (card?.amountEntered?.contains("R") == true) card.amountEntered else "R ${card?.amountEntered}"

                    when (elitePlanModel?.scope.isNullOrEmpty()) {
                        false -> {
                            //ElitePlan
                            pmaAmountEnteredTextView?.text = getDiscountAmount()
                            savedAmountTextView?.text =
                                getString(R.string.you_saved, getSavedAmount())
                            when (isAmountBelowMaxLimit(elitePlanModel?.settlementAmount)) {
                                true -> {
                                    savedAmountTextView?.visibility = VISIBLE
                                }
                                false -> {
                                    savedAmountTextView?.visibility = GONE
                                }
                            }
                            editAmountImageView?.visibility = GONE
                            tvTitle?.text = getString(R.string.amount_payable)
                        }
                        true -> {
                            //WOP-9291 - Prevent user from paying amount less than R 1. For
                            // this user it has overdue amount as R0.34 so it will populate R1.00 as default amount to pay
                            pmaAmountEnteredTextView?.text =
                                if (convertRandFormatToDouble(card?.amountEntered) in 0.01..0.99) {
                                    getCardDetail()?.amountEntered = ONE_RAND
                                    updateAmountEntered(ONE_RAND)
                                } else {
                                    updateAmountEntered(card?.amountEntered)
                                }
                        }
                    }


                    pmaAmountEnteredTextView?.apply {
                        typeface = Typeface.DEFAULT_BOLD
                        if (!payMyAccountViewModel.isAmountBelowMaxLimit(card?.amountEntered)) {
                            invalidPaymentAmountTextView?.visibility = VISIBLE
                        } else {
                            invalidPaymentAmountTextView?.visibility = GONE
                        }
                    }

                    // Enable/Disable confirm payment button
                    pmaConfirmPaymentButton?.isEnabled = isConfirmPaymentButtonEnabled(
                        cvvEditTextInput.length(),
                        pmaAmountEnteredTextView?.text?.toString()
                    )

                    //Disable change button when amount is R0.00
                    when (isChangeIconEnabled(pmaAmountEnteredTextView?.text?.toString())) {
                        true -> {
                            changeTextView?.alpha = 1.0f
                            changeTextView?.isEnabled = true
                        }
                        false -> {
                            changeTextView?.alpha = 0.3f
                            changeTextView?.isEnabled = false
                        }
                    }

                    cvvFieldEnableState(pmaAmountEnteredTextView?.text?.toString())

                    updateCard(this, card, this@ShowAmountPopupFragment)
                }
            }

            binding.initPaymentMethod()
        }
    }

    private fun PmaUpdatePaymentFragmentBinding.updateCard(
        payMyAccountViewModel1: PayMyAccountViewModel,
        card: PMACardPopupModel?,
        showAmountPopupFragment: ShowAmountPopupFragment
    ) {
        // Dismiss popup when payment method list is empty
        if (payMyAccountViewModel1.isPaymentListEmpty(card?.paymentMethodList))
            dismiss()


        if (payMyAccountViewModel1.isSelectedCardExpired()) {
            cardExpiredTagTextView?.visibility = VISIBLE
            changeTextView?.text = addCardLabel

        } else {
            cardExpiredTagTextView?.visibility = GONE
            changeTextView?.text = changeCardLabel
        }

        with(payMyAccountViewModel1.getSelectedPaymentMethodCard()) {
            showAmountPopupFragment.binding.cardNumberItemTextView?.text = this?.cardNumber
            showAmountPopupFragment.binding.cardItemImageView?.setImageResource(
                payMyAccountViewModel1.getVendorCardDrawableId(
                    this?.vendor
                )
            )

            if (showAmountPopupFragment.previousCardNumber != this?.cardNumber) {
                showAmountPopupFragment.binding.cvvEditTextInput?.text?.clear()
            }
            showAmountPopupFragment.previousCardNumber = this?.cardNumber

        }
    }

    private fun PmaUpdatePaymentFragmentBinding.cvvFieldEnableState(amountPayable: String?) {
        with(payMyAccountViewModel) {
            val isAmountPayableZero = amountPayable == RAND_AMOUNT_ZERO
            cvvEditTextInput?.apply {
                isEnabled = !isAmountPayableZero && !isSelectedCardExpired()
                isFocusable = !isAmountPayableZero && !isSelectedCardExpired()
                isFocusableInTouchMode = !isAmountPayableZero && !isSelectedCardExpired()
            }
        }
    }

    private fun PmaUpdatePaymentFragmentBinding.setupListener() {
        cvvEditTextInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                with(payMyAccountViewModel) {
                    pmaConfirmPaymentButton?.isEnabled = isConfirmPaymentButtonEnabled(
                        s.length,
                        pmaAmountEnteredTextView?.text?.toString()
                    )
                    if (isMaxCVVLength(s.length)) {
                        hideKeyboard()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun PmaUpdatePaymentFragmentBinding.hideKeyboard() {
        try {
            val imm: InputMethodManager? =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(cvvEditTextInput.windowToken, 0)
        } catch (ex: Exception) {
            FirebaseManager.logException(ex)
        }
    }

    private fun PmaUpdatePaymentFragmentBinding.initPaymentMethod() {

        with(payMyAccountViewModel) {

            if (isSelectedCardExpired()) {
                cardExpiredTagTextView?.visibility = VISIBLE
                changeTextView?.text = addCardLabel

            } else {
                cardExpiredTagTextView?.visibility = GONE
                changeTextView?.text = changeCardLabel
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
            setOnClickListener(this@ShowAmountPopupFragment)
        }

        with(changeCardHorizontalView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ShowAmountPopupFragment)
        }

        with(changeTextView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ShowAmountPopupFragment)
        }

        with(editAmountImageView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ShowAmountPopupFragment)
        }

        with(viewOtherPaymentOptionsTextView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ShowAmountPopupFragment)
        }

        viewOtherPaymentOptionsTextView?.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    @SuppressLint("DefaultLocale")
    override fun onClick(v: View?) {
        KotlinUtils.avoidDoubleClicks(v)
        with(payMyAccountViewModel) {
            val cardInfo = getCardDetail()
            if (activity is PayMyAccountActivity) {
                when (v?.id) {

                    R.id.editAmountImageView -> {
                        triggerFirebaseEventForEditAmount(activity as PayMyAccountActivity)
                        ActivityIntentNavigationManager.presentPayMyAccountActivity(
                            activity,
                            cardInfo,
                            PayMyAccountStartDestinationType.PAYMENT_AMOUNT,
                            true
                        )
                    }

                    R.id.changeTextView -> {
                        if (isAmountBelowMaxLimit(elitePlanModel?.settlementAmount)) {
                            ActivityIntentNavigationManager.presentPayMyAccountActivity(
                                activity,
                                cardInfo,
                                PayMyAccountStartDestinationType.MANAGE_CARD,
                                true,
                                payMyAccountViewModel.elitePlanModel
                            )
                        }
                    }

                    R.id.pmaConfirmPaymentButton -> {
                        setCVVNumber(binding.cvvEditTextInput?.text?.toString())
                        navController?.navigate(R.id.action_displayVendorCardDetailFragment_to_PMAProcessRequestFragment)
                    }

                    R.id.viewOtherPaymentOptionsTextView -> dismiss()

                    else -> return@with
                }
            } else {
                when (v?.id) {
                    R.id.editAmountImageView -> {
                        activity?.let { triggerFirebaseEventForEditAmount(it) }
                        ActivityIntentNavigationManager.presentPayMyAccountActivity(
                            activity,
                            cardInfo,
                            PayMyAccountStartDestinationType.PAYMENT_AMOUNT,
                            true
                        )
                    }
                    R.id.changeTextView -> {
                        if (isAmountBelowMaxLimit(elitePlanModel?.settlementAmount)) {
                            if (binding.changeTextView.text.toString()
                                    .equals(bindString(R.string.add_card_label), ignoreCase = true)
                            ) {
                                ActivityIntentNavigationManager.presentPayMyAccountActivity(
                                    activity,
                                    cardInfo,
                                    PayMyAccountStartDestinationType.ADD_NEW_CARD,
                                    true
                                )
                            } else {
                                ActivityIntentNavigationManager.presentPayMyAccountActivity(
                                    activity,
                                    cardInfo,
                                    PayMyAccountStartDestinationType.MANAGE_CARD,
                                    true
                                )
                            }
                        }

                    }

                    R.id.viewOtherPaymentOptionsTextView -> {
                        ActivityIntentNavigationManager.presentPayMyAccountActivity(
                            activity,
                            getCardDetail()
                        )
                        dismiss()
                    }

                    R.id.pmaConfirmPaymentButton -> {
                        setCVVNumber(binding.cvvEditTextInput?.text?.toString())
                        ActivityIntentNavigationManager.presentPayMyAccountActivity(
                            activity,
                            cardInfo,
                            PayMyAccountStartDestinationType.SECURE_3D,
                            true
                        )
                        dismiss()
                    }
                    else -> return@with
                }
            }
        }
    }
}
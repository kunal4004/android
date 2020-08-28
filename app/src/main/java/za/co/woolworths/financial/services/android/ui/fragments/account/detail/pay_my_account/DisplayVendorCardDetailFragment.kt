package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_update_payment_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import java.util.*

class DisplayVendorCardDetailFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var paymentMethodArgs: String? = null
    private var mAccounts: String? = null
    val args: DisplayVendorCardDetailFragmentArgs by navArgs()
    private var paymentMethodList: MutableList<GetPaymentMethod>? = null
    private var root: View? = null

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var navController: NavController? = null

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
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

        paymentMethodArgs = args.paymentMethod
        mAccounts = args.accounts

        if (activity is PayMyAccountActivity)
            navController = NavHostFragment.findNavController(this)

        paymentMethodList = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethodArgs, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        val accountArgs = Gson().fromJson(mAccounts, Account::class.java)
        val amountOverdue = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(accountArgs?.amountOverdue
                ?: 0), 1, activity))
        setupListener()

        payMyAccountViewModel.amountEntered.observe(viewLifecycleOwner, { amountEntered ->
            pmaAmountOutstandingTextView?.text = if (amountEntered.isNullOrEmpty()) amountOverdue else amountEntered
            pmaConfirmPaymentButton.isEnabled = ccvEditTextInput?.length() ?: 0 > 2 && (pmaAmountOutstandingTextView?.text?.toString() != "R 0.00")
        })

        payMyAccountViewModel.paymentMethodList?.observe(viewLifecycleOwner, {
            initPaymentMethod()
        })

        initPaymentMethod()
        pmaAmountOutstandingTextView?.text = payMyAccountViewModel.getAmountEntered()
    }

    private fun setupListener() {
        ccvEditTextInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                pmaConfirmPaymentButton?.isEnabled = s.length > 2 && (pmaAmountOutstandingTextView?.text?.toString() != "R 0.00")
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initPaymentMethod() {
        val paymentMethod = payMyAccountViewModel.getSelectedPaymentMethodCard()
        paymentMethod?.apply {
            cardNumberItemTextView?.text = cardNumber
            cardItemImageView?.setImageResource(when (vendor.toLowerCase(Locale.getDefault())) {
                "visa" -> R.drawable.card_visa
                "mastercard" -> R.drawable.card_mastercard
                else -> R.drawable.card_visa_grey
            })
        }

        with(pmaConfirmPaymentButton) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }

        with(changeCardHorizontalView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }

        with(changeTextView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }

        with(editAmountImageView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }
    }

    override fun onClick(v: View?) {
        val amountEntered = pmaAmountOutstandingTextView?.text?.toString()
        paymentMethodArgs = Gson().toJson(payMyAccountViewModel.getPaymentMethodList())
        if (activity is PayMyAccountActivity) {
            when (v?.id) {
                R.id.editAmountImageView -> {
                    ScreenManager.presentPayMyAccountActivity(activity, mAccounts, paymentMethodArgs, null, amountEntered, true, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
                }

                R.id.changeTextView -> {
                    ScreenManager.presentPayMyAccountActivity(activity, mAccounts, paymentMethodArgs, null, amountEntered, true, PayMyAccountStartDestinationType.MANAGE_CARD)
                }

                R.id.pmaConfirmPaymentButton -> {
                    val (account, cardResponse) = payMyAccountViewModel.createCard()
                    val cardVendorDirections = DisplayVendorCardDetailFragmentDirections.actionDisplayVendorCardDetailFragmentToPMAProcessRequestFragment(account?.second, cardResponse)
                    navController?.navigate(cardVendorDirections)
                }
            }
        } else {
            when (v?.id) {
                R.id.editAmountImageView -> {
                    ScreenManager.presentPayMyAccountActivity(activity, mAccounts, paymentMethodArgs, null, amountEntered, true, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
                }
                R.id.changeTextView -> {
                    ScreenManager.presentPayMyAccountActivity(activity, mAccounts, paymentMethodArgs, null, amountEntered, true, PayMyAccountStartDestinationType.MANAGE_CARD)
                }
                R.id.pmaConfirmPaymentButton -> {
                    val cvv = ccvEditTextInput?.text?.toString() ?: "0"
                    with(payMyAccountViewModel) {
                        setCVVNumber(cvv)
                        val (accounts, cardResponse) = createCard()
                        ScreenManager.presentPayMyAccountActivity(activity, Gson().toJson(accounts?.second), paymentMethodArgs, Gson().toJson(cardResponse), amountEntered, PayMyAccountStartDestinationType.SECURE_3D)
                    }
                    dismiss()
                }
            }
        }
    }
}
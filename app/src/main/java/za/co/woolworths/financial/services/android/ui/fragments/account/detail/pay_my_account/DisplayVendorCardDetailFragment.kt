package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_update_payment_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.models.dto.PMACard
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import java.util.*


class DisplayVendorCardDetailFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var mAccounts: String? = null
    val args: DisplayVendorCardDetailFragmentArgs by navArgs()
    private var paymentMethodList: MutableList<GetPaymentMethod>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_update_payment_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentMethodArgs = args.paymentMethod
        mAccounts = args.accounts
        paymentMethodList = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethodArgs, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        val accountArgs = Gson().fromJson<Account>(mAccounts, Account::class.java)
        val amountOverdue = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(accountArgs?.amountOverdue
                ?: 0), 1, activity))
        pmaAmountOutstandingTextView?.text = amountOverdue
        initPaymentMethod(paymentMethodList)
        setupListener()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("amountEntered")?.observe(viewLifecycleOwner) { amountEntered ->
            pmaAmountOutstandingTextView?.text = amountEntered
        }

    }

    private fun setupListener() {
        ccvEditTextInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                pmaConfirmPaymentButton?.isEnabled = s.length > 2
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initPaymentMethod(paymentMethodList: MutableList<GetPaymentMethod>?) {
        paymentMethodList?.get(0)?.apply {
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
        when (v?.id) {
            R.id.editAmountImageView -> ScreenManager.presentPayMyAccountActivity(activity, mAccounts, args.paymentMethod, null, amountEntered, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
            R.id.changeTextView -> ScreenManager.presentPayMyAccountActivity(activity, mAccounts, args.paymentMethod, null, amountEntered, PayMyAccountStartDestinationType.MANAGE_CARD)
            R.id.pmaConfirmPaymentButton -> {
                val paymentMethod = paymentMethodList?.get(0)
                val cvv = ccvEditTextInput?.text?.toString() ?: "0"

                val accounts = Gson().fromJson<Pair<ApplyNowState, Account>>(mAccounts, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)

                val pmaCard = PMACard(paymentMethod?.cardNumber
                        ?: "", "", paymentMethod?.expirationDate
                        ?: "", paymentMethod?.expirationDate ?: "", cvv, 1, paymentMethod?.vendor
                        ?: "", paymentMethod?.type
                        ?: "")

                val cardResponse = AddCardResponse(paymentMethod?.token ?: "", pmaCard, true)
                ScreenManager.presentPayMyAccountActivity(activity, Gson().toJson(accounts.second), args.paymentMethod, Gson().toJson(cardResponse), amountEntered, PayMyAccountStartDestinationType.SECURE_3D)
            }
        }
    }
}
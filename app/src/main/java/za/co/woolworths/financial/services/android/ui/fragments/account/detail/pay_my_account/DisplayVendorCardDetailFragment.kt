package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_update_payment_fragment.*
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import java.util.*

class DisplayVendorCardDetailFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    val args: DisplayVendorCardDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_update_payment_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentMethodArgs = args.paymentMethod
        val paymentMethodList: MutableList<GetPaymentMethod>? = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethodArgs, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)

        initPaymentMethod(paymentMethodList)
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

        with(manageDebitCreditCardItem) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }

        with(editAmountImageView) {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@DisplayVendorCardDetailFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.editAmountImageView -> {
                ScreenManager.presentPayMyAccountActivity(activity,args.accounts, args.paymentMethod, PayMyAccountStartDestinationType.PAYMENT_AMOUNT)
            }

            R.id.manageDebitCreditCardItem -> ScreenManager.presentPayMyAccountActivity(activity,args.accounts, args.paymentMethod, PayMyAccountStartDestinationType.MANAGE_CARD)

            R.id.pmaConfirmPaymentButton -> { }

        }
    }
}
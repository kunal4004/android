package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_cancel_delivery_confirmation_dialog.cancel
import kotlinx.android.synthetic.main.credit_card_setup_delivery_now.*
import kotlinx.android.synthetic.main.credit_card_setup_delivery_now.title
import za.co.woolworths.financial.services.android.contracts.ISetUpDeliveryNowLIstner
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class SetUpDeliveryNowDialog() : WBottomSheetDialogFragment(), View.OnClickListener {

    private var deliveredToName: String? = ""
    var mSetUpDeliveryListner: ISetUpDeliveryNowLIstner? = null
    var accountBinNumber: String? = null

    constructor(bundle: Bundle, mSetUpDeliveryListner: ISetUpDeliveryNowLIstner?) : this() {
        bundle.apply {
            accountBinNumber = getString("accountBinNumber")
        }
        this.mSetUpDeliveryListner = mSetUpDeliveryListner
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.credit_card_setup_delivery_now, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        deliveredToName = SessionUtilities.getInstance().getJwt().name.get(0)
        var creditCardName: String = bindString(R.string.blackCreditCard_title)
        if (accountBinNumber.equals(Utils.GOLD_CARD, true)) {
            cardImage.setImageDrawable(bindDrawable(R.drawable.w_gold_credit_card))
            creditCardName = bindString(R.string.goldCreditCard_title)
        } else if (accountBinNumber.equals(Utils.SILVER_CARD, true)) {
            cardImage.setImageDrawable(bindDrawable(R.drawable.w_silver_credit_card))
            creditCardName = bindString(R.string.silverCreditCard_title)
        } else if (accountBinNumber.equals(Utils.BLACK_CARD, true)) {
            cardImage.setImageDrawable(bindDrawable(R.drawable.w_black_credit_card))
            creditCardName = bindString(R.string.blackCreditCard_title)
        }

        cancel.setOnClickListener(this)
        setUpDeliveryNow.setOnClickListener(this)
        val nameTitleText1 = "Hey\u0020"
        title.text = nameTitleText1.plus(deliveredToName).plus(bindString(R.string.title_subdesc_setup_cc_delivery).plus(creditCardName).plus("?"))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> {
                dismiss()
            }
            R.id.setUpDeliveryNow -> {
                mSetUpDeliveryListner?.onSetUpDeliveryNowButtonClick()
                dismiss()
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_linked_item.view.*
import za.co.woolworths.financial.services.android.contracts.IAccountCardItemListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.removeNegativeSymbol
import za.co.woolworths.financial.services.android.util.WFormatter
import java.util.*

class AccountProductLinkedItemViewHolder(parent: ViewGroup) : WParentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.account_linked_item, parent, false)) {

    fun setAccountContent(account: Account?, accountCardItemListener: IAccountCardItemListener?) {
        with(itemView) {
            account?.apply {
                accountLoanStatusIndicatorImageView?.visibility = if (productOfferingGoodStanding) View.GONE else View.VISIBLE
                accountProductAmountTextView?.setTextColor(ContextCompat.getColor(context, if (productOfferingGoodStanding) R.color.black else R.color.black30))
                accountProductAmountTextView?.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(availableFunds), 1, context)))

                when (productGroupCode?.toLowerCase(Locale.getDefault())) {
                    "sc" -> {
                        linkAccountCardRelativeLayout?.contentDescription = context?.resources?.getString(R.string.linked_store_card_layout)
                        accountProductTitleTextView?.text = context?.resources?.getString(R.string.store_card)
                        accountProductImageView?.setImageResource(R.drawable.small_2)
                    }
                    "cc" -> {
                        linkAccountCardRelativeLayout?.contentDescription = context?.resources?.getString(R.string.linked_credit_card_layout);
                        accountProductTitleTextView?.text = context?.resources?.getString(R.string.credit_card)
                        when (account.accountNumberBin) {
                            Utils.SILVER_CARD -> accountProductImageView.setBackgroundResource(R.drawable.small_5)
                            Utils.GOLD_CARD -> accountProductImageView.setBackgroundResource(R.drawable.small_4)
                            Utils.BLACK_CARD -> accountProductImageView.setBackgroundResource(R.drawable.small_3)
                            else -> throw IllegalStateException("Unknown accountNumberBin found $this")
                        }
                    }
                    "pl" -> {
                        linkAccountCardRelativeLayout?.contentDescription = context?.resources?.getString(R.string.linked_personal_loan_layout);
                        accountProductTitleTextView?.text = context?.resources?.getString(R.string.personal_loan)
                        accountProductImageView?.setImageResource(R.drawable.small)
                    }
                    else -> throw IllegalStateException("Unknown productGroupCode found $this")
                }
            }

            setOnClickListener {
                account?.apply {
                    val productGroupCode = productGroupCode?.toLowerCase(Locale.getDefault()) ?: ""
                    accountCardItemListener?.onLinkedAccountClicked(productGroupCode)
                }
            }
        }
    }
}
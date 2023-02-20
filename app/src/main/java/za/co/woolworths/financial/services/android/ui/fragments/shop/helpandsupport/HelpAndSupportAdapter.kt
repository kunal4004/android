package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutHelpAndSupportItemCellBinding

class HelpAndSupportAdapter(
    val context: Context?,
    var helpAndSupportList: ArrayList<HelpAndSupport>,
    var helpAndSupportListener: HelpAndSupportClickListener
) : RecyclerView.Adapter<HelpAndSupportAdapter.HelpAndSupportViewHolder>() {

   inner class HelpAndSupportViewHolder(val itemBinding: LayoutHelpAndSupportItemCellBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(position: Int) {
            itemBinding.apply {
                val item = helpAndSupportList.get(position)
                imgHelpIcon?.setImageResource(item.icon)
                txtHelpTitle?.text = item.title
                txtHelpSubTitle?.visibility =
                    if (item.subTitle?.isNullOrEmpty() == true)
                        View.GONE
                    else
                        View.VISIBLE
                txtHelpSubTitle?.text = item.subTitle
                root.setOnClickListener {
                    when {
                        txtHelpTitle?.text.toString()
                            .equals(context?.getString(R.string.dash_call_customer_care), true) -> {
                            helpAndSupportListener.openCallSupport(txtHelpSubTitle?.text.toString())
                        }
                        txtHelpTitle?.text.toString()
                            .equals(context?.getString(R.string.cancel_order), true) -> {
                            helpAndSupportListener.onCancelOrder()
                        }
                        txtHelpTitle?.text.toString()
                            .equals(context?.getString(R.string.dash_send_us_an_email), true) -> {
                            helpAndSupportListener.openEmailSupport(txtHelpSubTitle?.text.toString())
                        }
                        context?.getString(R.string.dash_Chat_to_your_shopper)?.let { it ->
                            txtHelpTitle?.text.toString().startsWith(
                                it,
                                true
                            )
                        } == true -> {
                            helpAndSupportListener.openChatSupport()
                        }
                        txtHelpTitle?.text.toString()
                            .equals(context?.getString(R.string.dash_track_your_order), true) -> {
                            helpAndSupportListener.openTrackYourOrder()
                        }
                        txtHelpTitle?.text.toString()
                            .equals(context?.getString(R.string.view_tax_invoice), true) -> {
                            helpAndSupportListener.openTaxInvoice()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpAndSupportViewHolder {
        return HelpAndSupportViewHolder(
            LayoutHelpAndSupportItemCellBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HelpAndSupportViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return helpAndSupportList.size
    }


    interface HelpAndSupportClickListener {
        fun openCallSupport(contactNumber: String)
        fun onCancelOrder()
        fun openEmailSupport(emailId: String)
        fun openChatSupport()
        fun openTrackYourOrder()
        fun openTaxInvoice()
        fun onCancelOrderConfirmation()
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_help_and_support_item_cell.view.*

class HelpAndSupportAdapter(
    val context: Context?,
    var helpAndSupportList: ArrayList<HelpAndSupport>,
    var helpAndSupportListener: HelpAndSupportClickListener
) : RecyclerView.Adapter<HelpAndSupportAdapter.HelpAndSupportViewHolder>() {

   inner class HelpAndSupportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = helpAndSupportList.get(position)
            itemView.imgHelpIcon?.setImageResource(item.icon)
            itemView.txtHelpTitle?.text = item.title
            itemView.txtHelpSubTitle?.visibility =
                if (item.subTitle?.isNullOrEmpty() == true)
                    View.GONE
                else
                    View.VISIBLE
            itemView.txtHelpSubTitle?.text = item.subTitle
            itemView.setOnClickListener {
                if (itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.dash_call_customer_care), true)) {
                    helpAndSupportListener.openCallSupport(itemView?.txtHelpSubTitle?.text.toString())
                }
                else if(itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.cancel_order), true)){
                    helpAndSupportListener.onCancelOrder()
                }
                else if(itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.dash_send_us_an_email), true)){
                    helpAndSupportListener.openEmailSupport(itemView?.txtHelpSubTitle?.text.toString())
                }
                else if(itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.dash_Chat_to_your_shopper), true)){
                    helpAndSupportListener.openChatSupport()
                }
                else if(itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.dash_track_your_order), true)){
                    helpAndSupportListener.openTrackYourOrder()
                }
                else if(itemView.txtHelpTitle?.text.toString().equals(context?.getString(R.string.view_tax_invoice), true)){
                    helpAndSupportListener.openTaxInvoice()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpAndSupportViewHolder {
        return HelpAndSupportViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_help_and_support_item_cell, parent, false)
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
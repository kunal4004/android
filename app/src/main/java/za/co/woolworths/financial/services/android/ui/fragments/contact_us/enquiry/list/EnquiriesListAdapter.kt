package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.EnquiriesListItemBinding
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions

class EnquiriesListAdapter(
    private val listener: ItemListener,
    contactUsFinancialServicesEmail: List<ConfigOptions>,
    selectedIndex: Int?
) :
    RecyclerView.Adapter<ContactUsFinancialServiceViewHolder>() {
    init {
        lastSelectedPosition = selectedIndex?: -1
    }

    interface ItemListener {
        fun onSelectEnquiry(pos: Int)
    }

    private val items = contactUsFinancialServicesEmail

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactUsFinancialServiceViewHolder {
        val binding: EnquiriesListItemBinding =
            EnquiriesListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactUsFinancialServiceViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ContactUsFinancialServiceViewHolder, position: Int) =
        holder.bind(items[position])
}
var lastSelectedPosition = -1

class ContactUsFinancialServiceViewHolder(
    private val itemBinding: EnquiriesListItemBinding,
    private val listener: EnquiriesListAdapter.ItemListener
) : RecyclerView.ViewHolder(itemBinding.root),
    View.OnClickListener {
    private lateinit var item: ConfigOptions

    init {
        itemBinding.root.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: ConfigOptions) {
        this.item = item
        itemBinding.radioEnquiresList.text = item.displayName
        itemBinding.radioEnquiresList.isChecked = lastSelectedPosition == adapterPosition
    }

    override fun onClick(v: View?) {
        lastSelectedPosition = adapterPosition
        listener.onSelectEnquiry(adapterPosition)
    }
}
package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.address_row_layout.view.*
import kotlinx.android.synthetic.main.address_row_layout.view.tvAddress
import kotlinx.android.synthetic.main.address_row_layout.view.tvAddressNickName
import za.co.woolworths.financial.services.android.checkout.service.network.Address

class SavedAddressAdapter(
    val context: Context,
    val addressList: ArrayList<Address>,
    val defaultAddressNickName: String?,
    val listener: OnAddressSelected
) : RecyclerView.Adapter<SavedAddressAdapter.SavedAddressViewHolder>() {

     var selectedPosition = -1
     var addressSelected = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedAddressViewHolder {
        return SavedAddressViewHolder(
            LayoutInflater.from(context).inflate(R.layout.address_row_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: SavedAddressViewHolder, position: Int) {
        holder.tvAddressNickname.text = addressList[position].nickname

        if (addressList[position].nickname.equals(defaultAddressNickName, true)
            && !addressSelected) {
            selectedPosition = position
            holder.rbAddressSelector?.isChecked = true
            holder.view?.setBackgroundResource(R.drawable.bg_select_store)
            listener.onAddressSelected(addressList[position], position)
        } else {
            holder.rbAddressSelector?.isChecked = false
            holder.view?.setBackgroundResource(R.color.white)
        }

        showSelectedAddress(position, holder)

        holder.imgEditAddress?.visibility =
            if (selectedPosition == position && addressList[position].verified)
                View.VISIBLE
            else
                View.GONE

        holder.view.setOnClickListener {
            addressSelected = true
            selectedPosition = position
            listener.onAddressSelected(addressList[position], position)
            notifyDataSetChanged()
        }
        holder.imgEditAddress?.setOnClickListener {
            listener.onEditAddress(addressList[position], position)
        }

        holder.tvUpdateAddress.visibility =
            if (addressList[position].verified)
                View.GONE
            else
                View.VISIBLE

        holder.tvAddress?.text = addressList[position].address1
    }

    private fun showSelectedAddress(position: Int, holder: SavedAddressViewHolder) {
        if (selectedPosition == position) {
            holder.rbAddressSelector?.isChecked = true
            holder.view?.setBackgroundResource(R.drawable.bg_select_store)
        } else {
            holder.rbAddressSelector?.isChecked = false
            holder.view?.setBackgroundResource(R.color.white)
        }
    }

    inner class SavedAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvAddressNickname = itemView.tvAddressNickName
        val view = itemView
        val tvAddress = itemView.tvAddress
        val tvUpdateAddress = itemView.tvUpdateAddress
        val rbAddressSelector = itemView.rbAddressSelector
        val imgEditAddress = itemView.imgEditAddress
    }

    interface OnAddressSelected {
        fun onAddressSelected(address: Address, position: Int)
        fun onEditAddress(address: Address, position: Int)
    }
}
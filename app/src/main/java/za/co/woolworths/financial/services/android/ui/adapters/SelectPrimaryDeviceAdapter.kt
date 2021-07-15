package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_select_primary_device_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice

class SelectPrimaryDeviceAdapter(val deviceList: ArrayList<UserDevice>,
                                 val onClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class DeviceListViewType(val value: Int) { PRIMARY_DEVICE(0), OTHER_DEVICE(1) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return SelectPrimaryDeviceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_select_primary_device_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is SelectPrimaryDeviceViewHolder -> {
                holder.bind(deviceList[position])
            }
        }
    }

    override fun getItemCount(): Int = if(deviceList.isNullOrEmpty()) 0 else deviceList.filter { it.primarydDevice != true }.size

    override fun getItemViewType(position: Int): Int =
            if (position != deviceList.size && deviceList[position].primarydDevice == true)
                DeviceListViewType.PRIMARY_DEVICE.value else DeviceListViewType.OTHER_DEVICE.value


    inner class SelectPrimaryDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(device: UserDevice) {
            itemView.apply {
                itemView.deviceNameTextView?.text = device.deviceName
                itemView.deviceSubtitleTextView?.text =
                    context.getString(R.string.view_all_device_linked_on, device.linkedDate)
                itemView.deviceLocationTextView?.text =
                    if (TextUtils.isEmpty(device.locationLinked))
                        context.getString(R.string.view_all_device_location_n_a)
                    else device.locationLinked
                itemView.selectPrimaryDeviceConstraintLayout?.setTag(
                    R.id.selectPrimaryDeviceConstraintLayout,
                    device
                )
                itemView.selectPrimaryDeviceConstraintLayout?.setOnClickListener(onClickListener)
            }
        }
    }
}
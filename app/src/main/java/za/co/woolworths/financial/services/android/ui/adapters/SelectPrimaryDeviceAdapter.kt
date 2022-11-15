package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ItemSelectPrimaryDeviceLayoutBinding
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice

class SelectPrimaryDeviceAdapter(val deviceList: ArrayList<UserDevice>,
                                 val onRowSelected: ISelectPrimaryDeviceClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ISelectPrimaryDeviceClickListener {
        fun onPrimaryDeviceClicked(position: Int, isChecked: Boolean)
    }

    enum class DeviceListViewType(val value: Int) { PRIMARY_DEVICE(0), OTHER_DEVICE(1) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectPrimaryDeviceViewHolder(
            ItemSelectPrimaryDeviceLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is SelectPrimaryDeviceViewHolder -> {
                holder.bind(position)
            }
        }
    }

    override fun getItemCount(): Int = if(deviceList.isNullOrEmpty()) 0 else deviceList.size

    override fun getItemViewType(position: Int): Int =
            if (position != deviceList.size && deviceList[position].primarydDevice == true)
                DeviceListViewType.PRIMARY_DEVICE.value else DeviceListViewType.OTHER_DEVICE.value


    inner class SelectPrimaryDeviceViewHolder(val itemBinding: ItemSelectPrimaryDeviceLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(position: Int) {
            val device: UserDevice = deviceList[position]
            itemBinding.apply {
                deviceNameTextView?.text = device.deviceName
                deviceSubtitleTextView?.text =
                    root.context.getString(R.string.view_all_device_linked_on, device.linkedDate)
                deviceLocationTextView?.text =
                    if (TextUtils.isEmpty(device.locationLinked))
                        root.context.getString(R.string.view_all_device_location_n_a)
                    else device.locationLinked
                selectPrimaryDeviceConstraintLayout?.setTag(
                    R.id.selectPrimaryDeviceConstraintLayout,
                    position
                )
                deviceRadioButton.isChecked = device.primarydDevice == true
                selectPrimaryDeviceConstraintLayout?.setOnClickListener {
                    deviceRadioButton.isChecked = !deviceRadioButton.isChecked
                    onRowSelected.onPrimaryDeviceClicked(position, deviceRadioButton.isChecked)
                }
            }
        }
    }
}
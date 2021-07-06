package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_select_primary_device_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice

class SelectPrimaryDeviceAdapter(val context: Context, val onClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class DeviceListViewType(val value: Int) { PRIMARY_DEVICE(0), OTHER_DEVICE(1) }
    private var deviceList: List<UserDevice>? = ArrayList(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return SelectPrimaryDeviceViewHolder(LayoutInflater.from(context).inflate(R.layout.item_select_primary_device_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is SelectPrimaryDeviceViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = if(deviceList.isNullOrEmpty()) 0 else deviceList!!.filter { it.primarydDevice != true }.size

    override fun getItemViewType(position: Int): Int =
            if (position != deviceList?.size && deviceList?.get(position)?.primarydDevice == true) DeviceListViewType.PRIMARY_DEVICE.value else DeviceListViewType.OTHER_DEVICE.value

    fun setDeviceList(data: List<UserDevice>?) {
        deviceList = ArrayList(0)
        if (data != null) {
            deviceList = data.filter { it.primarydDevice != true }
        }
        notifyDataSetChanged()
    }

    inner class SelectPrimaryDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                deviceList?.forEach {
                    itemView.deviceNameTextView?.text = it.deviceName
                    itemView.deviceSubtitleTextView?.text =
                        context.getString(R.string.view_all_device_linked_on, it.linkedDate)
                    itemView.deviceLocationTextView?.text =
                        if (TextUtils.isEmpty(it.locationLinked))
                            context.getString(R.string.view_all_device_location_n_a)
                        else it.locationLinked
                    itemView.selectPrimaryDeviceConstraintLayout?.setTag(
                        R.id.selectPrimaryDeviceConstraintLayout,
                        it
                    )
                    itemView.selectPrimaryDeviceConstraintLayout?.setOnClickListener(onClickListener)
                }
            }
        }
    }
}
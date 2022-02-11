package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_view_all_linked_device_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import java.net.URLDecoder

class ViewAllLinkedDevicesAdapter(val context: Context, val onClickListener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class DeviceListViewType(val value: Int) { PRIMARY_DEVICE(0), OTHER_DEVICE(1) }
    private var deviceList: ArrayList<UserDevice>? = ArrayList(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            DeviceListViewType.PRIMARY_DEVICE.value -> {
                PrimaryDeviceViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_all_linked_device_layout, parent, false))
            }
            DeviceListViewType.OTHER_DEVICE.value -> {
                OtherDevicesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_all_linked_device_layout, parent, false))
            }
            else -> {
                OtherDevicesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_all_linked_device_layout, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is PrimaryDeviceViewHolder -> {
                holder.bind()
            }
            is OtherDevicesViewHolder -> {
                holder.bind()
            }
        }
    }

    // Two items Primary device and other devices
    override fun getItemCount(): Int = if(deviceList.isNullOrEmpty()) 0 else if (getPrimaryDevice() == null || deviceList?.size == 1) 1 else 2

    override fun getItemViewType(position: Int): Int =
            if (position != deviceList?.size && deviceList?.get(position)?.primarydDevice == true) DeviceListViewType.PRIMARY_DEVICE.value else DeviceListViewType.OTHER_DEVICE.value

    fun setDeviceList(data: ArrayList<UserDevice>?) {
        deviceList = ArrayList(0)
        deviceList = data ?: ArrayList(0)
        notifyDataSetChanged()
    }

    inner class PrimaryDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                val primaryDevice = getPrimaryDevice()
                viewAllDevicesTitleTextView?.text = context?.getString(R.string.view_all_primary_device_title)
                viewAllDeviceNameTextView?.text = URLDecoder.decode(primaryDevice?.deviceName, "UTF8")
                viewAllDeviceLocationTextView?.text = if (TextUtils.isEmpty(primaryDevice?.locationLinked)) context.getString(R.string.view_all_device_location_n_a) else primaryDevice?.locationLinked
                viewAllDeviceSubtitleTextView?.text = context.getString(R.string.view_all_device_linked_on, primaryDevice?.linkedDate)
                viewAllDeviceDeleteImageView?.visibility =  View.VISIBLE
                viewAllDeviceEditImageView?.visibility =  View.GONE
                viewAllDeviceDeleteImageView?.setTag(R.id.viewAllDeviceDeleteImageView, primaryDevice)
                viewAllDeviceDeleteImageView?.setOnClickListener(onClickListener)
            }
        }
    }

    private fun getPrimaryDevice(): UserDevice? {
        deviceList?.forEach {
            if (it.primarydDevice == true)
                return it
        }
        return null
    }

    inner class OtherDevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                var isDisplayedTitle = false
                val layoutInflater = LayoutInflater.from(context)
                deviceList?.forEach {

                    val listItem = layoutInflater.inflate(R.layout.item_view_all_linked_device_layout, null, false)
                    if (it.primarydDevice == false) {
                        if (isDisplayedTitle) {
                            listItem.viewAllDevicesTitleGroup?.visibility = View.GONE
                        } else {
                            isDisplayedTitle = true
                            listItem.viewAllDevicesTitleGroup?.visibility = View.VISIBLE
                            listItem.viewAllDevicesTitleTextView?.text = context?.getString(R.string.view_all_other_device_title)
                        }
                        listItem.viewAllDeviceNameTextView?.text = URLDecoder.decode(it.deviceName, "UTF8")
                        listItem.viewAllDeviceSubtitleTextView?.text = context.getString(R.string.view_all_device_linked_on, it.linkedDate)
                        listItem.viewAllDeviceLocationTextView?.text = if(TextUtils.isEmpty(it.locationLinked)) context.getString(R.string.view_all_device_location_n_a) else it.locationLinked
                        listItem.viewAllDeviceDeleteImageView?.visibility =  View.GONE
                        listItem.viewAllDeviceEditImageView?.visibility =  View.VISIBLE
                        listItem.viewAllDeviceEditImageView?.setTag(R.id.viewAllDeviceEditImageView, it)
                        listItem.viewAllDeviceEditImageView?.setOnClickListener(onClickListener)
                        itemView.viewAllOtherDevicesContainer.addView(listItem)
                    }
                }
            }
        }
    }
}
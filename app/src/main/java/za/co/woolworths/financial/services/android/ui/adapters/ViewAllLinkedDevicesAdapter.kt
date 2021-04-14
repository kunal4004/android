package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_view_all_linked_device_layout.view.*
import kotlinx.android.synthetic.main.layout_credit_report_privacy_policy.view.*
import kotlinx.android.synthetic.main.layout_credit_report_privacy_policy_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.util.KotlinUtils

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
    override fun getItemCount(): Int = if (getPrimaryDevice() == null) 1 else 2

    override fun getItemViewType(position: Int): Int =
            if (deviceList?.get(position)?.primarydDevice == true) DeviceListViewType.PRIMARY_DEVICE.value else DeviceListViewType.OTHER_DEVICE.value

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
                viewAllDeviceNameTextView?.text = primaryDevice?.deviceName
                viewAllDeviceLocationTextView?.text = if(TextUtils.isEmpty(primaryDevice?.locationLinked)) context.getString(R.string.view_all_device_location_n_a) else primaryDevice?.locationLinked
                viewAllDeviceSubtitleTextView?.text = context.getString(R.string.view_all_device_linked_on, primaryDevice?.linkedDate)
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
                        listItem.viewAllDeviceNameTextView?.text = it.deviceName
                        listItem.viewAllDeviceSubtitleTextView?.text = context.getString(R.string.view_all_device_linked_on, it.linkedDate)
                        listItem.viewAllDeviceLocationTextView?.text = if(TextUtils.isEmpty(it.locationLinked)) context.getString(R.string.view_all_device_location_n_a) else it.locationLinked
                        listItem.viewAllDeviceDeleteImageView?.setTag(R.id.viewAllDeviceDeleteImageView, it)
                        listItem.viewAllDeviceDeleteImageView?.setOnClickListener(onClickListener)
                        itemView.viewAllOtherDevicesContainer.addView(listItem)
                    }
                }
            }
        }
    }
}
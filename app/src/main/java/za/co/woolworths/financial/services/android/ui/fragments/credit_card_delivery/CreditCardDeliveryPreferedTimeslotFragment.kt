package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.credit_card_delivery_prefered_time_slots_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.TimeSlot
import za.co.woolworths.financial.services.android.util.picker.WheelView


class CreditCardDeliveryPreferedTimeslotFragment : Fragment(), WheelView.OnItemSelectedListener<Any> {

    var bundle: Bundle? = null
    var timeslots: List<TimeSlot>? = null
    var selectedDate: TimeSlot? = null
    var selectedTime: String? = null
    var navController: NavController? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_prefered_time_slots_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        timeslots = Gson().fromJson(bundle?.getString("available_time_slots"), object : TypeToken<List<TimeSlot>>() {}.type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        datePicker?.onItemSelectedListener = this
        timePicker?.onItemSelectedListener = this
        confirm?.setOnClickListener {
            bundle?.putString("selected_date", selectedDate?.date)
            bundle?.putString("selected_time", selectedTime)
            navController?.navigate(R.id.action_to_creditCardDeliveryScheduleDeliveryFragment, bundleOf("bundle" to bundle))
        }
        configureUI()
    }

    fun configureUI() {
        timeslots?.let { setDatePickerData(it) }
    }

    override fun onItemSelected(wheelView: WheelView<Any>?, data: Any?, position: Int) {
        when (wheelView?.id) {
            R.id.datePicker -> {
                selectedDate = data as TimeSlot?
                selectedDate?.let { setTimePickerData(it) }
            }
            R.id.timePicker -> {
                selectedTime = data as String?
            }
        }
    }

    private fun setDatePickerData(timeSlots: List<TimeSlot>) {
        val defaultItemPosition = timeSlots.let { (it.size / 2) + (it.size % 2) }
        selectedDate = timeSlots[defaultItemPosition]
        selectedTime = selectedDate?.availableTimeslots?.let { (it.size / 2) + (it.size % 2) }?.let { selectedDate?.availableTimeslots?.get(it) }
        datePicker?.apply {
            selectedItemPosition = defaultItemPosition
            data = timeSlots
        }
        setTimePickerData(timeSlots[defaultItemPosition])
    }

    private fun setTimePickerData(timeSlot: TimeSlot) {
        timePicker?.apply {
            selectedItemPosition = timeSlot.availableTimeslots.let { (it.size / 2) + (it.size % 2) }
            data = timeSlot.availableTimeslots
        }
    }

}
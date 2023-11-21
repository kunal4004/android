package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryPreferedTimeSlotsLayoutBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.SlotDetails
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.TimeSlot
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class CreditCardDeliveryPreferedTimeSlotFragment : CreditCardDeliveryBaseFragment(R.layout.credit_card_delivery_prefered_time_slots_layout) {

    private lateinit var binding: CreditCardDeliveryPreferedTimeSlotsLayoutBinding
    private var timeslots: List<TimeSlot>? = null
    private var selectedDate: TimeSlot? = null
    private var selectedTime: String? = null
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        timeslots = Gson().fromJson(bundle?.getString("available_time_slots"), object : TypeToken<List<TimeSlot>>() {}.type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreditCardDeliveryPreferedTimeSlotsLayoutBinding.bind(view)

        this.navController = Navigation.findNavController(view)
        setUpToolBar()

        binding.confirm?.setOnClickListener {
            bundle?.putString("selected_date", selectedDate?.date)
            bundle?.putString("selected_time", selectedTime)

            val slotDetails = SlotDetails()
            slotDetails.slot = selectedTime
            slotDetails.appointmentDate = selectedDate?.date
            slotDetails.formattedDate = WFormatter.convertToFormatedDate(selectedDate?.date)
            val request: ScheduleDeliveryRequest = scheduleDeliveryRequest
            request.slotDetails = slotDetails
            val response: StatusResponse? = statusResponse
            response?.slotDetails = slotDetails
            bundle?.putString("ScheduleDeliveryRequest", Utils.toJson(request))
            bundle?.putParcelable(BundleKeysConstants.STATUS_RESPONSE, response)

            if (bundle?.containsKey("isEditRecipient") == true) {
                if (bundle?.getBoolean("isEditRecipient") == true) {
                    this.navController?.navigate(R.id.action_to_creditCardDeliveryScheduleDeliveryFrag, bundleOf("bundle" to bundle))
                } else
                    this.navController?.navigate(R.id.action_to_creditCardDeliveryScheduleDeliveryFragment, bundleOf("bundle" to bundle))
            } else
                this.navController?.navigate(R.id.action_to_creditCardDeliveryScheduleDeliveryFragment, bundleOf("bundle" to bundle))
        }
        configureUI()
    }

    fun configureUI() {
        timeslots?.let {
            binding.dateTimePicker.initialize(it){ date, time ->
                selectedDate = date
                selectedTime = time
                binding.confirm.isEnabled = true
            }
        }
    }

    private fun setUpToolBar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle("")
                changeToolbarBackground(R.color.white)
            }
        }
    }
}
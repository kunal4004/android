package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_prefered_time_slots_layout.*
import za.co.woolworths.financial.services.android.util.picker.WheelView


class CreditCardDeliveryPreferedTimeslotFragment : Fragment(), WheelView.OnItemSelectedListener<String> {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_prefered_time_slots_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wheelview?.data = listOf("Mon, 29th April", "Wed, 31st April", "Thus, 1st May", "Fri, 22nd june", "Tue, 11st Aug")
        wheelview1?.data = listOf("12am - 2pm", "2am - 12pm", "2am - 10pm", "6am - 7pm", "10am - 3pm", "8am - 11pm")
        wheelview?.onItemSelectedListener = this
        wheelview1?.onItemSelectedListener = this
    }

    override fun onItemSelected(wheelView: WheelView<String>?, data: String?, position: Int) {
        Log.i("WWWWWWW  ", data)
    }


}
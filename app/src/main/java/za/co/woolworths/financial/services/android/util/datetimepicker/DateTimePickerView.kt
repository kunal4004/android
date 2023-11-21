package za.co.woolworths.financial.services.android.util.datetimepicker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.TimeSlot
import za.co.woolworths.financial.services.android.ui.extension.bindString

class DateTimePickerView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val recyclerView: RecyclerView
    private val descriptionTextView: TextView
    private val radioContainerView: RadioGroup

    init {
        LayoutInflater.from(context).inflate(R.layout.date_time_picker_layout, this, true)
        recyclerView = findViewById(R.id.datesRecyclerView)
        descriptionTextView = findViewById(R.id.descriptionText)
        radioContainerView = findViewById(R.id.radioContainer)
    }

    fun initialize(timeSlots: List<TimeSlot>, setSelectedDate: (item: TimeSlot, time: String) -> Unit) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = DateTimeSlotPickerAdapter(context, timeSlots) { timeSlot ->
            descriptionTextView.visibility = VISIBLE
            radioContainerView.removeAllViews()

            timeSlot.availableTimeslots.forEach {availableSlot ->
                val separatorView = LayoutInflater.from(context)
                    .inflate(R.layout.timeslot_separator_item, this, false) as View

                val radioButton: RadioButton = LayoutInflater.from(context)
                    .inflate(R.layout.timeslot_item, this, false) as RadioButton

                radioButton.text = availableSlot
                radioButton.id = View.generateViewId()
                radioButton.contentDescription = bindString(R.string.time_slot) + (timeSlot.availableTimeslots.indexOf(availableSlot)+1)

                radioButton.setOnClickListener {
                    setSelectedDate(timeSlot, availableSlot)
                }
                radioContainerView.addView(radioButton)
                radioContainerView.addView(separatorView)
            }
        }
        recyclerView.adapter = adapter
    }
}
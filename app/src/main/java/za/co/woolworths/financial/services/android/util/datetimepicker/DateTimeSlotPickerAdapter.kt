package za.co.woolworths.financial.services.android.util.datetimepicker

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.TimeSlot
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateTimeSlotPickerAdapter(private val context: Context,
                                private val timeSlots: List<TimeSlot>,
                                private val onItemClick: (item: TimeSlot) -> Unit) :
    RecyclerView.Adapter<DateTimeSlotPickerAdapter.ViewHolder>() {

    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.date_button_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timeSlots[position], position)
    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TimeSlot, position: Int) {
            val view = itemView as LinearLayout
            view.contentDescription = bindString(R.string.day_and_Time_slot) + (position + 1)

            val dayTextView = view.findViewById<TextView>(R.id.dayText)
            val dateTextView = view.findViewById<TextView>(R.id.dateText)

            dayTextView.text = formatToCorrespondingDay(item.date)
            dayTextView.contentDescription = bindString(R.string.day_slot) + (position + 1)
            dateTextView.text = formatToDateMonth(item.date)
            dateTextView.contentDescription = bindString(R.string.month_slot) + (position + 1)

            val itemViewBackground: Drawable? = if (position == selectedItemPosition) {
                ContextCompat.getDrawable(context, R.drawable.selected_date_item)
            } else {
                ContextCompat.getDrawable(context, R.drawable.unselected_date_item)
            }

            val textBackground: Drawable? = if (position == selectedItemPosition) {
                ContextCompat.getDrawable(context, R.color.black)
            } else {
                ContextCompat.getDrawable(context, R.color.white)
            }

            val textColor: Int = if (position == selectedItemPosition) {
                ContextCompat.getColor(context, R.color.white)
            } else {
                ContextCompat.getColor(context, R.color.black)
            }

            dayTextView.setTextColor(textColor)
            dateTextView.setTextColor(textColor)
            dayTextView.background = textBackground
            dateTextView.background = textBackground
            itemView.background = itemViewBackground

            itemView.setOnClickListener {
                val previousSelectedItemPosition = selectedItemPosition
                selectedItemPosition = position

                notifyItemChanged(previousSelectedItemPosition)
                notifyItemChanged(selectedItemPosition)

                onItemClick(item)
            }
        }

        private fun formatToDateMonth(date: String): String? {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.let {
                SimpleDateFormat("d MMMM", Locale.getDefault()).format(it)
            }
        }

        private fun formatToCorrespondingDay(date: String): String? {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.let {
                val currentDate = Calendar.getInstance().time
                val tomorrowDate = Calendar.getInstance()
                tomorrowDate.add(Calendar.DAY_OF_MONTH, 1)

                when (it) {
                    currentDate -> {
                        context.getString(R.string.date_picker_today)
                    }
                    tomorrowDate.time -> {
                        context.getString(R.string.date_picker_tomorrow)
                    }
                    else -> {
                        SimpleDateFormat("EEE", Locale.getDefault()).format(it)
                    }
                }
            }
        }
    }
}
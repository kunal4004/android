package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.province_and_suburb_selection_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Suburb

class SuburbDropdownAdapter(private val context: Activity, resource: Int, private var suburbs: List<Suburb>, private val onSuburbSelected: (Suburb) -> Unit) : ArrayAdapter<Suburb>(context, resource, suburbs) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = context.layoutInflater.inflate(R.layout.province_and_suburb_selection_item, parent, false)
        rowView.tvProvinceName.text = suburbs[position].name
        rowView.setOnClickListener { onSuburbSelected(suburbs[position]) }
        return rowView
    }
}
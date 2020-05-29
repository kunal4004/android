package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.province_selection_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Province

class ProvinceDropdownAdapter(private val context: Activity, resource: Int, private var reregions: List<Province>, private val onProvinceSelected: (Province) -> Unit) : ArrayAdapter<Province>(context, resource, reregions) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = context.layoutInflater.inflate(R.layout.province_selection_item, parent, false)
        rowView.tvProvinceName.text = reregions[position].name
        rowView.setOnClickListener { onProvinceSelected(reregions[position]) }
        return rowView
    }

}
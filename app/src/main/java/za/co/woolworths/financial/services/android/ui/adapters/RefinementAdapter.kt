package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementBaseViewHolder

class RefinementAdapter(val context: Context) : RecyclerView.Adapter<RefinementBaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RefinementBaseViewHolder<*> {
        when(viewType){
                RefinementSelectableItem.ViewType.SECTION_HEADER.value->{
                    return SampleHolder(View.inflate(context, R.layout.refinements_multiple_selection_layout, parent))
                }
        }
        return null)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder<*>, position: Int) {
        //holder.bind()
    }

    class SampleHolder(itemView: View) : RefinementBaseViewHolder<String>(itemView) {
        override fun bind(`object`: String) {
        }

    }
}
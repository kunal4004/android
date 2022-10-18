package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.information

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.AccountCardInformationItemBinding
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountInformation


class InformationAdapter(private val dataSet: ArrayList<AccountInformation>) :
    RecyclerView.Adapter<InformationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        AccountCardInformationItemBinding
            .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            .root
    )


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        AccountCardInformationItemBinding.bind(viewHolder.itemView).apply{
            titleTextView.text = bindString(dataSet[position].title!!)
            descriptionTextView.text = bindString(dataSet[position].description!!)
        }
    }

    override fun getItemCount() = dataSet.size

}

package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountSalesBulletItemBinding
import com.awfs.coordination.databinding.AccountSalesCardBenefitsItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowSectionType
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ChildrenItems
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.UniqueIdentifiers
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.loadSvg
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.setContentDescription

const val LEFT_ICON_WITH_CONTENT = 112
const val LIST_UNORDERED = 114
class ApplyNowChildrensAdapter(
    var type: ApplyNowSectionType,
    var data: List<ChildrenItems>,
    var sectionTitle: String
) :
    RecyclerView.Adapter<ApplyNowChildrensAdapter.ApplyNowChildrensViewHolder>() {


    override fun getItemViewType(position: Int): Int {
        return when (type) {
            ApplyNowSectionType.LEFT_ICON_WITH_CONTENT -> LEFT_ICON_WITH_CONTENT
            else -> LIST_UNORDERED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplyNowChildrensViewHolder {
        val layout = when (viewType) {
            LEFT_ICON_WITH_CONTENT -> {
                R.layout.account_sales_card_benefits_item
            }
            LIST_UNORDERED -> {
                R.layout.account_sales_bullet_item
            }
            else -> throw IllegalArgumentException("Invalid type")
        }
        return ApplyNowChildrensViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ApplyNowChildrensViewHolder, position: Int) {
        when (type) {
            ApplyNowSectionType.LEFT_ICON_WITH_CONTENT -> {holder.bindLeftIconContent(data[position],position)}
            ApplyNowSectionType.LIST_UNORDERED -> {holder.bindListUnordered(data[position],position)}
            else -> {holder.bindListUnordered(data[position], position)}
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


    inner class ApplyNowChildrensViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindLeftIconContent(item: ChildrenItems, position: Int) {
            with(AccountSalesCardBenefitsItemBinding.bind(itemView)){
                salesBenefitTitleTextView.text = item.title
                salesBenefitDescriptionTextView.text = item.description
                salesItemImageView.loadSvg(item.imageUrl)
                addUniqueLocators(position)

            }
        }
        private fun AccountSalesCardBenefitsItemBinding.addUniqueLocators(position:Int){
            salesBenefitTitleTextView.setContentDescription(sectionTitle, position = position, viewName = UniqueIdentifiers.Title)
            salesBenefitDescriptionTextView.setContentDescription(sectionTitle, position = position, viewName = UniqueIdentifiers.Description)
            salesItemImageView.setContentDescription(sectionTitle, position = position, viewName = UniqueIdentifiers.Image)
        }

        fun bindListUnordered(item: ChildrenItems, position: Int) {
            with(AccountSalesBulletItemBinding.bind(itemView)){
                bulletTitleTextView.text = item.description
                bulletTitleTextView.setContentDescription(sectionTitle, position = position, viewName = UniqueIdentifiers.Title)
            }
        }
    }
}


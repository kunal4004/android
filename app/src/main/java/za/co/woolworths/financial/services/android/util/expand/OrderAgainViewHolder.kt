package za.co.woolworths.financial.services.android.util.expand

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OrderAgainCategoryViewBinding
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.SessionUtilities

class OrderAgainViewHolder (val itemBinding: OrderAgainCategoryViewBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(subCategoryModel: SubCategoryModel) {
        itemBinding.renderComposeView.background = ContextCompat.getDrawable(itemBinding.root.context, R.color.white)
        itemBinding.renderComposeView.setContent {
            OneAppTheme {
                OrderAgainView(
                    isSignedIn = SessionUtilities.getInstance().isUserAuthenticated
                )
            }
        }
    }
}

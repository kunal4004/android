package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeSelectorAdapter
import za.co.woolworths.financial.services.android.util.Utils

class ProductDetailsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = Utils.calculateNoOfColumns(activity, 50F)
        colorSelectorRecycleView.layoutManager = GridLayoutManager(activity, spanCount)
       /* val layoutManager = FlexboxLayoutManager(activity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        colorSelectorRecycleView.layoutManager = layoutManager*/
        colorSelectorRecycleView.adapter = ProductColorSelectorAdapter()

        /*val layoutManager1 = FlexboxLayoutManager(activity)
        layoutManager1.flexDirection = FlexDirection.ROW
        layoutManager1.justifyContent = JustifyContent.FLEX_START*/
        sizeSelectorRecycleView.layoutManager = GridLayoutManager(activity, 4)
        sizeSelectorRecycleView.adapter = ProductSizeSelectorAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.product_details_fragment, container, false)
    }
}
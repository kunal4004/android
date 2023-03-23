package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.ManageProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SubstitutionRecylerViewItem
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener, ProductSubstitutionListListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)

        manageProductSubstitutionAdapter = ManageProductSubstitutionAdapter(
            getHeaderForSubstituteList(), getSubstututeProductList() , this
        )

        binding.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
        }
    }

    private fun getHeaderForSubstituteList(): SubstitutionRecylerViewItem.SubstitutionOptionHeader {
        return SubstitutionRecylerViewItem.SubstitutionOptionHeader(
            searchHint = resources.getString(R.string.search_alternative_product)
        )
    }


    private fun getSubstututeProductList(): MutableList<SubstitutionRecylerViewItem.SubstitutionProducts> {
        var list = mutableListOf<SubstitutionRecylerViewItem.SubstitutionProducts>()

        /*prepare list from kibo api and set to recyler view */
        list.add( SubstitutionRecylerViewItem.SubstitutionProducts(1, productTitle = "Banana 1",
                productThumbnail = "https://assets.woolworthsstatic.co.za/100-Rye-Bread-400-g-6001009038821.jpg?V=QmO3&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTA4LzYwMDEwMDkwMzg4MjFfaGVyby5qcGcifQ&", productPrice = "123", promotionText = "you have saved rs 10"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(2, productTitle = "Banana 2",
                productThumbnail = "https://assets.woolworthsstatic.co.za/100-Rye-Bread-400-g-6001009038821.jpg?V=QmO3&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTA4LzYwMDEwMDkwMzg4MjFfaGVyby5qcGcifQ&", productPrice = "123", promotionText = "you have saved rs 10"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(3, productTitle = "Banana 3",
                productThumbnail = "https://assets.woolworthsstatic.co.za/100-Rye-Bread-400-g-6001009038821.jpg?V=QmO3&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTA4LzYwMDEwMDkwMzg4MjFfaGVyby5qcGcifQ&", productPrice = "123", promotionText = "you have saved rs 10"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(4, productTitle = "Banana 4",
                productThumbnail = "https://assets.woolworthsstatic.co.za/100-Rye-Bread-400-g-6001009038821.jpg?V=QmO3&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTA4LzYwMDEwMDkwMzg4MjFfaGVyby5qcGcifQ&", productPrice = "123", promotionText = "you have saved rs 10"))

        return list
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmSubstitutionProduct()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
        }
    }

    private fun confirmSubstitutionProduct() {

    }

    private fun confirmDontWantSubstitutionForProduct() {

    }

    override fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                SearchSubstitutionFragment()
        )
    }

    override fun clickOnLetMyShooperChooseOption() {
        binding.btnConfirm?.background = resources.getDrawable(R.drawable.black_background_with_corner_5, null)
    }
    override fun clickOnMySubstitutioneOption() {
        binding.btnConfirm?.isEnabled = false
        binding.btnConfirm?.background = resources.getDrawable(R.drawable.grey_background_with_corner_5, null)
    }
}

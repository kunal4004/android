package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.ManageProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SubstitutionRecylerViewItem
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener, ProductSubstitutionListListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null
    private var selectionChoice = ""
    private lateinit var productSubstitutionViewModel: ProductSubstitutionViewModel
    private var commerceItemId = ""


    companion object {
        private const val SELECTION_CHOICE = "SELECTION_CHOICE"
        const val COMMERCE_ITEM_ID = "COMMERCE_ITEM_ID"

        fun newInstance(
            substitutionSelectionChoice: String?,
            commerceItemId: String?,
        ) = ManageSubstitutionFragment().withArgs {
            putString(SELECTION_CHOICE, substitutionSelectionChoice)
            putString(COMMERCE_ITEM_ID, commerceItemId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            selectionChoice = getString(SELECTION_CHOICE, "")
            commerceItemId = getString(COMMERCE_ITEM_ID, "")
        }
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)
        binding.imgBack?.setOnClickListener(this)
        setUpViewModel()

        manageProductSubstitutionAdapter = ManageProductSubstitutionAdapter(
            getHeaderForSubstituteList(), getSubstituteProductList(), this
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

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
            this,
            ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))
        )[ProductSubstitutionViewModel::class.java]
    }


    private fun getSubstituteProductList(): MutableList<SubstitutionRecylerViewItem.SubstitutionProducts> {
        val list = mutableListOf<SubstitutionRecylerViewItem.SubstitutionProducts>()
        /*prepare list from kibo api and set to recycler view */
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you have 5", "R21"
            )
        )
        return list
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmSubstitutionProduct()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
            R.id.imgBack -> (activity as? BottomNavigationActivity)?.popFragment()
        }
    }

    private fun confirmSubstitutionProduct() {
        callAddSubstitutionApi()
    }

    private fun callAddSubstitutionApi() {
        if (commerceItemId?.isEmpty() == true) {
            /*navigate to pdp with selected product  object and call add to cart api in order to add substitute there*/
        } else {
            /*add substitute api here since we have commerceId because product is already added in cart */
        }

    }

    private fun confirmDontWantSubstitutionForProduct() {
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    override fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
            SearchSubstitutionFragment.newInstance(commerceItemId)
        )
    }

    override fun clickOnLetMyShooperChooseOption() {
        binding.btnConfirm?.background =
            resources.getDrawable(R.drawable.black_color_drawable, null)
    }

    override fun clickOnMySubstitutioneOption() {
        binding.btnConfirm?.isEnabled = false
        binding.btnConfirm?.background = resources.getDrawable(R.drawable.grey_bg_drawable, null)
    }

    override fun clickOnSubstituteProduct() {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background = resources.getDrawable(R.drawable.black_color_drawable, null)
    }
}

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
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener, ProductSubstitutionListListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null
    private var selectionChoice = ""

    companion object {
        private const val SELECTION_CHOICE = "SELECTION_CHOICE"

        fun newInstance(
            substitutionSelectionChoice: String?,
        ) = ManageSubstitutionFragment().withArgs {
            putString(SELECTION_CHOICE, substitutionSelectionChoice)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            selectionChoice = getString(SELECTION_CHOICE, ProductDetailsFragment.USER_CHOICE)
        }
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)
        binding.imgBack?.setOnClickListener(this)

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


    private fun getSubstituteProductList(): MutableList<SubstitutionRecylerViewItem.SubstitutionProducts> {
        val list = mutableListOf<SubstitutionRecylerViewItem.SubstitutionProducts>()
        /*prepare list from kibo api and set to recycler view */
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

    }

    private fun confirmDontWantSubstitutionForProduct() {
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    override fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
            SearchSubstitutionFragment()
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

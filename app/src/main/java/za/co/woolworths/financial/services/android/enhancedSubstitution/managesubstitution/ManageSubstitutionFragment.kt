package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.ManageProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SubstitutionRecylerViewItem
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)

        manageProductSubstitutionAdapter = ManageProductSubstitutionAdapter(
            getHeaderForSubstituteList(), getSubstututeProductList()
        )

        binding.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
        }
    }

    private fun getHeaderForSubstituteList(): SubstitutionRecylerViewItem.SubstitutionOptionHeader {
        return SubstitutionRecylerViewItem.SubstitutionOptionHeader(
            optionFirstText = resources.getString(R.string.let_my_shooper_choose_for_me),
            optionSecondText = resources.getString(R.string.choose_substitute),
            searchHint = resources.getString(R.string.search_alternative_product)
        )
    }


    private fun getSubstututeProductList(): MutableList<SubstitutionRecylerViewItem.SubstitutionProducts> {
        var list = mutableListOf<SubstitutionRecylerViewItem.SubstitutionProducts>()

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 1", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save2 ", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 3", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 4", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save5 ", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 6", "22"
            )
        )


        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 7", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 8", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save9 ", "22"
            )
        )

        list.add(
            SubstitutionRecylerViewItem.SubstitutionProducts(
                1, "Banana", "", "you save 10", "22"
            )
        )

        return list
    }

    override fun onClick(v: View?) {
        when (v?.id) {
          /*  R.id.btnConfirm -> confirmSubstitutionProduct()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct() */
        }
    }


    private fun searchForSubstitutions() {

    }

    private fun confirmSubstitutionProduct() {

    }

    private fun confirmDontWantSubstitutionForProduct() {

    }
}

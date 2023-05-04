package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.chanel.listener.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.ManageProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.SubstitutionProducts
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils
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
        setUpViewModel()
        initView()
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)
        binding.imgBack?.setOnClickListener(this)
    }

    fun initView() {
        binding.layoutManageSubstitution?.apply {
            manageProductSubstitutionAdapter = ManageProductSubstitutionAdapter(
                    getKiboList(), this@ManageSubstitutionFragment
            )
            this.listSubstitute?.recyclerView?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = manageProductSubstitutionAdapter
                isNestedScrollingEnabled = false
            }


            this.listSubstitute?.tvSearchProduct?.onClick {
                /*navigate to new search screen*/
                openSubstitutionSearchScreen()
            }
            rbShopperChoose?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    rbOwnSubstitute?.isChecked = false
                    this.listSubstitute?.tvSearchProduct?.isEnabled = false
                    this.listSubstitute?.recyclerView?.isEnabled = false
                    clickOnLetMyShooperChooseOption()
                }
            }

            rbOwnSubstitute?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    rbShopperChoose?.isChecked = false
                    this.listSubstitute?.tvSearchProduct?.isEnabled = true
                    clickOnMySubstitutioneOption()
                }
            }
        }
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
                this,
                ProductSubstitutionViewModelFactory(
                    ProductSubstitutionRepository(
                    SubstitutionApiHelper()
                )
                )
        )[ProductSubstitutionViewModel::class.java]
    }

    private fun getKiboList(): ArrayList<SubstitutionProducts> {
        val list = ArrayList<SubstitutionProducts>()
        /*prepare list from kibo api and set to recycler view */
        /* todo this will be uncommented once kibo api is integrated*/
        /*list.add(
                SubstitutionProducts(
                        1, "Banana1", "", "you have 5", "R21"
                )
        )
        list.add(
                SubstitutionProducts(
                        1, "Banana2", "", "you have 5", "R21"
                )
        )
        list.add(
                SubstitutionProducts(
                        1, "Banana2", "", "you have 5", "R21"
                )
        )
        list.add(
                SubstitutionProducts(
                        1, "Banana2", "", "you have 5", "R21"
                )
        )
        list.add(
                SubstitutionProducts(
                        1, "Banana2", "", "you have 5", "R21"
                )
                )*/
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
            /*call add substitute api here since we have commerceId because product is already added in cart */
        }
    }

    private fun confirmDontWantSubstitutionForProduct() {
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                SearchSubstitutionFragment.newInstance(commerceItemId)
        )
    }

    private fun clickOnLetMyShooperChooseOption() {
        binding.btnConfirm?.background =
                resources.getDrawable(R.drawable.black_color_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseptionSelected = true
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, true)
    }

    private fun clickOnMySubstitutioneOption() {
        binding.btnConfirm?.isEnabled = false
        binding.btnConfirm?.background = resources.getDrawable(R.drawable.grey_bg_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseptionSelected = false
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, false)
    }

    override fun clickOnSubstituteProduct() {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background = resources.getDrawable(R.drawable.black_color_drawable, null)
    }
}

package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.GetKiboProductRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Product
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.SubstitutionProducts
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.utils.listener.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SELECTED_SUBSTITUTED_PRODUCT
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.network.Status
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
    private var productId = ""
    private var skuId = ""
    private var itemList: ArrayList<Item>? = ArrayList<Item>()
    private var storeId = ""
    private var multiSku = ""

    companion object {
        private const val SELECTION_CHOICE = "SELECTION_CHOICE"
        const val COMMERCE_ITEM_ID = "COMMERCE_ITEM_ID"
        const val PRODUCT_ID = "PRODUCT_ID"
        const val SKU_ID = "SKU_ID"
        const val DONT_WANT_SUBSTITUTE_LISTENER = "DONT_WANT_SUBSTITUTE_LISTENER"

        fun newInstance(
            substitutionSelectionChoice: String?,
            commerceItemId: String?,
            productId: String? = "",
            skuId: String? = "",
        ) = ManageSubstitutionFragment().withArgs {
            putString(SELECTION_CHOICE, substitutionSelectionChoice)
            putString(COMMERCE_ITEM_ID, commerceItemId)
            putString(PRODUCT_ID, productId)
            putString(SKU_ID, skuId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            selectionChoice = getString(SELECTION_CHOICE, "")
            commerceItemId = getString(COMMERCE_ITEM_ID, "")
            productId = getString(PRODUCT_ID, "")
            skuId = getString(SKU_ID, "")
        }
        setUpViewModel()
        initView()
        binding.btnConfirm.setOnClickListener(this)
        binding.dontWantText.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
    }

    fun initView() {
        getKiboList()
        binding.layoutManageSubstitution.apply {
            listSubstitute.tvSearchProduct.onClick {
                openSubstitutionSearchScreen()
            }
            if (selectionChoice == SubstitutionChoice.SHOPPER_CHOICE.name || selectionChoice == SubstitutionChoice.NO.name) {
                clickOnLetMyShooperChooseOption()
            } else {
                clickOnMySubstitutioneOption()
            }

            rbShopperChoose.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    clickOnLetMyShooperChooseOption()
                }
            }

            rbOwnSubstitute.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    clickOnMySubstitutioneOption()
                }
            }
        }
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
            this, ProductSubstitutionViewModelFactory(
                ProductSubstitutionRepository(
                    SubstitutionApiHelper()
                )
            )
        )[ProductSubstitutionViewModel::class.java]
    }

    private fun getKiboList(): ArrayList<SubstitutionProducts> {
        val list = ArrayList<SubstitutionProducts>()
        productSubstitutionViewModel.getKiboProducts(prepareProductRequest())
        productSubstitutionViewModel.kiboProductResponse.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {

                        resource.data?.data?.let {
                            itemList = it.responses.getOrNull(0)?.actions?.getOrNull(0)?.items
                            prepareStockInventoryCallRequest(itemList)
                        }
                    }
                    Status.ERROR -> {
                    }
                }
            }
        }
        return list
    }

    private fun prepareStockInventoryCallRequest(itemList: ArrayList<Item>?) {
        storeId = Utils.retrieveStoreId("01")?.replace("\"", "") ?: ""
        val skudIds = ArrayList<String>()
        itemList?.forEach {
            skudIds.add(it.id)
        }
        multiSku = TextUtils.join("-", skudIds)
        getInventoryStock(storeId, multiSku, itemList)
    }

    private fun getInventoryStock(skudIds: String, multiSku: String, itemList: ArrayList<Item>?) {
        productSubstitutionViewModel.getInventoryForStock(skudIds, multiSku)
        productSubstitutionViewModel.stockInventoryResponse.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                    }

                    Status.SUCCESS -> {
                        val skuInventory = resource.data?.skuInventory
                        skuInventory?.removeAll {
                            it.quantity <= 10
                        }
                        itemList?.removeAll { item ->
                            skuInventory?.any {
                                it.sku == item.id
                            } == false
                        }
                    }
                    Status.ERROR -> {

                    }
                }
            }
        }
    }

    private fun setRecyclerViewForKiboProducts() {
        manageProductSubstitutionAdapter = itemList?.let { it1 ->
            ManageProductSubstitutionAdapter(
                it1, this@ManageSubstitutionFragment
            )
        }
        binding.layoutManageSubstitution.listSubstitute.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
            isNestedScrollingEnabled = false
        }
    }

    fun prepareProductRequest(): GetKiboProductRequest {

        val product = Product(productId, skuId)
        val list = ArrayList<Product>()
        list.add(product)
        val getKiboProductRequest = GetKiboProductRequest(list)
        return getKiboProductRequest
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
        if (commerceItemId.isEmpty() == true) {
            /*navigate to pdp with selected product  object and call add to cart api in order to add substitute there*/

        } else {
            /*call add substitute api here since we have commerceId because product is already added in cart */

        }
    }

    private fun confirmDontWantSubstitutionForProduct() {
        setFragmentResult(
            SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                DONT_WANT_SUBSTITUTE_LISTENER to DONT_WANT_SUBSTITUTE_LISTENER
            )
        )
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
            SearchSubstitutionFragment.newInstance(commerceItemId)
        )
    }

    private fun clickOnLetMyShooperChooseOption() {
        binding.layoutManageSubstitution.rbShopperChoose.isChecked = true
        binding.layoutManageSubstitution.rbOwnSubstitute.isChecked = false
        binding.layoutManageSubstitution.listSubstitute.tvSearchProduct.isEnabled = false
        binding.layoutManageSubstitution.listSubstitute.recyclerView.isEnabled = false
        binding.layoutManageSubstitution.listSubstitute.recyclerView.isClickable = false
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseptionSelected = true
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, true)
    }

    private fun clickOnMySubstitutioneOption() {
        binding.layoutManageSubstitution.rbOwnSubstitute.isChecked = true
        binding.layoutManageSubstitution.rbShopperChoose.isChecked = false
        binding.layoutManageSubstitution.listSubstitute.tvSearchProduct.isEnabled = true
        binding.layoutManageSubstitution.listSubstitute.recyclerView.isEnabled = true
        binding.layoutManageSubstitution.listSubstitute.recyclerView.isClickable = true
        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.grey_bg_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseptionSelected = false
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, false)
    }

    override fun clickOnSubstituteProduct() {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
    }
}

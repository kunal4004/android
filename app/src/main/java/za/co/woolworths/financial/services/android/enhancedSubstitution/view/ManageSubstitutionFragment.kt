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
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import com.facebook.shimmer.Shimmer
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.utils.listener.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SELECTED_SUBSTITUTED_PRODUCT
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SUBSTITUTION_ITEM_KEY
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
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
    private var item: Item? = null

    companion object {
        private const val SELECTION_CHOICE = "SELECTION_CHOICE"
        const val COMMERCE_ITEM_ID = "COMMERCE_ITEM_ID"
        const val PRODUCT_ID = "PRODUCT_ID"
        const val SKU_ID = "SKU_ID"
        const val DONT_WANT_SUBSTITUTE_LISTENER = "DONT_WANT_SUBSTITUTE_LISTENER"
        const val LET_MY_SHOPPER_CHOOSE = "LET_MY_SHOPPER_CHOOSE"

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
        binding.layoutManageSubstitution.rbShopperChoose.setOnClickListener(this)
        binding.layoutManageSubstitution.rbOwnSubstitute.setOnClickListener(this)
    }

    fun initView() {
        getKiboList()
        binding.layoutManageSubstitution.apply {
            listSubstitute.tvSearchProduct.onClick {
                openSubstitutionSearchScreen()
            }
            if (selectionChoice == SubstitutionChoice.SHOPPER_CHOICE.name || selectionChoice == SubstitutionChoice.NO.name) {
                rbShopperChoose.isChecked = true
                rbOwnSubstitute.isChecked = false
            } else {
                rbShopperChoose.isChecked = false
                rbOwnSubstitute.isChecked = true
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


    private fun showShimmerView() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        binding.layoutManageSubstitution.listSubstitute.shimmerLayout.apply {
            visibility = View.VISIBLE
            setShimmer(shimmer)
            startShimmer()
        }
    }

    private fun hideShimmerView() {
        binding.layoutManageSubstitution.listSubstitute.shimmerLayout.apply {
            setShimmer(null)
            stopShimmer()
            visibility = View.GONE
        }
    }

    private fun getKiboList(): ArrayList<SubstitutionProducts> {
        val list = ArrayList<SubstitutionProducts>()
        productSubstitutionViewModel.getKiboProducts(prepareProductRequest())
        productSubstitutionViewModel.kiboProductResponse.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showShimmerView()
                    }
                    Status.SUCCESS -> {
                        resource.data?.data?.let {
                            itemList = it.responses.getOrNull(0)?.actions?.getOrNull(0)?.items
                            prepareStockInventoryCallRequest(itemList)
                        }
                    }
                    Status.ERROR -> {
                        hideShimmerView()
                        /*todo need to show error screen*/
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
        var configQuantity: Int? =
            AppConfigSingleton.enhanceSubstitution?.thresholdQuantityForSubstitutionProduct
        productSubstitutionViewModel.getInventoryForStock(skudIds, multiSku)
        productSubstitutionViewModel.stockInventoryResponse.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showShimmerView()
                    }

                    Status.SUCCESS -> {
                        hideShimmerView()
                        val skuInventory = resource.data?.skuInventory
                        configQuantity?.let {
                            skuInventory?.removeAll {
                                it.quantity <= configQuantity
                            }
                        }

                        itemList?.removeAll { item ->
                            skuInventory?.any {
                                it.sku == item.id
                            } == false
                        }
                        setRecyclerViewForKiboProducts()
                    }
                    Status.ERROR -> {
                        hideShimmerView()
                    }
                }
            }
        }
    }

    private fun setRecyclerViewForKiboProducts() {
        if (itemList?.isEmpty() == true) {
            /*todo show empty error scren*/
        }
        manageProductSubstitutionAdapter = itemList?.let { it1 ->
            ManageProductSubstitutionAdapter(
                it1,
                this@ManageSubstitutionFragment,
                binding.layoutManageSubstitution.rbOwnSubstitute.isChecked
            )
        }
        binding.layoutManageSubstitution.listSubstitute.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
            isNestedScrollingEnabled = false
        }
        if (binding.layoutManageSubstitution.rbOwnSubstitute.isChecked) {
            Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, false)
        } else {
            Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, true)
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
            R.id.btnConfirm -> handleConfirmButton()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
            R.id.imgBack -> (activity as? BottomNavigationActivity)?.popFragment()
            R.id.rbShopperChoose -> {
                Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, true)
                clickOnLetMyShooperChooseOption()
            }
            R.id.rbOwnSubstitute -> clickOnOwnSubstitutioneOption()
        }
    }

    private fun handleConfirmButton() {
        if (binding.layoutManageSubstitution.rbShopperChoose.isChecked) {
            setFragmentResult(
                SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                    LET_MY_SHOPPER_CHOOSE to true
                )
            )
            (activity as? BottomNavigationActivity)?.popFragment()
            return
        }
        callAddSubstitutionApi()
    }

    private fun callAddSubstitutionApi() {
        if (commerceItemId.isEmpty() == true) {
            /*navigate to pdp with selected product  object and call add to cart api in order to add substitute there*/
            val kiboProduct = ProductList()
            kiboProduct.productName = item?.title
            kiboProduct.externalImageRefV2 = item?.imageLink
            kiboProduct.productId = item?.id

            setFragmentResult(
                SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                    SUBSTITUTION_ITEM_KEY to kiboProduct
                )
            )
            (activity as? BottomNavigationActivity)?.popFragment()
        } else {
            /*call add substitute api here since we have commerceId because product is already added in cart */
            callAddSubsAPi()
        }
    }

    fun callAddSubsAPi() {
        val addSubstitutionRequest = AddSubstitutionRequest(
            substitutionSelection = SubstitutionChoice.USER_CHOICE.name,
            substitutionId = skuId,
            commerceItemId = commerceItemId
        )
        productSubstitutionViewModel.addSubstitutionForProduct(addSubstitutionRequest)
        productSubstitutionViewModel.addSubstitutionResponse?.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE

                        /* if we get form exception need to show error popup*/
                        resource.data?.data?.getOrNull(0)?.formExceptions?.getOrNull(0)?.let {
                            if (it.message?.isNotEmpty() == true) {
                                /*todo show error screen*/
                            }
                            return@observe
                        }
                        /* navigate to pdp and call getSubs. api*/
                        setFragmentResult(SELECTED_SUBSTITUTED_PRODUCT, bundleOf(SearchSubstitutionFragment.SUBSTITUTION_ITEM_ADDED to true))
                        (activity as? BottomNavigationActivity)?.popFragment()
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun confirmDontWantSubstitutionForProduct() {
        setFragmentResult(
            SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                DONT_WANT_SUBSTITUTE_LISTENER to true
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
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseOptionSelected = true
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
    }

    private fun clickOnOwnSubstitutioneOption() {
        binding.layoutManageSubstitution.rbOwnSubstitute.isChecked = true
        binding.layoutManageSubstitution.rbShopperChoose.isChecked = false
        binding.layoutManageSubstitution.listSubstitute.tvSearchProduct.isEnabled = true
        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.grey_bg_drawable, null)
        manageProductSubstitutionAdapter?.isShopperchooseOptionSelected = false
        manageProductSubstitutionAdapter?.notifyDataSetChanged()
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, false)
    }

    override fun clickOnSubstituteProduct(item: Item?) {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
        this.item = item
    }
}

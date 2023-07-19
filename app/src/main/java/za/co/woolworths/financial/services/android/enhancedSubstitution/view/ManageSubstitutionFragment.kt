package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.graphics.Typeface
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import com.facebook.shimmer.Shimmer
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SEARCH_SCREEN_BACK_NAVIGATION
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForAddSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.ERROR_SEARCH_SCREEN_BACK_NAVIGATION
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SELECTED_SUBSTITUTED_PRODUCT
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SUBSTITUTION_ITEM_KEY
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SubstitutionProcessingScreen.Companion.SUBSTITUTION_ERROR_SCREEN_BACK_NAVIGATION
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

@AndroidEntryPoint
class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener, ProductSubstitutionListListener, OnTouchListener,
    ViewTreeObserver.OnScrollChangedListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null
    private var selectionChoice = ""
    private val productSubstitutionViewModel: ProductSubstitutionViewModel by activityViewModels()
    private var commerceItemId = ""
    private var productId = ""
    private var skuId = ""
    private var itemList: ArrayList<Item>? = ArrayList()
    private var storeId: String? = ""
    private var multiSku = ""
    private var item: Item? = null

    companion object {
        const val SELECTION_CHOICE = "SELECTION_CHOICE"
        const val COMMERCE_ITEM_ID = "COMMERCE_ITEM_ID"
        const val PRODUCT_ID = "PRODUCT_ID"
        const val SKU_ID = "SKU_ID"
        const val DONT_WANT_SUBSTITUTE_LISTENER = "DONT_WANT_SUBSTITUTE_LISTENER"
        const val LET_MY_SHOPPER_CHOOSE = "LET_MY_SHOPPER_CHOOSE"
        const val KIBO_PRODUCT_SIZE = 5

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
        addFragmentResultListner()
        arguments?.apply {
            selectionChoice = getString(SELECTION_CHOICE, "")
            commerceItemId = getString(COMMERCE_ITEM_ID, "")
            productId = getString(PRODUCT_ID, "")
            skuId = getString(SKU_ID, "")
        }
        initView()
        binding.btnConfirm.setOnClickListener(this)
        binding.dontWantText.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.layoutManageSubstitution.rbShopperChoose.setOnClickListener(this)
        binding.layoutManageSubstitution.rbOwnSubstitute.setOnClickListener(this)
    }

    private fun addFragmentResultListner() {
        setFragmentResultListener(SEARCH_SCREEN_BACK_NAVIGATION) { _, bundle ->
            setFragmentResultForPreviousFragment(bundle)
        }

        setFragmentResultListener(SUBSTITUTION_ERROR_SCREEN_BACK_NAVIGATION) { _, bundle ->
            setFragmentResultForPreviousFragment(bundle)
        }

        setFragmentResultListener(ERROR_SEARCH_SCREEN_BACK_NAVIGATION) { _, bundle ->
            setFragmentResultForPreviousFragment(bundle)
        }
    }

    private fun setFragmentResultForPreviousFragment(bundle: Bundle) {
        setFragmentResult(SELECTED_SUBSTITUTED_PRODUCT, bundle)
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    fun initView() {
        binding.layoutManageSubstitution.apply {
            listSubstitute.tvSearchProduct.onClick {
                openSubstitutionSearchScreen()
            }
            if (selectionChoice == SubstitutionChoice.SHOPPER_CHOICE.name || selectionChoice == SubstitutionChoice.NO.name) {
                handleOptionsForShopperchoice()
                enableConfirmButton()
            } else {
                handleOptionsForOwnSubstitution()
                disableConfirmButton()
            }
        }
        binding.layoutManageSubstitution.nestedScrollView.viewTreeObserver.addOnScrollChangedListener(
            this
        )
        getKiboList()
    }

    private fun showShimmerView() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        binding.layoutManageSubstitution.listSubstitute.shimmerLayout.apply {
            visibility = VISIBLE
            setShimmer(shimmer)
            startShimmer()
        }
    }

    private fun hideShimmerView() {
        binding.layoutManageSubstitution.listSubstitute.shimmerLayout.apply {
            setShimmer(null)
            stopShimmer()
            visibility = GONE
        }
    }

    private fun getKiboList() {
        binding.layoutManageSubstitution.listSubstitute.apply {
            recyclerView.visibility = VISIBLE
        }
        productSubstitutionViewModel.getKiboProducts(prepareProductRequest())
        productSubstitutionViewModel.kiboProductResponse.observe(viewLifecycleOwner) { kiboProductResponse ->

            kiboProductResponse.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showShimmerView()
                    }

                    Status.SUCCESS -> {
                        resource.data?.data?.let {
                            hideShimmerView()
                            itemList?.clear()
                            it.responses.getOrNull(0)?.actions?.forEach {
                                   it.items.forEach {
                                       itemList?.add(it)
                                   }
                            }
                            if (itemList.isNullOrEmpty()) {
                                showEmptyErrorScreen()
                                return@observe
                            }
                            binding.errorMessage.visibility = GONE
                            /*remove same product id */
                            itemList?.removeAll {
                                it.id == productId
                            }
                            prepareStockInventoryCallRequest(itemList)
                        }
                    }

                    Status.ERROR -> {
                        hideShimmerView()
                        showKiboFailureErrorView()
                    }
                }
            }
        }
    }

    private fun showKiboFailureErrorView() {
        binding.layoutManageSubstitution.listSubstitute.apply {
            emptySubstitutionLayout.root.visibility = GONE
            recyclerView.visibility = GONE
        }
        if (selectionChoice == SubstitutionChoice.USER_CHOICE.name) {
            disableConfirmButton()
        }
        binding.errorMessage.visibility = VISIBLE
        binding.errorMessage.makeLinks(
            Pair(getString(R.string.tap_to_retry), OnClickListener {
                getKiboList()
            })
        )
    }

    /*
     *  This function is to create a underline and to create a particular link text in a text view.
     */
    private fun TextView.makeLinks(vararg links: Pair<String, OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                    textPaint.isUnderlineText = true
                    textPaint.isFakeBoldText = true
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not be clickable.
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun prepareStockInventoryCallRequest(itemList: ArrayList<Item>?) {
        storeId = KotlinUtils.getDeliveryType()?.storeId
        val skudIds = ArrayList<String>()
        itemList?.forEach {
            skudIds.add(it.id)
        }
        multiSku = TextUtils.join("-", skudIds)
        storeId?.let {
            getInventoryStock(it, multiSku, itemList)
        }
    }

    private fun getInventoryStock(storeId: String, multiSku: String, itemList: ArrayList<Item>?) {
        val configQuantity: Int? =
            AppConfigSingleton.enhanceSubstitution?.thresholdQuantityForSubstitutionProduct
        productSubstitutionViewModel.getInventoryForKiboProducts(storeId, multiSku)
        productSubstitutionViewModel.stockInventoryResponse.observe(viewLifecycleOwner) { skuInventoryForStoreResponse ->

            skuInventoryForStoreResponse.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showShimmerView()
                    }

                    Status.SUCCESS -> {
                        hideShimmerView()
                        binding.errorMessage.visibility = GONE
                        val skuInventory = resource.data?.skuInventory
                        configQuantity?.let {
                            skuInventory?.removeAll {
                                it.quantity < configQuantity
                            }
                        }

                        itemList?.removeAll { item ->
                            skuInventory?.any {
                                it.sku == item.id
                            } == false
                        }

                        if (itemList?.isEmpty() == false) {
                            binding.layoutManageSubstitution.listSubstitute.recyclerView.visibility = VISIBLE
                            if (itemList.size > KIBO_PRODUCT_SIZE) {
                                setRecyclerViewForKiboProducts(itemList.take(KIBO_PRODUCT_SIZE) as? ArrayList<Item>?)
                            } else {
                                setRecyclerViewForKiboProducts(itemList)
                            }
                        } else {
                            showEmptyErrorScreen()
                        }

                    }

                    Status.ERROR -> {
                        hideShimmerView()
                        showKiboFailureErrorView()
                    }
                }
            }
        }
    }

    private fun setRecyclerViewForKiboProducts(itemList: ArrayList<Item>?) {
        manageProductSubstitutionAdapter = itemList?.let { items ->
            ManageProductSubstitutionAdapter(
                items,
                this@ManageSubstitutionFragment
            )
        }
        manageProductSubstitutionAdapter?.setRadioButtonDisabled(binding.layoutManageSubstitution.rbShopperChoose.isChecked)
        binding.layoutManageSubstitution.listSubstitute.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
            isNestedScrollingEnabled = false
        }
        if (binding.layoutManageSubstitution.rbOwnSubstitute.isChecked) {
            Utils.fadeInFadeOutAnimation(
                binding.layoutManageSubstitution.listSubstitute.root,
                false
            )
        } else {
            Utils.fadeInFadeOutAnimation(
                binding.layoutManageSubstitution.listSubstitute.root,
                true
            )
        }
    }

    private fun showEmptyErrorScreen() {
        binding.layoutManageSubstitution.listSubstitute.apply {
            emptySubstitutionLayout.root.visibility = VISIBLE
            recyclerView.visibility = GONE
        }
    }

    private fun prepareProductRequest(): KiboProductRequest {
        val product = Product(productId, skuId)
        val list = ArrayList<Product>()
        list.add(product)
        val plist = KotlinUtils.extractPlistFromDeliveryDetails()
        if (plist.isNullOrEmpty()) {
            return  KiboProductRequest(products = list)
        }
        return KiboProductRequest(priceListId = plist, products = list)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> handleConfirmButton()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
            R.id.imgBack -> {
                triggerFirebaseEventForSubstitution(isbackButtonEvent = true)
                (activity as? BottomNavigationActivity)?.popFragment()
            }
            R.id.rbShopperChoose -> {
                Utils.fadeInFadeOutAnimation(
                    binding.layoutManageSubstitution.listSubstitute.root,
                    true
                )
                clickOnLetMyShooperChooseOption()
            }

            R.id.rbOwnSubstitute -> clickOnOwnSubstitutioneOption()
        }
    }

    private fun handleConfirmButton() {
        val selectionChoice = if (binding.layoutManageSubstitution.rbOwnSubstitute.isChecked) {
            SubstitutionChoice.USER_CHOICE.name
        } else {
            SubstitutionChoice.SHOPPER_CHOICE.name
        }
        callAddSubstitutionAPi(selectionChoice)
    }

    private fun callAddSubstitutionAPi(substitutionChoice: String) {
        if (substitutionChoice == SubstitutionChoice.SHOPPER_CHOICE.name) {
            if (commerceItemId.isEmpty()) {
                setFragmentResult(
                    SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                        LET_MY_SHOPPER_CHOOSE to true
                    )
                )
                (activity as? BottomNavigationActivity)?.popFragment()
                return
            }
        }

        if (substitutionChoice == SubstitutionChoice.USER_CHOICE.name) {
            if (commerceItemId.isEmpty()) {
                /*navigate to pdp with selected product object and call add to cart api in order to add substitute there*/
                val selectedKiboProduct = getSelectedKiboProduct()
                setFragemntResultAndNavigateToPreviousFragment(
                    bundleOf(
                        SUBSTITUTION_ITEM_KEY to selectedKiboProduct
                    )
                )
                return
            }
        }


        val addSubstitutionRequest = AddSubstitutionRequest(
            substitutionSelection = substitutionChoice,
            substitutionId = item?.id,
            commerceItemId = commerceItemId
        )

        productSubstitutionViewModel.addSubstitutionForProduct(addSubstitutionRequest)
        productSubstitutionViewModel.addSubstitutionResponse.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressBar.visibility = GONE
                        /* if we get form exception need to show error screen*/
                        resource.data?.data?.getOrNull(0)?.formexceptions?.getOrNull(0)?.let {
                            if (it.message?.isNotEmpty() == true) {
                                showErrorScreen(substitutionChoice)
                            }
                            return@observe
                        }
                        triggerFirebaseEventForSubstitution(selectionChoice = substitutionChoice)
                        if (selectionChoice == SubstitutionChoice.USER_CHOICE.name) {
                            item?.price?.let { price -> triggerFirebaseEventForAddSubstitution(itemId = item?.id, itemName = item?.title, itemPrice = price) }
                        }
                        navigateToPreviousFragment(substitutionChoice)
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = GONE
                        showErrorScreen(substitutionChoice)
                    }
                }
            }
        })
    }

    private fun getSelectedKiboProduct(): ProductList {
        val kiboProduct = ProductList()
        kiboProduct.apply {
            productName = item?.title
            externalImageRefV2 = item?.imageLink
            productId = item?.id
        }
        return kiboProduct
    }

    private fun navigateToPreviousFragment(substitutionChoice: String) {
        if (substitutionChoice == SubstitutionChoice.USER_CHOICE.name) {
            val selectedKiboProduct = getSelectedKiboProduct()
            setFragemntResultAndNavigateToPreviousFragment(
                bundleOf(
                    SUBSTITUTION_ITEM_KEY to selectedKiboProduct
                )
            )
            return
        }

        if (substitutionChoice == SubstitutionChoice.SHOPPER_CHOICE.name) {
            setFragemntResultAndNavigateToPreviousFragment(bundleOf(LET_MY_SHOPPER_CHOOSE to true))
            return
        }

        if (substitutionChoice == SubstitutionChoice.NO.name) {
            setFragemntResultAndNavigateToPreviousFragment(bundleOf(DONT_WANT_SUBSTITUTE_LISTENER to true))
            return
        }
    }

    private fun setFragemntResultAndNavigateToPreviousFragment(bundle: Bundle) {
        setFragmentResult(SELECTED_SUBSTITUTED_PRODUCT, bundle)
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun showErrorScreen(substitutionChoice: String) {
        removeObserver()
        (activity as? BottomNavigationActivity)?.pushFragment(
            SubstitutionProcessingScreen.newInstance(
                commerceItemId,
                item?.id,
                substitutionChoice
            )
        )
    }

    private fun removeObserver() {
        productSubstitutionViewModel.addSubstitutionResponse.removeObservers(viewLifecycleOwner)
    }

    private fun confirmDontWantSubstitutionForProduct() {
        if (commerceItemId.isEmpty()) {
            setFragmentResult(
                SELECTED_SUBSTITUTED_PRODUCT, bundleOf(
                    DONT_WANT_SUBSTITUTE_LISTENER to true
                )
            )
            (activity as? BottomNavigationActivity)?.popFragment()
        } else {
            callAddSubstitutionAPi(SubstitutionChoice.NO.name)
        }
    }

    private fun openSubstitutionSearchScreen() {
        productSubstitutionViewModel.stockInventoryResponse.removeObservers(viewLifecycleOwner)
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
            SearchSubstitutionFragment.newInstance(commerceItemId, productId)
        )
    }

    private fun clickOnLetMyShooperChooseOption() {
        handleOptionsForShopperchoice()
        enableConfirmButton()
        manageProductSubstitutionAdapter?.setRadioButtonDisabled(true)
    }

    private fun clickOnOwnSubstitutioneOption() {
        handleOptionsForOwnSubstitution()
        disableConfirmButton()
        manageProductSubstitutionAdapter?.setRadioButtonDisabled(false)
        Utils.fadeInFadeOutAnimation(binding.layoutManageSubstitution.listSubstitute.root, false)
    }

    override fun clickOnSubstituteProduct(item: Item?) {
        enableConfirmButton()
        this.item = item
    }

    private fun enableConfirmButton() {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.black_color_drawable, null)
    }

    private fun disableConfirmButton() {
        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.background =
            ResourcesCompat.getDrawable(resources, R.drawable.grey_bg_drawable, null)
    }

    private fun handleOptionsForShopperchoice() {
        binding.layoutManageSubstitution.apply {
            rbShopperChoose.isChecked = true
            rbOwnSubstitute.isChecked = false
            listSubstitute.tvSearchProduct.isEnabled = false
        }
    }

    private fun handleOptionsForOwnSubstitution() {
        binding.layoutManageSubstitution.apply {
            rbOwnSubstitute.isChecked = true
            rbShopperChoose.isChecked = false
            listSubstitute.tvSearchProduct.isEnabled = true
        }

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    override fun onScrollChanged() {
        binding.layoutManageSubstitution.nestedScrollView.apply {
            val view = this.getChildAt(this.childCount - 1)
            val bottomDetector: Int = view.bottom - (this.height + this.scrollY)
            if (bottomDetector == 0) {
                binding.viewSeparator.visibility = VISIBLE
            } else {
                binding.viewSeparator.visibility = GONE
            }
        }
    }
}


package za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.recommendations.presentation.fragment.RecommendationFragment
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.KotlinUtils

class MyRecycleViewHolder(val mProductListingPageRowBinding: RecommendationsProductListingPageRowBinding) :
    RecyclerView.ViewHolder(mProductListingPageRowBinding.root) {


    fun setProductItem(
        productList: ProductList,
        navigator: IProductListing,
        nextProduct: ProductList? = null,
        previousProduct: ProductList? = null,
        position: Int
    ) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages)
            setProductName(this)
            setBrandText(this, nextProduct, previousProduct)
            setBrandHeaderDescriptionText(this)
            setPromotionalText(this)
            setRatingAndReviewCount(this)
            val priceItem = ProductsPriceItem()
            priceItem.setPrice(
                productList,
                mProductListingPageRowBinding.includeProductListingPriceLayout
            )
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            setOnClickListener(navigator, this, position)
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList, position: Int
    ) {
        mProductListingPageRowBinding.imProductImage.setOnClickListener { navigator.openProductDetailView(productList,position) }
        mProductListingPageRowBinding.brandName.setOnClickListener { navigator.openProductDetailView(productList,position) }
        mProductListingPageRowBinding.tvRangeName.setOnClickListener { navigator.openProductDetailView(productList,position) }
        mProductListingPageRowBinding.tvProductName.setOnClickListener { navigator.openProductDetailView(productList,position) }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(
            mProductListingPageRowBinding.imProductImage,
            productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85"
        )
    }

    private fun setPromotionalImage(imPromo: PromotionImages?) {
        mProductListingPageRowBinding.apply {
            root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(
                productListingPromotionalImage.imReducedImage,
                imPromo?.reduced ?: ""
            )
            ImageManager.setPictureOverrideWidthHeight(
                productListingPromotionalImage.imSave,
                imPromo?.save ?: ""
            )
            ImageManager.setPictureWithoutPlaceHolder(
                productListingPromotionalImage.imReward,
                imPromo?.wRewards ?: ""
            )
            ImageManager.setPictureWithoutPlaceHolder(
                productListingPromotionalImage.imVitality,
                imPromo?.vitality ?: ""
            )
        }
    }

    private fun setProductName(productList: ProductList?) = mProductListingPageRowBinding.apply {
        tvProductName.maxLines = 3
        tvProductName.minLines = 1
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setBrandText(
        productList: ProductList?,
        nextProduct: ProductList?,
        previousProduct: ProductList?
    ) = mProductListingPageRowBinding.apply {
        brandName?.text = productList?.brandText ?: ""
        previousProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setBrandHeaderDescriptionText(productList: ProductList?) =
        mProductListingPageRowBinding.apply {
            if (TextUtils.isEmpty(productList?.brandHeaderDescription)) {
                tvRangeName?.visibility = View.GONE
            } else {
                tvRangeName?.visibility = View.VISIBLE
                tvRangeName?.text = productList?.brandHeaderDescription
            }
        }

    private fun setPromotionalText(productList: ProductList?) = mProductListingPageRowBinding.apply {
        if (productList?.promotions?.isEmpty() == false) {
            productList?.promotions?.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = View.VISIBLE
                        val promotionsListCount = productList?.promotions?.size ?: 0
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1?.maxLines = 2
                            onlinePromotionalTextView2?.text = ""
                            onlinePromotionalTextView2?.visibility = View.GONE
                        } else
                            onlinePromotionalTextView1?.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = View.VISIBLE
                        onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        }
    }

    private fun setRatingAndReviewCount(productList: ProductList) =
        mProductListingPageRowBinding.apply {
            if (RatingAndReviewUtil.isRatingAndReviewConfigavailbel() &&
                productList.isRnREnabled == true
            ) {
                val ratings: Float = productList.averageRating!!.toFloat()
                if (ratings == 0.0f) {
                    ratingBar.visibility = View.GONE
                    txtRatingCount.visibility = View.GONE
                } else {
                    ratingBar.visibility = View.VISIBLE
                    txtRatingCount.visibility = View.VISIBLE
                    ratingBar.rating =
                        KotlinUtils.getUpdatedUtils(productList.averageRating!!.toFloat())
                    txtRatingCount.text = String.format("(\t%s\t)", productList.reviewCount)
                }

            } else {
                ratingBar.visibility = View.GONE
                txtRatingCount.visibility = View.GONE
            }

        }

    private fun setProductVariant(productList: ProductList?) = mProductListingPageRowBinding.apply {
        val productVarientName = productList?.productVariants ?: ""
        if (!TextUtils.isEmpty(productVarientName)) {
            productVariantTextView?.visibility = View.VISIBLE
            productVariantTextView?.text = productVarientName
        } else {
            productVariantTextView?.visibility = View.GONE
            productVariantTextView?.text = ""
        }
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        mProductListingPageRowBinding.apply {
            root.context?.apply {
                productList?.apply {
                    includeProductListingPriceLayout.imQuickShopAddToCartIcon.visibility = if (productType.equals(
                            RecommendationFragment.ITEM_TYPE_FOOD, ignoreCase = true)) View.VISIBLE else View.GONE
                }
            }
        }
    }
}

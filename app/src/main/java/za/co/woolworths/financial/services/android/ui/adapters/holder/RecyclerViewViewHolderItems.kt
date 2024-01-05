package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductListingPageRowBinding
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.STOCK_AVAILABILITY_0
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.STOCK_AVAILABILITY_MINUS1
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.annotation.meta.When

class RecyclerViewViewHolderItems(val itemBinding: ProductListingPageRowBinding) : RecyclerViewViewHolder(itemBinding.root) {

    fun setProductItem(productList: ProductList, navigator: IProductListing, nextProduct: ProductList? = null, previousProduct: ProductList? = null, position: Int) {
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages,virtualTryOn)
            setProductName(this)
            setBrandText(this, nextProduct, previousProduct)
            setBrandHeaderDescriptionText(this)
            setPromotionalText(this)
            setRatingAndReviewCount(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemBinding.includeProductListingPriceLayout)
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            quickAddToListSwitch(this)
            setOnClickListener(navigator, this,position)
            setNetworkName(this)
            setOutOfStock(this)
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList, position: Int) = itemBinding?.apply {
        tvProductName.setOnClickListener { navigator.openProductDetailView(productList,position) }
        mainImgLayout.setOnClickListener { navigator.openProductDetailView(productList,position) }
        brandName.setOnClickListener { navigator.openProductDetailView(productList,position) }
        tvRangeName.setOnClickListener { navigator.openProductDetailView(productList,position) }
    }

    private fun setNetworkName(productList: ProductList?)= itemBinding.apply {
        if (!TextUtils.isEmpty(productList?.network)) {
            SIMLabel.visibility = VISIBLE
            val networkOperator = productList?.network
            SIMLabel.text = networkOperator?.let { AppConfigSingleton.connectOnline?.freeSimTextMsg?.replace("\${networkOperator}", it) }
        } else {
            SIMLabel.visibility = GONE
            SIMLabel.text = ""
        }
    }

    private fun setProductName(productList: ProductList?) = itemBinding.apply {
        tvProductName?.text = productList?.productName ?: ""
    }


    private fun setRatingAndReviewCount(productList: ProductList) = itemBinding.apply {
       if (RatingAndReviewUtil.isRatingAndReviewConfigavailbel() &&
           productList.isRnREnabled == true
       ) {
               val ratings:Float = productList.averageRating!!.toFloat()
               if (ratings == 0.0f) {
                   ratingBar.visibility = View.GONE
                   txtRatingCount.visibility = View.GONE
               } else {
                   ratingBar.visibility = VISIBLE
                   txtRatingCount.visibility = VISIBLE
                   ratingBar.rating = KotlinUtils.getUpdatedUtils(productList.averageRating!!.toFloat())
                   txtRatingCount.text = String.format("(\t%s\t)",productList.reviewCount)
               }

       }  else {
           ratingBar.visibility = View.GONE
           txtRatingCount.visibility = View.GONE
       }

    }

    private fun setPromotionalText(productList: ProductList?) = itemBinding.apply {
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
                        onlinePromotionalTextView1?.visibility = VISIBLE
                        val promotionsListCount = productList?.promotions?.size ?: 0
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1?.maxLines = 2
                            onlinePromotionalTextView2?.text = ""
                            onlinePromotionalTextView2?.visibility = GONE
                        }
                        else
                            onlinePromotionalTextView1?.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = VISIBLE
                        onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        } else {
            onlinePromotionalTextView1?.text = ""
            onlinePromotionalTextView2?.text = ""
        }
    }

    private fun setProductVariant(productList: ProductList?) = itemBinding.apply {
        val productVarientName = productList?.productVariants ?: ""
        if (!TextUtils.isEmpty(productVarientName)) {
            productVariantTextView?.visibility = VISIBLE
            productVariantTextView?.text = productVarientName
        } else {
            productVariantTextView?.visibility = GONE
            productVariantTextView?.text = ""
        }
    }

    private fun setBrandText(productList: ProductList?, nextProduct: ProductList?, previousProduct: ProductList?) = itemBinding.apply {
        brandName?.text = productList?.brandText ?: ""
        previousProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) GONE else VISIBLE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = GONE
            } else {
                brandName?.visibility = if (productList?.brandText.isNullOrEmpty()) GONE else VISIBLE
            }
        }
    }

    private fun setBrandHeaderDescriptionText(productList: ProductList?) = itemBinding.apply {
        if(TextUtils.isEmpty(productList?.brandHeaderDescription)){
            tvRangeName?.visibility = GONE
        } else {
            tvRangeName?.visibility = VISIBLE
            tvRangeName?.text = productList?.brandHeaderDescription
        }
    }

    private fun setPromotionalImage(imPromo: PromotionImages?,virtualTryOn : String?) {
        itemBinding.apply {
            root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(productListingPromotionalImage.imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(productListingPromotionalImage.imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imNewImage, imPromo?.newImage ?: "")
            ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imageWList, imPromo?.wList ?: "")
            if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                ImageManager.setPictureWithoutPlaceHolder(productListingPromotionalImage.imgTryItOn, virtualTryOn ?: "")
            }
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(itemBinding.imProductImage, productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85")
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        itemBinding.apply {
            root.context?.apply {
                productList?.apply {
                    when (productType) {
                        getString(R.string.food_product_type) -> {
                            when (stockAvailable) {
                                STOCK_AVAILABILITY_MINUS1 -> {
                                    includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = VISIBLE
                                }
                                STOCK_AVAILABILITY_0 -> {
                                    includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = GONE
                                }
                                else -> {
                                    includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = GONE
                                }
                            }
                        }
                        /* commenting it for quick fix in production*/
                       /* getString(R.string.digital_product_type) -> {
                            includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = VISIBLE
                        }*/
                        else -> {
                            includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = GONE
                        }
                    }
                }
            }
        }
    }

    private fun quickAddToListSwitch(productList: ProductList) {
        itemBinding.apply {
            root.context?.apply {
                productList?.apply {
                    when(productType) {
                       getString(R.string.food_product_type) -> {
                           when (stockAvailable) {
                               STOCK_AVAILABILITY_MINUS1 -> {
                                   imAddToList?.visibility = VISIBLE
                               }
                               STOCK_AVAILABILITY_0 -> {
                                   imAddToList?.visibility = GONE
                               }
                               else -> {
                                   imAddToList?.visibility = GONE
                               }
                           }
                        }
                        getString(R.string.digital_product_type) -> {
                            imAddToList?.visibility = VISIBLE
                        }
                        else -> {
                            imAddToList?.visibility = GONE
                        }
                    }
                }
            }
        }
    }
    private fun setOutOfStock(productList: ProductList) {
        itemBinding.apply {
            root.context.apply {
                productList.apply {
                    when(productType) {
                        getString(R.string.food_product_type) -> {
                            when (stockAvailable) {
                                STOCK_AVAILABILITY_MINUS1 -> {
                                    outOfStockTag.visibility = GONE
                                }
                                STOCK_AVAILABILITY_0 -> {
                                    outOfStockTag.visibility = VISIBLE
                                    imProductImage.alpha = 0.5f
                                    productListingPromotionalImage.root.visibility = View.GONE
                                }
                                else -> {
                                    outOfStockTag.visibility = GONE
                                }
                            }
                        }
                        getString(R.string.digital_product_type) -> {
                            outOfStockTag.visibility = GONE
                        }
                        else -> {
                            outOfStockTag.visibility = GONE
                        }
                    }
                }
            }
        }
    }

    companion object {
        // Extracting the fulfilmentStoreId from user location or default MC config
        fun getFulFillmentStoreId(fulfilmentTypeId: String): String {
            var defaultStoreId = ""
            Utils.retrieveStoreId(fulfilmentTypeId)?.let { defaultStoreId = it }
            return defaultStoreId
        }
    }
}

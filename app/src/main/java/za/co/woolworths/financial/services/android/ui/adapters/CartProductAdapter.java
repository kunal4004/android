package za.co.woolworths.financial.services.android.ui.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.CommerceItemInfo;
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.CartUtils;

import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.ImageManager;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;

public class CartProductAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

    private final float DISABLE_VIEW_VALUE = 0.5f;
    private final String GIFT_ITEM = "GIFT";
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    private enum CartRowType {
        HEADER(0), PRODUCT(1), PRICES(2), GIFT(3);

        public final int value;

        CartRowType(int value) {
            this.value = value;
        }
    }

    public interface OnItemClick {
        void onItemDeleteClickInEditMode(CommerceItem commerceId);

        void onChangeQuantity(CommerceItem commerceId);

        void totalItemInBasket(int total);

        void onOpenProductDetail(CommerceItem commerceItem);

        void onViewVouchers();

        void updateOrderTotal();

        void onGiftItemClicked(CommerceItem commerceItem);

        void onEnterPromoCode();

        void onRemovePromoCode(String promoCode);

        void onPromoDiscountInfo();

        void onItemDeleteClick(CommerceItem commerceId);
    }

    private OnItemClick onItemClick;
    private HashMap<String, ArrayList<ProductList>> productCategoryItems;
    private boolean editMode = false;
    private boolean firstLoadCompleted = false;
    private ArrayList<CartItemGroup> cartItems;
    private OrderSummary orderSummary;
    private LiquorCompliance liquorComplianceInfo;
    private Activity mContext;
    private VoucherDetails voucherDetails;

    public CartProductAdapter(ArrayList<CartItemGroup> cartItems, OnItemClick onItemClick, OrderSummary orderSummary, Activity context, VoucherDetails voucherDetails,LiquorCompliance liquorCompliance) {
        this.cartItems = cartItems;
        this.onItemClick = onItemClick;
        this.orderSummary = orderSummary;
        this.mContext = context;
        this.voucherDetails = voucherDetails;
        this.liquorComplianceInfo=liquorCompliance;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CartRowType.HEADER.value) {
            return new CartHeaderViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_product_header_item, parent, false));
        } else if (viewType == CartRowType.PRODUCT.value) {
            return new ProductHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_product_item, parent, false));
        } else if (viewType == CartRowType.GIFT.value) {
            return new GiftProductHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_gift_item, parent, false));
        } else {
            return new CartPricesViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_product_basket_prices, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CartcommerceItemRow itemRow = getItemTypeAtPosition(position);
        switch (itemRow.rowType) {
            case HEADER:
                CartHeaderViewHolder headerHolder = ((CartHeaderViewHolder) holder);
                ArrayList<CommerceItem> commerceItems = itemRow.commerceItems;
                headerHolder.tvHeaderTitle.setText(commerceItems.size() > 1 ? commerceItems.size() + " " + itemRow.category.toUpperCase() + " ITEMS" : commerceItems.size() + " " + itemRow.category.toUpperCase() + " ITEM");
                headerHolder.addToListListener(commerceItems);
                if (itemRow.category.toUpperCase().equalsIgnoreCase(GIFT_ITEM)){
                    headerHolder.tvAddToList.setVisibility(View.GONE);
                }else {
                    headerHolder.tvAddToList.setVisibility(View.VISIBLE);
                    headerHolder.tvAddToList.setVisibility(this.editMode ? View.INVISIBLE : View.VISIBLE);
                }
                break;
            case PRODUCT:
                final ProductHolder productHolder = ((ProductHolder) holder);
                final CommerceItem commerceItem = itemRow.commerceItem;
                CommerceItemInfo commerceItemInfo;
                if (commerceItem == null) return;
                productHolder.swipeLayout.setRightSwipeEnabled(false);
                productHolder.swipeLayout.setLeftSwipeEnabled(false);
                commerceItemInfo = commerceItem.commerceItemInfo;
                productHolder.tvTitle.setText((commerceItemInfo == null) ? "" : commerceItemInfo.getProductDisplayName());
                Utils.truncateMaxLine(productHolder.tvTitle);
                productHolder.quantity.setText((commerceItemInfo == null) ? "" : String.valueOf(commerceItemInfo.getQuantity()));
                productHolder.price.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(commerceItem.getPriceInfo().getAmount()));
                String productImageUrl = (commerceItemInfo == null) ? "" : commerceItemInfo.externalImageRefV2;
                ImageManager.Companion.setPicture(productHolder.productImage, productImageUrl);
                productHolder.btnDeleteRow.setVisibility(this.editMode ? View.VISIBLE : View.GONE);
                productHolder.rlDeleteButton.setVisibility(this.editMode ? View.VISIBLE : View.GONE);
                productHolder.rlDelete.setVisibility(this.editMode ? View.GONE : View.VISIBLE);
                onRemoveSingleItemInEditMode(productHolder, commerceItem);
                onRemoveSingleItem(productHolder, commerceItem);
                //enable/disable change quantity click
                productHolder.llQuantity.setEnabled(!this.editMode);
                Utils.fadeInFadeOutAnimation(productHolder.llQuantity, this.editMode);
                productHolder.llQuantity.setEnabled(!commerceItem.isDeletePressed());
                productHolder.btnDelete.setVisibility(!commerceItem.isDeletePressed() ? View.VISIBLE : View.GONE);
                productHolder.pbDelete.setVisibility(commerceItem.isDeletePressed() ? View.VISIBLE : View.GONE);
                // prevent triggering animation on first load
                if (firstLoadWasCompleted())
                    animateOnDeleteButtonVisibility(productHolder.llCartItems, this.editMode);

                boolean quantityIsLoading = commerceItem.getQuantityUploading();
                productHolder.pbQuantity.setVisibility(quantityIsLoading ? View.VISIBLE : View.GONE);
                productHolder.quantity.setVisibility(quantityIsLoading ? View.GONE : View.VISIBLE);
                productHolder.imPrice.setVisibility(quantityIsLoading ? View.GONE : View.VISIBLE);

                //Set Promotion Text START
                if (commerceItem.getPriceInfo().getDiscountedAmount() > 0) {
                    productHolder.promotionalText.setText(" " + CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(commerceItem.getPriceInfo().getDiscountedAmount()));
                    productHolder.llPromotionalText.setVisibility(View.VISIBLE);
                } else {
                    productHolder.llPromotionalText.setVisibility(View.GONE);
                }
                //Set Promotion Text END

                // Set Color and Size START
                if (itemRow.category.equalsIgnoreCase("FOOD")) {
                    productHolder.tvColorSize.setVisibility(View.INVISIBLE);
                } else {
                    String sizeColor = getSizeColor(commerceItemInfo);
                    productHolder.tvColorSize.setText(sizeColor);
                    productHolder.tvColorSize.setVisibility(View.VISIBLE);
                }
                // Set Color and Size END
                productHolder.pbQuantity.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

                productHolder.llQuantity.setAlpha(commerceItem.isStockChecked ? 1.0f : DISABLE_VIEW_VALUE);
                if (commerceItem.isStockChecked) {
                    productHolder.llQuantity.setAlpha((commerceItem.quantityInStock == 0) ? 0.0f : 1.0f);
                    productHolder.tvProductAvailability.setVisibility((commerceItem.quantityInStock == 0) ? View.VISIBLE : View.GONE);
                    Utils.setBackgroundColor(productHolder.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock);
                    if (commerceItem.quantityInStock == 0) {
                        productHolder.llPromotionalText.setVisibility(View.GONE);
                        productHolder.price.setVisibility(View.VISIBLE);
                    } else if (commerceItem.quantityInStock == -1) {
                        productHolder.llQuantity.setAlpha(DISABLE_VIEW_VALUE);
                        productHolder.price.setVisibility(View.VISIBLE);
                        productHolder.llQuantity.setEnabled(false);
                        productHolder.quantity.setAlpha(DISABLE_VIEW_VALUE);
                        productHolder.imPrice.setAlpha(DISABLE_VIEW_VALUE);
                    } else {
                        productHolder.price.setVisibility(View.VISIBLE);
                    }
                } else {
                    productHolder.llQuantity.setVisibility(View.VISIBLE);
                    productHolder.tvProductAvailability.setVisibility(View.GONE);
                }

                productHolder.btnDeleteRow.setOnClickListener(v -> {
                    setFirstLoadCompleted(false);
                    commerceItem.commerceItemDeletedId(commerceItem);
                    commerceItem.setDeleteIconWasPressed(true);
                    notifyItemRangeChanged(productHolder.getAdapterPosition(), cartItems.size());
                });

                productHolder.llQuantity.setOnClickListener(view -> {
                    if (commerceItem.quantityInStock == 0) return;
                    if (!NetworkManager.getInstance().isConnectedToNetwork(mContext)) {
                        new ErrorHandlerView(mContext).showToast();
                        return;
                    }
                    commerceItem.setQuantityUploading(true);
                    setFirstLoadCompleted(false);
                    onItemClick.onChangeQuantity(commerceItem);
                });

                productHolder.btnDelete.setOnClickListener(v -> {
                    commerceItem.commerceItemDeletedId(commerceItem);
                    commerceItem.setDeletePressed(true);
                    notifyItemRangeChanged(productHolder.getAdapterPosition(), cartItems.size());
                });

                productHolder.swipeLayout.setOnClickListener(view -> onItemClick.onOpenProductDetail(commerceItem));
                if (commerceItem.lowStockThreshold > commerceItem.quantityInStock
                        && commerceItem.quantityInStock > 0 && AppConfigSingleton.INSTANCE.getLowStock().isEnabled()) {
                    showLowStockIndicator(productHolder);
                }
                mItemManger.bindView(productHolder.itemView, position);
                break;

            case GIFT:
                final GiftProductHolder giftProductHolder = ((GiftProductHolder) holder);
                final CommerceItem giftCommerceItem = itemRow.commerceItem;
                if (giftCommerceItem == null) return;
                CommerceItemInfo giftCommerceItemInfo = giftCommerceItem.commerceItemInfo;
                String imageUrl = (giftCommerceItemInfo == null) ? "" : giftCommerceItemInfo.externalImageRefV2;
                ImageManager.Companion.setPicture(giftProductHolder.giftItemImageView, imageUrl);
                giftProductHolder.productNameTextView.setText(giftCommerceItemInfo.getProductDisplayName());
                Utils.truncateMaxLine(giftProductHolder.productNameTextView);
                String sizeColor = getSizeColor(giftCommerceItemInfo);
                giftProductHolder.brandProductDescriptionTextView.setText(sizeColor);
                giftProductHolder.giftRootContainerConstraintLayout.setOnClickListener(v -> onItemClick.onGiftItemClicked(giftCommerceItem));
                break;

            case PRICES:
                CartPricesViewHolder priceHolder = ((CartPricesViewHolder) holder);
                if (orderSummary != null) {
                    priceHolder.orderSummeryLayout.setVisibility(View.VISIBLE);
                    setPriceValue(priceHolder.txtYourCartPrice, orderSummary.getBasketTotal());

                    if(orderSummary.discountDetails!=null){
                        DiscountDetails discountDetails = orderSummary.discountDetails;
                        if (discountDetails.getCompanyDiscount() > 0) {
                            setDiscountPriceValue(priceHolder.txtCompanyDiscount, discountDetails.getCompanyDiscount());
                            priceHolder.rlCompanyDiscount.setVisibility(View.VISIBLE);
                        } else {
                            priceHolder.rlCompanyDiscount.setVisibility(View.GONE);
                        }

                        if (discountDetails.getTotalOrderDiscount() > 0) {
                            setDiscountPriceValue(priceHolder.txtTotalDiscount, discountDetails.getTotalOrderDiscount());
                            priceHolder.rlTotalDiscount.setVisibility(View.VISIBLE);
                        } else {
                            priceHolder.rlTotalDiscount.setVisibility(View.GONE);
                        }

                        if (discountDetails.getOtherDiscount() > 0) {
                            setDiscountPriceValue(priceHolder.txtDiscount, discountDetails.getOtherDiscount());
                            priceHolder.rlDiscount.setVisibility(View.VISIBLE);
                        } else {
                            priceHolder.rlDiscount.setVisibility(View.GONE);
                        }

                        if (discountDetails.getVoucherDiscount() > 0) {
                            setDiscountPriceValue(priceHolder.txtWrewardsDiscount, discountDetails.getVoucherDiscount());
                            priceHolder.rlWrewardsDiscount.setVisibility(View.VISIBLE);
                        } else {
                            priceHolder.rlWrewardsDiscount.setVisibility(View.GONE);
                        }

                        if (discountDetails.getPromoCodeDiscount() > 0) {
                            setDiscountPriceValue(priceHolder.txtPromoCodeDiscount, discountDetails.getPromoCodeDiscount());
                            priceHolder.rlPromoCodeDiscount.setVisibility(View.VISIBLE);
                        } else {
                            priceHolder.rlPromoCodeDiscount.setVisibility(View.GONE);
                        }

                    }
                } else {
                    priceHolder.orderSummeryLayout.setVisibility(View.GONE);
                }

                priceHolder.viewVouchers.setOnClickListener(v -> {
                            onItemClick.onViewVouchers();
                            Utils.triggerFireBaseEvents(getAppliedVouchersCount() > 0 ? FirebaseManagerAnalyticsProperties.Cart_ovr_edit : FirebaseManagerAnalyticsProperties.Cart_ovr_view, mContext);
                        }
                );
                int activeVouchersCount = voucherDetails.getActiveVouchersCount();
                if (activeVouchersCount > 0) {
                    if (getAppliedVouchersCount() > 0) {
                        String availableVouchersLabel = getAppliedVouchersCount() + mContext.getString(getAppliedVouchersCount() == 1 ? R.string.available_voucher_toast_message : R.string.available_vouchers_toast_message) + mContext.getString(R.string.applied);
                        priceHolder.availableVouchersCount.setText(availableVouchersLabel);
                        priceHolder.viewVouchers.setText(mContext.getString(R.string.edit));
                        priceHolder.viewVouchers.setEnabled(true);
                    } else {
                        String availableVouchersLabel = activeVouchersCount + mContext.getString(voucherDetails.getActiveVouchersCount() == 1 ? R.string.available_voucher_toast_message : R.string.available_vouchers_toast_message) + mContext.getString(R.string.available);
                        priceHolder.availableVouchersCount.setText(availableVouchersLabel);
                        priceHolder.viewVouchers.setText(mContext.getString(R.string.view));
                        priceHolder.viewVouchers.setEnabled(true);
                    }
                } else {
                    priceHolder.availableVouchersCount.setText(mContext.getString(R.string.no_vouchers_available));
                    priceHolder.viewVouchers.setText(mContext.getString(R.string.view));
                    priceHolder.viewVouchers.setEnabled(false);
                }
                priceHolder.promoCodeAction.setText(mContext.getString((voucherDetails.getPromoCodes() != null && voucherDetails.getPromoCodes().size() > 0) ? R.string.remove : R.string.enter));

                if (voucherDetails.getPromoCodes() != null && voucherDetails.getPromoCodes().size() > 0) {
                    String appliedPromoCodeText = mContext.getString(R.string.promo_code_applied) + voucherDetails.getPromoCodes().get(0).getPromoCode();
                    priceHolder.promoCodeLabel.setText(appliedPromoCodeText);
                } else {
                    priceHolder.promoCodeLabel.setText(mContext.getString(R.string.do_you_have_a_promo_code));
                }

                priceHolder.promoCodeAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (voucherDetails.getPromoCodes() != null && voucherDetails.getPromoCodes().size() > 0)
                            onItemClick.onRemovePromoCode(voucherDetails.getPromoCodes().get(0).getPromoCode());
                        else
                            onItemClick.onEnterPromoCode();
                    }
                });

                priceHolder.promoDiscountInfo.setOnClickListener(v -> {
                        onItemClick.onPromoDiscountInfo();
                });
                if(liquorComplianceInfo!=null&&liquorComplianceInfo.isLiquorOrder()){
                    priceHolder.liquorBannerRootConstraintLayout.setVisibility(View.VISIBLE);
                   if(AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl()!=null&&!AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl().isEmpty())
                    ImageManager.Companion.setPicture(priceHolder.imgLiBanner, AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl());
                }else{
                    priceHolder.liquorBannerRootConstraintLayout.setVisibility(View.GONE);
                }
                break;


            default:
                break;
        }
    }

    /**
     * This method used to show low stock indicator
     * When lowStockThreshold > quantity
     * @param productHolder
     */
    private void showLowStockIndicator(ProductHolder productHolder) {
        productHolder.cartLowStock.setVisibility(View.VISIBLE);
        productHolder.txtCartLowStock.setText(AppConfigSingleton.INSTANCE.getLowStock().getLowStockCopy());

    }

    private String getSizeColor(CommerceItemInfo commerceItemInfo) {
        String sizeColor = (commerceItemInfo == null) ? "" : commerceItemInfo.getColor();
        if (sizeColor == null)
            sizeColor = "";
        if (commerceItemInfo != null) {
            if (sizeColor.isEmpty() && !commerceItemInfo.getSize().isEmpty() && !commerceItemInfo.getSize().equalsIgnoreCase("NO SZ"))
                sizeColor = commerceItemInfo.getSize();
            else if (!sizeColor.isEmpty() && !commerceItemInfo.getSize().isEmpty() && !commerceItemInfo.getSize().equalsIgnoreCase("NO SZ"))
                sizeColor = sizeColor + ", " + commerceItemInfo.getSize();
        }
        return sizeColor;
    }

    private void onRemoveSingleItemInEditMode(final ProductHolder productHolder, final CommerceItem commerceItem) {
        if (this.editMode) {
            if (commerceItem.deleteIconWasPressed()) {
                Animation animateRowToDelete = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.animate_layout_delete);
                animateRowToDelete.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        productHolder.pbDeleteProgress.setVisibility(commerceItem.deleteIconWasPressed() ? View.VISIBLE : View.GONE);
                        productHolder.btnDeleteRow.setVisibility(commerceItem.deleteIconWasPressed() ? View.GONE : View.VISIBLE);
                        onItemClick.onItemDeleteClickInEditMode(commerceItem.getDeletedCommerceItemId());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                productHolder.llCartItems.startAnimation(animateRowToDelete);
            } else {
                productHolder.pbDeleteProgress.setVisibility(View.GONE);
            }
        } else {
            productHolder.pbDeleteProgress.setVisibility(View.GONE);
        }
    }

    private void onRemoveSingleItem(final ProductHolder productHolder, final CommerceItem commerceItem) {
        if (commerceItem.isDeletePressed()) {
            Animation animateRowToDelete = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.animate_layout_delete);
            animateRowToDelete.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    productHolder.pbDelete.setVisibility(commerceItem.isDeletePressed() ? View.VISIBLE : View.GONE);
                    productHolder.btnDelete.setVisibility(commerceItem.isDeletePressed() ? View.GONE : View.VISIBLE);
                    onItemClick.onItemDeleteClick(commerceItem.getDeletedCommerceItemId());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            productHolder.llCartItems.startAnimation(animateRowToDelete);
        }
    }

    private void setPriceValue(WTextView textView, double value) {
        textView.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(value));
    }

    private void setDiscountPriceValue(WTextView textView, double value) {
        textView.setText("- " + CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(value));
    }

    @Override
    public int getItemCount() {
        Integer size = cartItems.size();
        for (CartItemGroup collection : cartItems) {
            size += collection.getCommerceItems().size();
        }
        if (editMode) {
            // returns sum of headers + product items
            return size;
        } else {
            // returns sum of headers + product items + last row for prices
            return size + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemTypeAtPosition(position).rowType.value;
    }

    private CartcommerceItemRow getItemTypeAtPosition(int position) {
        int currentPosition = 0;
        for (CartItemGroup entry : cartItems) {
            if (currentPosition == position) {
                return new CartcommerceItemRow(CartRowType.HEADER, entry.type, null, entry.getCommerceItems());
            }

            // increment position for header
            currentPosition++;

            ArrayList<CommerceItem> productCollection = entry.commerceItems;

            if (position > currentPosition + productCollection.size() - 1) {
                currentPosition += productCollection.size();
            } else {
                if (entry.type.equalsIgnoreCase(GIFT_ITEM))
                    return new CartcommerceItemRow(CartRowType.GIFT, entry.type, productCollection.get(position - currentPosition), null);
                else
                    return new CartcommerceItemRow(CartRowType.PRODUCT, entry.type, productCollection.get(position - currentPosition), null);
            }
        }
        // last row is for prices
        return new CartcommerceItemRow(CartRowType.PRICES, null, null, null);
    }

    public boolean toggleEditMode() {
        editMode = !editMode;
        toggleFirstLoad();
        notifyDataSetChanged();
        return editMode;
    }

    public boolean toggleFirstLoad() {
        setFirstLoadCompleted(true);
        return firstLoadCompleted;
    }

    public void toggleDeleteSingleItem(CommerceItem commerceItem) {
        for (CartItemGroup cartItemGroup : this.cartItems) {
            ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
            if (commerceItemList != null) {
                for (CommerceItem cm : commerceItemList) {
                    if (cm.commerceItemInfo.getCommerceId().equalsIgnoreCase(commerceItem.commerceItemInfo.getCommerceId())) {
                        boolean deleteSingleItem = !commerceItem.deleteSingleItem();
                        commerceItem.setDeleteSingleItem(deleteSingleItem);
                        notifyDataSetChanged();
                    }
                }
            }
        }
    }

    public void clear() {
        this.cartItems.clear();
        this.orderSummary = null;
        notifyDataSetChanged();
    }

    private class CartHeaderViewHolder extends RecyclerView.ViewHolder {
        private WTextView tvHeaderTitle;
        private WTextView tvAddToList;

        public CartHeaderViewHolder(View view) {
            super(view);
            tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
            tvAddToList = view.findViewById(R.id.tvAddToList);
        }

        public void addToListListener(final ArrayList<CommerceItem> commerceItems) {
            tvAddToList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = tvAddToList.getContext();
                    if (context == null) return;
                    WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
                    if (woolworthsApplication != null) {
                        woolworthsApplication.getWGlobalState().setSelectedSKUId(null);
                    }
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTADDTOLIST, mContext);

                    ArrayList<AddToListRequest> addToListRequests = new ArrayList<>();
                    for (CommerceItem commerceItem : commerceItems) {
                        AddToListRequest listItem = new AddToListRequest();
                        CommerceItemInfo commerceItemInfo = commerceItem.commerceItemInfo;
                        listItem.setCatalogRefId(commerceItemInfo.catalogRefId);
                        listItem.setSkuID(commerceItemInfo.catalogRefId);
                        listItem.setGiftListId(commerceItemInfo.catalogRefId);
                        listItem.setQuantity("1");
                        addToListRequests.add(listItem);
                    }
                    NavigateToShoppingList.Companion.openShoppingList(mContext, addToListRequests, "", false);
                }
            });
        }
    }

    public class ProductHolder extends RecyclerView.ViewHolder {
        private WTextView tvTitle, tvColorSize, quantity, price, promotionalText;
        private ImageView btnDeleteRow;
        private ImageView imPrice;
        private RelativeLayout llQuantity;
        private ImageView productImage;
        private LinearLayout llCartItems, llPromotionalText;
        private WTextView tvDelete;
        private ProgressBar pbQuantity;
        private ProgressBar pbDeleteProgress;
        private RelativeLayout rlDeleteButton;
        private WTextView tvProductAvailability;
        private SwipeLayout swipeLayout;
        private ImageView btnDelete;
        private ProgressBar pbDelete;
        private RelativeLayout rlDelete;
        private View cartLowStock;
        private TextView txtCartLowStock;

        public ProductHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvColorSize = view.findViewById(R.id.tvSize);
            quantity = view.findViewById(R.id.tvQuantity);
            price = view.findViewById(R.id.price);
            btnDeleteRow = view.findViewById(R.id.btnDeleteRow);
            productImage = view.findViewById(R.id.cartProductImage);
            llQuantity = view.findViewById(R.id.llQuantity);
            pbQuantity = view.findViewById(R.id.pbQuantity);
            pbDeleteProgress = view.findViewById(R.id.pbDeleteProgress);
            imPrice = view.findViewById(R.id.imPrice);
            llCartItems = view.findViewById(R.id.llCartItems);
            tvDelete = view.findViewById(R.id.tvDelete);
            promotionalText = view.findViewById(R.id.promotionalText);
            llPromotionalText = view.findViewById(R.id.promotionalTextLayout);
            rlDeleteButton = view.findViewById(R.id.rlDeleteButton);
            tvProductAvailability = view.findViewById(R.id.tvProductAvailability);
            swipeLayout = view.findViewById(R.id.swipe);
            btnDelete = view.findViewById(R.id.btnDelete);
            rlDelete = view.findViewById(R.id.rlDelete);
            pbDelete = view.findViewById(R.id.pbDelete);
            cartLowStock = view.findViewById(R.id.cartLowStock);
            txtCartLowStock = view.findViewById(R.id.txtCartLowStock);

        }
    }

    private class CartPricesViewHolder extends RecyclerView.ViewHolder {
        private WTextView txtYourCartPrice, txtDiscount, txtCompanyDiscount, txtWrewardsDiscount, txtTotalDiscount, txtPromoCodeDiscount ;
        private LinearLayout orderSummeryLayout;
        private RelativeLayout rlDiscount, rlCompanyDiscount, rlWrewardsDiscount, rlTotalDiscount, rlPromoCodeDiscount;
        private TextView availableVouchersCount, viewVouchers, promoCodeAction, promoCodeLabel;
        private ImageView promoDiscountInfo;
        private ConstraintLayout liquorBannerRootConstraintLayout;
        private ImageView imgLiBanner;


        public CartPricesViewHolder(View view) {
            super(view);
            txtYourCartPrice = view.findViewById(R.id.txtYourCartPrice);
            orderSummeryLayout = view.findViewById(R.id.orderSummeryLayout);
            rlCompanyDiscount = view.findViewById(R.id.rlCompanyDiscount);
            availableVouchersCount = view.findViewById(R.id.availableVouchersCount);
            viewVouchers = view.findViewById(R.id.viewVouchers);
            txtDiscount = view.findViewById(R.id.txtDiscount);
            txtCompanyDiscount = view.findViewById(R.id.txtCompanyDiscount);
            txtWrewardsDiscount = view.findViewById(R.id.txtWrewardsDiscount);
            txtTotalDiscount = view.findViewById(R.id.txtTotalDiscount);
            rlDiscount = view.findViewById(R.id.rlDiscount);
            rlCompanyDiscount = view.findViewById(R.id.rlCompanyDiscount);
            rlWrewardsDiscount = view.findViewById(R.id.rlWrewardsDiscount);
            rlTotalDiscount = view.findViewById(R.id.rlTotalDiscount);
            promoCodeAction = view.findViewById(R.id.promoCodeAction);
            promoCodeLabel = view.findViewById(R.id.promoCodeLabel);
            txtPromoCodeDiscount = view.findViewById(R.id.txtPromoCodeDiscount);
            rlPromoCodeDiscount = view.findViewById(R.id.rlPromoCodeDiscount);
            promoDiscountInfo = view.findViewById(R.id.promoDiscountInfo);
            liquorBannerRootConstraintLayout=view.findViewById(R.id.liquorBannerRootConstraintLayout);
            imgLiBanner=view.findViewById(R.id.imgLiquorBanner);

        }
    }

    public class GiftProductHolder extends RecyclerView.ViewHolder {

        private ImageView giftItemImageView;
        private TextView productNameTextView;
        private TextView brandProductDescriptionTextView;
        private ConstraintLayout giftRootContainerConstraintLayout;

        GiftProductHolder(View view) {
            super(view);
            giftItemImageView = view.findViewById(R.id.giftItemImageView);
            productNameTextView = view.findViewById(R.id.productNameTextView);
            brandProductDescriptionTextView = view.findViewById(R.id.brandProductDescriptionTextView);
            giftRootContainerConstraintLayout = view.findViewById(R.id.giftRootContainerConstraintLayout);
        }
    }

    public class CartcommerceItemRow {
        private CartRowType rowType;
        private String category;
        private CommerceItem commerceItem;
        private ArrayList<CommerceItem> commerceItems;

        CartcommerceItemRow(CartRowType rowType, String category, CommerceItem commerceItem, ArrayList<CommerceItem> commerceItems) {
            this.rowType = rowType;
            this.category = category;
            this.commerceItem = commerceItem;
            this.commerceItems = commerceItems;
        }
    }


    public void notifyAdapter(ArrayList<CartItemGroup> cartItems,
                              OrderSummary orderSummary, VoucherDetails voucherDetails) {
        this.cartItems = cartItems;
        this.orderSummary = orderSummary;
        this.voucherDetails = voucherDetails;
        resetQuantityState(false);
        notifyDataSetChanged();
        onItemClick.updateOrderTotal();
    }

    public void onChangeQuantityComplete() {
        resetQuantityState(false);
        notifyDataSetChanged();
    }

    public void onChangeQuantityLoad(CommerceItem mCommerceItem) {
        for (CartItemGroup cartItemGroup : this.cartItems) {
            ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
            if (commerceItemList != null) {
                for (CommerceItem cm : commerceItemList) {
                    if (cm == mCommerceItem) {
                        cm.setQuantityUploading(true);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void onChangeQuantityError() {
        resetQuantityState(true);
        notifyDataSetChanged();
    }

    public void onChangeQuantityLoad() {
        notifyDataSetChanged();
    }

    private void animateOnDeleteButtonVisibility(View view, boolean animate) {
        if (mContext != null) {
            int width = getWidthAndHeight(mContext);
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", animate ? -width : width, 1f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(300);
            animator.start();
        }
    }

    private int getWidthAndHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels / 10;
    }

    public void onPopUpCancel(String status) {
        switch (status) {
            case CANCEL_DIALOG_TAPPED:
                resetQuantityState(true);
                notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    private void resetQuantityState(boolean refreshQuantity) {
        for (CartItemGroup cartItemGroup : this.cartItems) {
            ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
            if (commerceItemList != null) {
                for (CommerceItem cm : commerceItemList) {
                    if (refreshQuantity)
                        cm.setQuantityUploading(false);
                    setFirstLoadCompleted(false);
                }
            }
        }
    }

    private void setFirstLoadCompleted(boolean firstLoadCompleted) {
        this.firstLoadCompleted = firstLoadCompleted;
    }

    private boolean firstLoadWasCompleted() {
        return firstLoadCompleted;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (cartItems != null)
            notifyItemRangeChanged(0, cartItems.size());
    }

    public void updateStockAvailability(ArrayList<CartItemGroup> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public ArrayList<CartItemGroup> getCartItems() {
        return cartItems;
    }

    public int getAppliedVouchersCount() {
        return CartUtils.Companion.getAppliedVouchersCount(voucherDetails.getVouchers());
    }
}
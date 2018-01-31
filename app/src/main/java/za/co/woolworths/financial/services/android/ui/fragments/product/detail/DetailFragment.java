package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductViewDetailBinding;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class DetailFragment extends BaseFragment<ProductViewDetailBinding, DetailViewModel> implements DetailNavigator, ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener {

	private DetailViewModel detailViewModel;
	private String mDefaultProductList;
	private List<String> mAuxiliaryImage = new ArrayList<>();
	private String mSubCategoryTitle;
	public final int IMAGE_QUALITY = 85;
	private ProductList mDefaultProduct;

	@Override
	public DetailViewModel getViewModel() {
		return detailViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.product_view_detail;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		detailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
		getViewModel().setNavigator(this);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mDefaultProductList = bundle.getString("strProductList");
			mSubCategoryTitle = bundle.getString("strProductCategory");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		slideBottomPanel();
		nestedScrollViewHelper();
		defaultProduct();
		setUpImageViewPager();

		onLoadStart();
	}

	@Override
	public void renderView() {
		ImageView imCloseProduct = getViewDataBinding().imCloseProduct;
		imCloseProduct.setOnClickListener(this);
	}

	@Override
	public void closeView(View view) {
		getBottomNavigator().closeSlideUpPanel();
	}

	@Override
	public void nestedScrollViewHelper() {
		getBottomNavigator().scrollableViewHelper(getViewDataBinding().scrollProductDetail);
	}

	@Override
	public void setUpImageViewPager() {
		ProductViewPagerAdapter mProductViewPagerAdapter = new ProductViewPagerAdapter(getActivity(), getAuxiliaryImage(), this);
		getViewDataBinding().mProductDetailPager.setAdapter(mProductViewPagerAdapter);
		mProductViewPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void defaultProduct() {
		getViewModel().setDefaultProduct(mDefaultProductList);
		mDefaultProduct = getViewModel().getDefaultProduct();
		String externalImageRef = getImageByWidth(mDefaultProduct.externalImageRef, getActivity());
		mAuxiliaryImage.add(externalImageRef);
		getViewModel().setAuxiliaryImage(mAuxiliaryImage);

		//set product name
		setProductName();

		//set sub category title
		setSubCategoryTitle();

		// set price list
		ProductUtils.gridPriceList(getViewDataBinding().textPrice, getViewDataBinding().textActualPrice,
				String.valueOf(mDefaultProduct.fromPrice), ProductUtils.maxWasPrice(mDefaultProduct.otherSkus));

		//set promotional Images
		ProductUtils.showPromotionalImages(getViewDataBinding().imSave, getViewDataBinding().imReward, getViewDataBinding().imVitality, getViewDataBinding().imVitality, mDefaultProduct.promotionImages);

		//set promotional text
		setText(mDefaultProduct.saveText, getViewDataBinding().tvSaveText);

		getViewDataBinding().btnAddShoppingList.setOnClickListener(this);

	}

	private void setSubCategoryTitle() {
		getViewDataBinding().tvSubCategoryTitle.setText(mSubCategoryTitle);

	}

	@Override
	public List<String> getAuxiliaryImage() {
		return getViewModel().getAuxiliaryImage();
	}

	@Override
	public void SelectedImage(int position, View view) {

	}

	@Override
	public void setProductName() {
		getViewDataBinding().tvProductName.setText(getViewModel().getDefaultProduct().productName);
	}

	@Override
	public void onLoadStart() {

		// hide color and size view
		hideView(getViewDataBinding().incProductColor);
		hideView(getViewDataBinding().incProductColorBottomLine);

		// hide ingredient
		hideView(getViewDataBinding().ingredientLine);
		hideView(getViewDataBinding().linIngredient);

		// load product info
		setText(getViewDataBinding().productCode, getString(R.string.loading_product_info));


	}

	@Override
	public void onLoadComplete() {

	}

	@Override
	public void addToShoppingList() {
		Utils.addToShoppingCart(getActivity(), new ShoppingList(
				mDefaultProduct.productId,
				mDefaultProduct.productName, false));
		Utils.displayValidationMessage(getActivity(),
				CustomPopUpWindow.MODAL_LAYOUT.SHOPPING_LIST_INFO,
				"viewShoppingList");
	}

	@Override
	public String getImageByWidth(String imageUrl, Context context) {
		WindowManager display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
		Display disp = display.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		int width = size.x;
		return imageUrl + "?w=" + width + "&q=" + IMAGE_QUALITY;
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.imCloseProduct:
				closeView(view);
				break;

			case R.id.btnAddShoppingList:
				addToShoppingList();
				break;
		}
	}
}

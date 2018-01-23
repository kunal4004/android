package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductViewDetailBinding;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

public class DetailFragment extends BaseFragment<ProductViewDetailBinding, DetailViewModel> implements DetailNavigator, ProductViewPagerAdapter.MultipleImageInterface {

	private DetailViewModel detailViewModel;
	private String mDefaultProductList;
	private final int IMAGE_QUALITY = 85;
	private List<String> mAuxiliaryImage = new ArrayList<>();
	private String mSubCategoryTitle;

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
	}

	@Override
	public void closeView(View view) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			((BottomNavigationActivity) activity).closeSlideUpPanel();
		}
	}

	@Override
	public void nestedScrollViewHelper() {
		Activity activity = getBaseActivity();
		if (activity != null) {
			((BottomNavigationActivity) activity).scrollableViewHelper(getViewDataBinding().scrollProductDetail);
		}
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
		ProductList defaultProduct = getViewModel().getDefaultProduct();
		String externalImageRef = getImageByWidth(defaultProduct.externalImageRef, getActivity());
		mAuxiliaryImage.add(externalImageRef);
		getViewModel().setAuxiliaryImage(mAuxiliaryImage);

		setProductName();
		setSubCategoryTitle();
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
	public String getImageByWidth(String imageUrl, Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		return imageUrl + "?w=" + width + "&q=" + IMAGE_QUALITY;
	}
}

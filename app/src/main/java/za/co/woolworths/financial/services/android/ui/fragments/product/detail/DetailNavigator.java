package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.content.Context;
import android.view.View;

import java.util.List;

public interface DetailNavigator {

	void closeView(View view);

	void nestedScrollViewHelper();

	void setUpImageViewPager();

	void defaultProduct();

	void setProductName();

	String getImageByWidth(String imageUrl, Context context);

	List<String> getAuxiliaryImage();
}

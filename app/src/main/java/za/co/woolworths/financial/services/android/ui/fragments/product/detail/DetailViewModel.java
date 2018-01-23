package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class DetailViewModel extends BaseViewModel<DetailNavigator> {

	private ProductList defaultProduct;

	private List<String> AuxiliaryImage;

	public DetailViewModel() {
		super();
	}

	public DetailViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void setAuxiliaryImage(List<String> auxiliaryImage) {
		AuxiliaryImage = auxiliaryImage;
	}

	public List<String> getAuxiliaryImage() {
		return AuxiliaryImage;
	}

	public void setDefaultProduct(String defaultProduct) {
		this.defaultProduct = new Gson().fromJson(defaultProduct, ProductList.class);
	}

	public ProductList getDefaultProduct() {
		return defaultProduct;
	}

}

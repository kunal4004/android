package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.graphics.Paint;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
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



//	public void productDetailPriceList(WTextView wPrice, WTextView WwasPrice,
//									   String price, String wasPrice, String productType) {
//		switch (productType) {
//			case "clothingProducts":
//				if (TextUtils.isEmpty(wasPrice)) {
//					wPrice.setText(WFormatter.formatAmount(price));
//					wPrice.setPaintFlags(0);
//					WwasPrice.setText("");
//				} else {
//					if (wasPrice.equalsIgnoreCase(price)) {
//						//wasPrice equals currentPrice
//						wPrice.setText(WFormatter.formatAmount(price));
//						WwasPrice.setText("");
//						wPrice.setPaintFlags(0);
//					} else {
//						wPrice.setText(WFormatter.formatAmount(wasPrice));
//						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//						WwasPrice.setText(WFormatter.formatAmount(price));
//					}
//				}
//				break;
//
//			default:
//				if (TextUtils.isEmpty(wasPrice)) {
//					if (Utils.isLocationEnabled(wPrice.getContext())) {
//						ArrayList<Double> priceList = new ArrayList<>();
//						for (OtherSku os : mObjProductDetail.otherSkus) {
//							if (!TextUtils.isEmpty(os.price)) {
//								priceList.add(Double.valueOf(os.price));
//							}
//						}
//						if (priceList.size() > 0) {
//							price = String.valueOf(Collections.max(priceList));
//						}
//					}
//					wPrice.setText(WFormatter.formatAmount(price));
//					wPrice.setPaintFlags(0);
//					WwasPrice.setText("");
//				} else {
//					if (Utils.isLocationEnabled(wPrice.getContext())) {
//						ArrayList<Double> priceList = new ArrayList<>();
//						for (OtherSku os : mObjProductDetail.otherSkus) {
//							if (!TextUtils.isEmpty(os.price)) {
//								priceList.add(Double.valueOf(os.price));
//							}
//						}
//						if (priceList.size() > 0) {
//							price = String.valueOf(Collections.max(priceList));
//						}
//					}
//
//					if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
//						wPrice.setText(WFormatter.formatAmount(price));
//						WwasPrice.setText("");
//					} else {
//						wPrice.setText(WFormatter.formatAmount(wasPrice));
//						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//						WwasPrice.setText(WFormatter.formatAmount(price));
//					}
//				}
//				break;
//		}
//	}

}

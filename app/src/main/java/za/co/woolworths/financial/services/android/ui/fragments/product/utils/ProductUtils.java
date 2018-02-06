package za.co.woolworths.financial.services.android.ui.fragments.product.utils;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class ProductUtils {

	public static void gridPriceList(WTextView wPrice, WTextView WwasPrice,
									 String fromPrice, String wasPrice) throws NumberFormatException,NullPointerException {
		if (TextUtils.isEmpty(wasPrice)) {
			if (!TextUtils.isEmpty(fromPrice)){
				wPrice.setText(WFormatter.formatAmount(fromPrice));
			}else {
				wPrice.setText("");
			}
			WwasPrice.setText("");
		} else {
			if (wasPrice.equalsIgnoreCase(fromPrice)) { //wasPrice equals currentPrice
				wPrice.setText(WFormatter.formatAmount(fromPrice));
				WwasPrice.setText("");
			} else {
				wPrice.setText(WFormatter.formatAmount(wasPrice));
				wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				WwasPrice.setText(WFormatter.formatAmount(fromPrice));
			}
		}
	}

	public static void showPromotionalImages(SimpleDraweeView imSave, SimpleDraweeView imReward,
											 SimpleDraweeView imVitality, SimpleDraweeView imNewImage,
											 PromotionImages imPromo) {
		if (imPromo != null) {
			String wSave = imPromo.save;
			String wReward = imPromo.wRewards;
			String wVitality = imPromo.vitality;
			String wNewImage = imPromo.newImage;
			DrawImage drawImage = new DrawImage(imSave.getContext());
			if (!TextUtils.isEmpty(wSave)) {
				imSave.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(imSave, wSave);
			} else {
				imSave.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wReward)) {
				imReward.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(imReward, wReward);
			} else {
				imReward.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wVitality)) {
				imVitality.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(imVitality, wVitality);
			} else {
				imVitality.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wNewImage)) {
				imNewImage.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(imNewImage, wNewImage);

			} else {
				imNewImage.setVisibility(View.GONE);
			}
		}
	}
}

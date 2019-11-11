package za.co.woolworths.financial.services.android.ui.fragments.product.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class ProductUtils extends BaseProductUtils{

	public static void displayPrice(TextView tvPrice, TextView tvWasPrice,
									String fromPrice, String wasPrice) throws NumberFormatException, NullPointerException {
		if (TextUtils.isEmpty(wasPrice)) {
			tvPrice.setText(TextUtils.isEmpty(fromPrice) ? "" : WFormatter.formatAmount(fromPrice));
			tvWasPrice.setText("");
			tvPrice.setTextColor(Color.BLACK);
		} else {
			if (wasPrice.equalsIgnoreCase(fromPrice)) {//wasPrice equals currentPrice
				tvPrice.setText(TextUtils.isEmpty(fromPrice) ? WFormatter.formatAmount(wasPrice) : WFormatter.formatAmount(fromPrice));
				tvPrice.setTextColor(Color.BLACK);
				tvWasPrice.setText("");
			} else {
				tvPrice.setText(WFormatter.formatAmount(fromPrice));
				tvWasPrice.setText(WFormatter.formatAmount(wasPrice));
				tvWasPrice.setPaintFlags(tvWasPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				tvWasPrice.setTextColor(Color.BLACK);
				Context context = WoolworthsApplication.getAppContext();
				if (context == null) return;
				tvPrice.setTextColor(ContextCompat.getColor(context, R.color.was_price_color));

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

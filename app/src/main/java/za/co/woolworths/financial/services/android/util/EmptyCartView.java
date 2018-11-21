package za.co.woolworths.financial.services.android.util;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class EmptyCartView implements View.OnClickListener {

	public interface EmptyCartInterface {
		void onEmptyCartRetry();
	}

	private EmptyCartInterface emptyCartInterface;
	private WTextView tvDescription;
	private WTextView tvTitle;
	private ImageView imImage;
	private WButton btnGoToProduct;
	private String description;
	private String title;
	private int imageDrawable;

	public EmptyCartView(View view, EmptyCartInterface emptyCartInterface) {
		this.tvTitle = view.findViewById(R.id.txtEmptyStateTitle);
		this.tvDescription = view.findViewById(R.id.txtEmptyStateDesc);
		this.imImage = view.findViewById(R.id.imgEmpyStateIcon);
		this.btnGoToProduct = view.findViewById(R.id.btnGoToProduct);
		this.btnGoToProduct.setOnClickListener(this);
		this.emptyCartInterface = emptyCartInterface;
	}

	public void setTitle(String title) {
		this.title = title;
		tvTitle.setText(TextUtils.isEmpty(this.title) ? "" : this.title);
	}

	public void setDescription(String description) {
		this.description = description;
		tvDescription.setText(TextUtils.isEmpty(this.description) ? "" : this.description);
	}

	public void setImageUrl(int drawable) {
		this.imageDrawable = drawable;
		this.imImage.setImageResource(imageDrawable);
	}

	public void buttonVisibility(String text) {
		btnGoToProduct.setText(text);
		btnGoToProduct.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		MultiClickPreventer.preventMultiClick(view);
		switch (view.getId()) {
			case R.id.btnGoToProduct:
				emptyCartInterface.onEmptyCartRetry();
				break;

			default:
				break;
		}
	}

	public void setView(String title, String description, int drawable) {
		setTitle(title);
		setDescription(description);
		setImageUrl(drawable);
	}

	public void setView(String title, String description, String buttonText, int drawable) {
		setTitle(title);
		setDescription(description);
		buttonVisibility(buttonText);
		setImageUrl(drawable);
	}
}

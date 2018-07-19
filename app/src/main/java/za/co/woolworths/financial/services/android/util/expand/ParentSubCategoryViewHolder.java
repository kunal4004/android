package za.co.woolworths.financial.services.android.util.expand;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ParentSubCategoryViewHolder extends ParentViewHolder {

	private static final float INITIAL_POSITION = 0.0f;
	private static final float ROTATED_POSITION = 180f;
	private final ImageView mArrowExpandImageView;
	private final ProgressBar pbLoadChildItem;
	private WTextView tvSubCategoryName;
	private LinearLayout llLoadChild;

	public ParentSubCategoryViewHolder(View itemView) {
		super(itemView);
		tvSubCategoryName = itemView.findViewById(R.id.carbon_groupText);
		llLoadChild = itemView.findViewById(R.id.llLoadChild);
		mArrowExpandImageView = itemView.findViewById(R.id.carbon_groupExpandedIndicator);
		pbLoadChildItem = itemView.findViewById(R.id.pbLoadChildItem);
	}

	public void bind(SubCategoryModel subCategoryModel) {
		tvSubCategoryName.setText(subCategoryModel.getName());
		arrowVisibility(subCategoryModel);
		retrieveChildVisibility(subCategoryModel);
	}

	private void arrowVisibility(SubCategoryModel subCategoryModel) {
		llLoadChild.setVisibility(subCategoryModel.getSubCategory().hasChildren ? View.VISIBLE : View.INVISIBLE);

	}

	public void retrieveChildVisibility(SubCategoryModel subCategoryModel) {
		boolean progressBarIsVisible = subCategoryModel.getSubCategory().singleProductItemIsLoading;
		pbLoadChildItem.setVisibility(progressBarIsVisible ? View.VISIBLE : View.GONE);
		mArrowExpandImageView.setVisibility(progressBarIsVisible ? View.GONE : View.VISIBLE);
	}

	public void retrieveChildVisibility(boolean visible) {
		pbLoadChildItem.setVisibility(visible ? View.VISIBLE : View.GONE);
		mArrowExpandImageView.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	@Override
	public void setExpanded(boolean expanded) {
		super.setExpanded(expanded);
		if (expanded) {
			mArrowExpandImageView.setRotation(ROTATED_POSITION);
		} else {
			mArrowExpandImageView.setRotation(INITIAL_POSITION);
		}
	}

	@Override
	public void onExpansionToggled(boolean expanded) {
		super.onExpansionToggled(expanded);

		RotateAnimation rotateAnimation;
		if (expanded) { // rotate clockwise
			rotateAnimation = new RotateAnimation(ROTATED_POSITION,
					INITIAL_POSITION,
					RotateAnimation.RELATIVE_TO_SELF, 0.5f,
					RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		} else { // rotate counterclockwise
			rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
					INITIAL_POSITION,
					RotateAnimation.RELATIVE_TO_SELF, 0.5f,
					RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		}

		rotateAnimation.setDuration(200);
		rotateAnimation.setFillAfter(true);
		mArrowExpandImageView.startAnimation(rotateAnimation);

	}
}

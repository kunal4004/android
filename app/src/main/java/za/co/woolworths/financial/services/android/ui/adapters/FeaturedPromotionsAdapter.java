package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Promotion;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.DrawImage;

public class FeaturedPromotionsAdapter extends PagerAdapter {
	public Activity mContext;
	private List<Promotion> promotions;
	private BottomNavigator mBottomNavigator;

	public FeaturedPromotionsAdapter(Activity mContext, List<Promotion> promotions) {
		this.mContext = mContext;
		this.promotions = promotions;
		this.mBottomNavigator = (BottomNavigator) mContext;
	}

	@Override
	public int getCount() {
		return promotions.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {

		return view.equals(object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View cView = mContext.getLayoutInflater().inflate(R.layout.featured_prmotion_list_item, container, false);
		final WrapContentDraweeView image = cView.findViewById(R.id.promotionImage);
		DrawImage drawImage = new DrawImage(container.getContext());
		drawImage.displayImage(image, promotions.get(position).image);
		container.addView(cView);
		cView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GridFragment gridFragment = new GridFragment();
				Bundle bundle = new Bundle();
				bundle.putString("sub_category_name", image.getContext().getResources().getString(R.string.featured_promotions));
				bundle.putString("sub_category_id", promotions.get(position).path);
				gridFragment.setArguments(bundle);
				mBottomNavigator.setSelectedIconPosition(BottomNavigationActivity.INDEX_ACCOUNT);
				mBottomNavigator.pushFragment(gridFragment);
			}
		});
		return cView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		super.restoreState(state, loader);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public float getPageWidth(int position) {
		return 0.96f;
	}
}

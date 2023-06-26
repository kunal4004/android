package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.controller.SelectedItemCallback;

/**
 * Created by W7099877 on 2017/09/27.
 */

public class CLIIncreaseLimitInfoPagerAdapter extends PagerAdapter {

	private SelectedItemCallback selectedItemCallback;
	public Activity mContext;
	private String[] titles = null;
	private String[] descriptions = null;
	private TypedArray images;

	public CLIIncreaseLimitInfoPagerAdapter(Activity context, SelectedItemCallback selectedItemCallback) {
		this.mContext = context;
		this.titles = mContext.getResources().getStringArray(R.array.cli_carousel_title);
		this.images = mContext.getResources().obtainTypedArray(R.array.cli_carousel_image);
		this.descriptions = mContext.getResources().getStringArray(R.array.cli_carousel_desc);
		this.selectedItemCallback = selectedItemCallback;
	}

	@Override
	public int getCount() {
		return titles.length;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View cView = mContext.getLayoutInflater().inflate(R.layout.cli_increase_limit_pager_item, container, false);
		final WTextView title = (WTextView) cView.findViewById(R.id.cli_info_title);
		TextView description = (TextView) cView.findViewById(R.id.cli_info_desc);
		WTextView videoText = (WTextView) cView.findViewById(R.id.cli_ifo_video);
		ImageView image=(ImageView)cView.findViewById(R.id.cli_info_image);
		image.setImageResource(images.getResourceId(position,-1));
		image.setContentDescription("pagerImage"+position);
		title.setText(titles[position]);
		title.setContentDescription("pagerTitle"+position);
		description.setText(descriptions[position]);
		description.setContentDescription("pagerDescription"+position);
		container.addView(cView);

		videoText.setOnClickListener(v -> selectedItemCallback.onItemClick(v, position));
		videoText.setContentDescription("pagerVideoText"+position);
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
}
package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
	private TypedArray images = null;

	public CLIIncreaseLimitInfoPagerAdapter(Activity context, SelectedItemCallback selectedItemCallback) {
		this.mContext = context;
		titles = mContext.getResources().getStringArray(R.array.on_boarding_walkthrough_titles);
		images = mContext.getResources().obtainTypedArray(R.array.on_boarding_walkthrough_images);
		descriptions = mContext.getResources().getStringArray(R.array.on_boarding_walkthrough_descriptions);
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
		View cView = mContext.getLayoutInflater().inflate(R.layout.cli_increse_limit_info_pager_item, container, false);
		ImageView boardingImage = (ImageView) cView.findViewById(R.id.cli_info_image);
		final WTextView title = (WTextView) cView.findViewById(R.id.cli_info_title);
		WTextView description = (WTextView) cView.findViewById(R.id.cli_info_desc);
		WTextView videoText = (WTextView) cView.findViewById(R.id.cli_ifo_video);
		//boardingImage.setImageResource(images.getResourceId(position,-1));
		title.setText(titles[position]);
		description.setText(descriptions[position]);
		container.addView(cView);

		videoText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedItemCallback.onItemClick(v, position);
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
}
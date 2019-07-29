package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2017/05/26.
 */

public class OnBoardingViewPagerAdapter extends PagerAdapter {

    public Activity mContext;
    private  String[] titles = null;
    private  String[] descriptions = null;
    private TypedArray images = null;
    public  OnBoardingViewPagerAdapter(Activity context)
    {
        this.mContext=context;
        titles=mContext.getResources().getStringArray(R.array.on_boarding_walkthrough_titles);
        images = mContext.getResources().obtainTypedArray(R.array.on_boarding_walkthrough_images);
        descriptions=mContext.getResources().getStringArray(R.array.on_boarding_walkthrough_descriptions);
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
    public Object instantiateItem(ViewGroup container, int position) {
        View cView=mContext.getLayoutInflater().inflate(R.layout.on_boarding_item,container,false);
        ImageView boardingImage=(ImageView)cView.findViewById(R.id.onBoadingImage);
        WTextView title=(WTextView)cView.findViewById(R.id.onBoardingTitle);
        WTextView description=(WTextView)cView.findViewById(R.id.onBoardingDescription);
        boardingImage.setImageResource(images.getResourceId(position,-1));
        title.setText(titles[position]);
        description.setText(descriptions[position]);
        container.addView(cView);
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
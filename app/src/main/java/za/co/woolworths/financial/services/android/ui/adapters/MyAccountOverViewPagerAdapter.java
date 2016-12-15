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

import static com.awfs.coordination.R.id.boardingImage;
import static com.awfs.coordination.R.id.myaccountsCard;

/**
 * Created by W7099877 on 09/12/2016.
 */

public class MyAccountOverViewPagerAdapter extends PagerAdapter {

    public   Activity mContext;
    private  String[] titles = null;
    private  String[] descriptions = null;
    private  TypedArray images = null;
   public  MyAccountOverViewPagerAdapter(Activity context)
    {
        this.mContext=context;
        titles=mContext.getResources().getStringArray(R.array.accounts_walkthrough_titles);
        images = mContext.getResources().obtainTypedArray(R.array.accounts_walkthrough_images);
        descriptions=mContext.getResources().getStringArray(R.array.accounts_walkthrough_descriptions);
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
        View cView=mContext.getLayoutInflater().inflate(R.layout.my_account_fragment_header_logged_out_item,container,false);
        ImageView boardingImage=(ImageView)cView.findViewById(R.id.boardingImage);
        WTextView title=(WTextView)cView.findViewById(R.id.accountsWalkthroughTitle);
        WTextView description=(WTextView)cView.findViewById(R.id.accountsWalkthroughDescription);
        boardingImage.setBackgroundResource(images.getResourceId(position,-1));
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

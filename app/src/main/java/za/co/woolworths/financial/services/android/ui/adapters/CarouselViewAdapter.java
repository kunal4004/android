package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.ui.views.CarouselView;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 26/10/2016.
 */

public class CarouselViewAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener{


    private CarouselView cur = null;
    private CarouselView next = null;
    private Activity context;
    private float scale;
    int pCount = 0;
    ArrayList<CarouselView> layoutList=new ArrayList<>();
    int coverUrl[];


    public CarouselViewAdapter(Activity context,int coverUrl[])
    {
        this.context = context;
        this.coverUrl=coverUrl;

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

      /*  View cView=context.getLayoutInflater().inflate(R.layout.mf,container,false);

        try {
            if (position == Utils.FIRST_PAGE)
                scale = Utils.BIG_SCALE;
            else
                scale = Utils.SMALL_SCALE;


        } catch (Exception e) {
            // TODO: handle exception
        }
        CarouselView root = (CarouselView) cView.findViewById(R.id.root);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400,400);

        ImageView iv = (ImageView) cView.findViewById(R.id.pagerImg);
        iv.setImageResource(coverUrl[position] );
        iv.setLayoutParams(layoutParams);

        iv.setPadding(15, 15, 15, 15);
        root.setScaleBoth(scale);
        layoutList.add(root);
        container.addView(cView);*/

        return false;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view.equals(object);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        try {
            if (positionOffset >= 0f && positionOffset <= 1f) {
                cur = getRootView( position );
                next = getRootView( position +1 );

                cur.setScaleBoth(Utils.BIG_SCALE   - Utils.DIFF_SCALE * positionOffset);
                next.setScaleBoth(Utils.SMALL_SCALE  + Utils.DIFF_SCALE * positionOffset);

                pCount++;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
    private CarouselView getRootView(int position) {

        return layoutList.get(position);
    }
}

package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Promotion;
import za.co.woolworths.financial.services.android.ui.activities.ProductViewGridActivity;
import za.co.woolworths.financial.services.android.util.DrawImage;

public class FeaturedPromotionsAdapter extends PagerAdapter {
    public Activity mContext;
    private List<Promotion> promotions;
    public FeaturedPromotionsAdapter(Activity mContext, List<Promotion> promotions)
    {
        this.mContext=mContext;
        this.promotions=promotions;
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
        View cView=mContext.getLayoutInflater().inflate(R.layout.featured_prmotion_list_item,container,false);
        ImageView image=(ImageView)cView.findViewById(R.id.promotionImage);
        DrawImage drawImage = new DrawImage(container.getContext());
        drawImage.displayImage(image,promotions.get(position).image);
        container.addView(cView);
        cView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openProductName = new Intent(mContext, ProductViewGridActivity.class);
                openProductName.putExtra("searchProduct", "");
                openProductName.putExtra("title", promotions.get(position).path);
                openProductName.putExtra("titleNav",mContext.getResources().getString(R.string.featured_promotions) );
                mContext.startActivity(openProductName);
                mContext.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

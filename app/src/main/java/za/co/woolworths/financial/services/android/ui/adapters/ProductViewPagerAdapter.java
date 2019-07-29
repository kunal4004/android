package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.net.Uri;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

public class ProductViewPagerAdapter extends PagerAdapter {

	public interface MultipleImageInterface {
		void SelectedImage(String otherSkus);
	}

	private MultipleImageInterface multipleImageInterface;

	private final Context mContext;
	private List<String> mExternalImageRefList;

	public ProductViewPagerAdapter(Context mContext, List<String> externalImageRefList,
								   MultipleImageInterface multipleImageInterface) {
		this.mContext = mContext;
		this.multipleImageInterface = multipleImageInterface;
		this.mExternalImageRefList = externalImageRefList;
	}

	@Override
	public Object instantiateItem(ViewGroup collection, final int position) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.product_view,
				collection, false);
		String image = mExternalImageRefList.get(position);
		WrapContentDraweeView mProductImage = v.findViewById(R.id.imProductView);
		mProductImage.setResizeImage(true);
		mProductImage.setImageURI(Uri.parse(image), mProductImage.getContext());
		collection.addView(v, 0);

		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				multipleImageInterface.SelectedImage(mExternalImageRefList.get(position));
			}
		});
		return v;
	}

	@Override
	public void destroyItem(ViewGroup collection, int position, Object view) {
		collection.removeView((View) view);
	}

	@Override
	public int getCount() {
		return mExternalImageRefList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void updatePagerItems(List<String> mAuxiliaryImage){
		this.mExternalImageRefList.clear();
		this.notifyDataSetChanged();
		this.mExternalImageRefList = mAuxiliaryImage;
		this.notifyDataSetChanged();
	}
}

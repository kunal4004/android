package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.fragments.ColorFragmentDialog;
import za.co.woolworths.financial.services.android.ui.fragments.SizeFragmentDialog;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.NonSwipeableViewPager;
import za.co.woolworths.financial.services.android.util.Utils;

public class ConfirmColorSizeActivity extends AppCompatActivity implements View.OnClickListener, WStockFinderActivity.RecyclerItemSelected {

	private LinearLayout mRelRootContainer, mRelPopContainer;
	private ImageView mImCloseIcon, mImBackIcon;
	private String mColorList, mOtherSKU, mProductName;
	private static final int ANIM_DOWN_DURATION = 700;
	private NonSwipeableViewPager mPager;
	private WTextView tvTitle;
	private String mSelectedColour;
	private ArrayList<OtherSku> mOtherSizeSKU;
	private boolean mProductHasColor, mProductHasSize, viewWasClicked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, android.R.color.transparent);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		setContentView(R.layout.activity_confirm_color_size);

		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mColorList = mBundle.getString("COLOR_LIST");
			mOtherSKU = mBundle.getString("OTHERSKU");
			mProductName = mBundle.getString("PRODUCT_NAME");
			mProductHasColor = mBundle.getBoolean("PRODUCT_HAS_COLOR");
			mProductHasSize = mBundle.getBoolean("PRODUCT_HAS_SIZE");
		}

		init();
		addListener();
		hideBackIcon();
		setAnimation();

		if (mProductHasColor) {
			setPagerItem(0);
		} else {
			setPagerItem(1);
			mImBackIcon.setVisibility(View.GONE);
		}
	}

	private void addListener() {
		mRelPopContainer.setOnClickListener(this);
		mImCloseIcon.setOnClickListener(this);
		mImBackIcon.setOnClickListener(this);
	}

	private void init() {
		mRelRootContainer = (LinearLayout) findViewById(R.id.relContainerRootMessage);
		mRelPopContainer = (LinearLayout) findViewById(R.id.relPopContainer);
		tvTitle = (WTextView) findViewById(R.id.title);
		mPager = (NonSwipeableViewPager) findViewById(R.id.viewPager);
		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				switch (position) {
					case 0:
						hideBackIcon();
						tvTitle.setText(getString(R.string.confirm_color_desc));
						break;
					case 1:
						showBackIcon();
						tvTitle.setText(getString(R.string.confirm_size_desc));
						mOtherSizeSKU = commonSizeList();
						if (mProductHasColor) {
							SizeFragmentDialog sizeFragmentDialog = (SizeFragmentDialog) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
							sizeFragmentDialog.resetIndex();
							sizeFragmentDialog.updateSizeAdapter(mOtherSizeSKU, "size");
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mImCloseIcon = (ImageView) findViewById(R.id.imCloseIcon);
		mImBackIcon = (ImageView) findViewById(R.id.imBackIcon);

	}

	private void setAnimation() {
		Animation mPopEnterAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_enter);
		mRelRootContainer.startAnimation(mPopEnterAnimation);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.relPopContainer:
			case R.id.imCloseIcon:
				closeViewAnimation();
				break;

			case R.id.imBackIcon:
				hideBackIcon();
				mPager.setCurrentItem(0);
				break;

			default:
				break;
		}
	}

	private void closeViewAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void dismissSizeColorActivity(final String sku) {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					openStockFinder(sku);
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void dismissLayout() {
		finish();
		overridePendingTransition(0, 0);
	}

	private void showBackIcon() {
		mImBackIcon.setVisibility(View.VISIBLE);
	}

	private void hideBackIcon() {
		mImBackIcon.setVisibility(View.GONE);
	}

	@Override
	public void onRecyclerItemClick(View v, int position, String filterType) {
		if (filterType.equalsIgnoreCase("color")) {
			if (mProductHasSize) {
				mSelectedColour = getOtherSKUList(mColorList).get(position).colour;
				mPager.setCurrentItem(1);
			} else {
				String selectedSKU = getOtherSKUList(mColorList).get(position).sku;
				dismissSizeColorActivity(selectedSKU);
			}
		} else {
			String selectedSKU = mOtherSizeSKU.get(position).sku;
			dismissSizeColorActivity(selectedSKU);
		}
	}

	private void openStockFinder(String sku) {
		Intent mIntent = new Intent(this, WStockFinderActivity.class);
		mIntent.putExtra("PRODUCT_NAME", mProductName);
		mIntent.putExtra("SELECTED_SKU", sku);
		startActivity(mIntent);
		overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		MyPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);

		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return ColorFragmentDialog.newInstance(mColorList, "color");
				case 1:
					return SizeFragmentDialog.newInstance(mOtherSKU, "size");
				default:
					break;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	private boolean sizeValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<OtherSku> getOtherSKUList(String item) {
		return new Gson().fromJson(item,
				new TypeToken<ArrayList<OtherSku>>() {
				}.getType());
	}

	private ArrayList<OtherSku> commonSizeList() {
		ArrayList<OtherSku> otherSkus = getOtherSKUList(mOtherSKU);
		ArrayList<OtherSku> commonSizeList = new ArrayList<>();

		if (mProductHasColor) { //product has color
			// filter by colour
			ArrayList<OtherSku> sizeList = new ArrayList<>();
			for (OtherSku sku : otherSkus) {
				if (sku.colour.equalsIgnoreCase(mSelectedColour)) {
					sizeList.add(sku);
				}
			}

			//remove duplicates
			for (OtherSku os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		} else { // no color found
			//remove duplicates
			for (OtherSku os : otherSkus) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		}
		return commonSizeList;
	}

	private void setPagerItem(int position) {
		mPager.setCurrentItem(position);
	}
}

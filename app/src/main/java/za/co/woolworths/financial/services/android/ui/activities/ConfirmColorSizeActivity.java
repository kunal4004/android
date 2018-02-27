package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderFragmentAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ColorFragmentList;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.SizeFragmentList;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.EditQuantityFragmentList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ColorInterface;
import za.co.woolworths.financial.services.android.util.NonSwipeableViewPager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.POST_ADD_ITEM_TO_CART;

public class ConfirmColorSizeActivity extends AppCompatActivity implements View.OnClickListener, WStockFinderActivity.RecyclerItemSelected {

	public static final String SELECT_PAGE = "SELECT_PAGE";
	public static final String QUANTITY = "quantity";
	public final String SELECTED_COLOUR = "SELECTED_COLOUR";
	public final String COLOR_LIST = "COLOR_LIST";
	public final String OTHERSKU = "OTHERSKU";
	public final String PRODUCT_HAS_COLOR = "PRODUCT_HAS_COLOR";
	public final String PRODUCT_HAS_SIZE = "PRODUCT_HAS_SIZE";

	private LinearLayout mRelRootContainer, mRelPopContainer;
	private ImageView mImCloseIcon, mImBackIcon;
	private String mColorList, mOtherSKU;
	private static final int ANIM_DOWN_DURATION = 700;
	private NonSwipeableViewPager mViewPager;
	private WTextView tvTitle;
	private String mSelectedColour;
	private boolean mProductHasColor, mProductHasSize, viewWasClicked;
	private StockFinderFragmentAdapter mPagerAdapter;
	private WGlobalState mGlobalState;
	private String mSelectPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, android.R.color.transparent);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		setContentView(R.layout.activity_confirm_color_size);
		mGlobalState = ((WoolworthsApplication) ConfirmColorSizeActivity.this.getApplication()).getWGlobalState();
		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mSelectPage = mBundle.getString(SELECT_PAGE);
			mSelectedColour = mBundle.getString(SELECTED_COLOUR);
			mColorList = mBundle.getString(COLOR_LIST);
			mOtherSKU = mBundle.getString(OTHERSKU);
			mProductHasColor = mBundle.getBoolean(PRODUCT_HAS_COLOR);
			mProductHasSize = mBundle.getBoolean(PRODUCT_HAS_SIZE);
		}
		init();
		addListener();
		hideBackIcon();
		setAnimation();
		switch (mSelectPage) {
			case QUANTITY:
				selectCurrentPage(2);
				break;
			default:
				if (mProductHasColor) {
					selectCurrentPage(0);
				} else {
					selectCurrentPage(1);
					hideBackIcon();
				}
				break;
		}
	}

	private void addListener() {
		mRelPopContainer.setOnClickListener(this);
		mImCloseIcon.setOnClickListener(this);
		mImBackIcon.setOnClickListener(this);
	}

	private void init() {
		mRelRootContainer = findViewById(R.id.relContainerRootMessage);
		mRelPopContainer = findViewById(R.id.relPopContainer);
		tvTitle = findViewById(R.id.title);
		mViewPager = findViewById(R.id.viewPager);
		mPagerAdapter = new StockFinderFragmentAdapter(getSupportFragmentManager());
		mPagerAdapter.addFrag(new ColorFragmentList(), getString(R.string.color));
		mPagerAdapter.addFrag(new SizeFragmentList(), getString(R.string.size));
		mPagerAdapter.addFrag(new EditQuantityFragmentList(), getString(R.string.edit_quantity));
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.addOnPageChangeListener(pageChangeListener);
		mImCloseIcon = findViewById(R.id.imCloseIcon);
		mImBackIcon = findViewById(R.id.imBackIcon);
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
				mViewPager.setCurrentItem(0);
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

	private void dismissQuantityView(final int quantity) {
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
					if (mGlobalState != null) {
						switch (mGlobalState.getNavigateFromQuantity()) {
							case 1: //cart
								Utils.sendBus(new CartState(CHANGE_QUANTITY, quantity));
								mGlobalState.navigateFromQuantity(0);
								break;

							default:
								Utils.sendBus(new ProductState(POST_ADD_ITEM_TO_CART, quantity));
								break;

						}
					}
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void dismissSizeColorActivity() {
		if (mGlobalState.getSaveButtonClick() == DetailFragment.INDEX_STORE_FINDER) {
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
						callInStoreFinder();
						dismissLayout();
					}
				});
				mRelRootContainer.startAnimation(animation);
			}
		} else {
			dismissQuantityView(1);
		}
	}

	private void dismissLayout() {
		finish();
		overridePendingTransition(0, 0);
	}

	private void showBackIcon() {
		if (mProductHasColor)
			mImBackIcon.setVisibility(View.VISIBLE);
	}

	private void hideBackIcon() {
		mImBackIcon.setVisibility(View.GONE);
	}

	@Override
	public void onRecyclerItemClick(View v, int position, String filterType) {
		if (filterType.equalsIgnoreCase(getString(R.string.color))) {
			if (mProductHasSize) {
				mSelectedColour = getOtherSKUList(mColorList).get(position).colour;
				ArrayList<OtherSkus> otherSkuList = Utils.commonSizeList(mSelectedColour, mProductHasColor, getOtherSKUList(mOtherSKU));
				if (otherSkuList.size() > 0) {
					if (otherSkuList.size() == 1) {
						String selectedSKU = otherSkuList.get(0).sku;
						mGlobalState.setSelectedSKUId(selectedSKU);
						dismissSizeColorActivity();
					} else {
						String selectedSKU = getOtherSKUList(mColorList).get(position).sku;
						mGlobalState.setSelectedSKUId(selectedSKU);
						mViewPager.setCurrentItem(1);
					}
				} else {
					String selectedSKU = getOtherSKUList(mColorList).get(position).sku;
					mGlobalState.setSelectedSKUId(selectedSKU);
					dismissSizeColorActivity();
				}
			} else {
				String selectedSKU = getOtherSKUList(mColorList).get(position).sku;
				mGlobalState.setSelectedSKUId(selectedSKU);
				dismissSizeColorActivity();
			}
		} else {
			ArrayList<OtherSkus> mOtherSizeSKU = Utils.commonSizeList(mSelectedColour, mProductHasColor, getOtherSKUList(mOtherSKU));
			String selectedSKU = mOtherSizeSKU.get(position).sku;
			mGlobalState.setSelectedSKUId(selectedSKU);
			inStoreFinderUpdate();
		}
	}

	@Override
	public void onQuantitySelected(int quantity) {
		dismissQuantityView(quantity);
	}

	private void inStoreFinderUpdate() {
		if (mGlobalState.getSaveButtonClick() == DetailFragment.INDEX_STORE_FINDER) {
			callInStoreFinder();
			closeViewAnimation();
		} else {
			dismissQuantityView(1);
		}
	}

	private void callInStoreFinder() {
		WoolworthsApplication.getInstance().bus().send(new ConfirmColorSizeActivity());
	}


	private ArrayList<OtherSkus> getOtherSKUList(String item) {
		return new Gson().fromJson(item,
				new TypeToken<ArrayList<OtherSkus>>() {
				}.getType());
	}

	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int newPosition) {
			selectPage(newPosition);
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	private void selectPage(int position) {
		ColorInterface fragmentToShow = (ColorInterface) mPagerAdapter.getItem(position);
		switch (position) {
			case 0:
				ArrayList<OtherSkus> mOtherSKUList = getOtherSKUList(mColorList);
				hideBackIcon();
				tvTitle.setText(getString(R.string.confirm_color_desc));
				if (fragmentToShow != null) {
					fragmentToShow.onUpdate(mOtherSKUList, getString(R.string.color));
				}
				break;
			case 1:
				showBackIcon();
				tvTitle.setText(getString(R.string.confirm_size_desc));
				ArrayList<OtherSkus> mOtherSizeSKU = Utils.commonSizeList(mSelectedColour,
						mProductHasColor, getOtherSKUList(mOtherSKU));
				if (fragmentToShow != null) {
					fragmentToShow.onUpdate(mOtherSizeSKU, getString(R.string.size));
				}
				break;
			case 2:
				tvTitle.setText(getString(R.string.edit_quantity));
				if (fragmentToShow != null) {
					ArrayList list = new ArrayList();
					for (int number = 0; number < 10; number++) {
						list.add(number + 1);
					}
					fragmentToShow.onUpdate(list);
				}
				break;
		}
	}

	private void selectCurrentPage(final int position) {
		mViewPager.post(new Runnable() {
			@Override
			public void run() {
				mViewPager.setCurrentItem(position);
			}
		});
	}
}

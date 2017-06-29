package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.adapters.PSSubCategoryAdapter;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.SubCategoryBinder;

public class ProductSubCategoryActivity extends AppCompatActivity implements View.OnClickListener,
		SubCategoryBinder.OnClickListener {

	private RecyclerView recyclerView;
	private List<SubCategory> mSubCategories;
	private LinearLayoutManager mLayoutManager;
	private PSSubCategoryAdapter mPSRootCategoryAdapter;
	private ProductSubCategoryActivity mContext;
	private WTextView mTextNoProductFound;
	private int mCatStep;
	private String mRootCategoryName;
	private String mRootCategoryId;
	private String mSubCategoriesName;
	private PopWindowValidationMessage mPopWindowValidationMessage;
	private ProgressBar mProgressBar;
	private ErrorHandlerView mErrorHandlerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.product_search_sub_category);
		mContext = this;
		Bundle bundleSubCategory = getIntent().getExtras();
		if (bundleSubCategory != null) {
			mRootCategoryId = bundleSubCategory.getString("root_category_id");
			mRootCategoryName = bundleSubCategory.getString("root_category_name");
			mCatStep = bundleSubCategory.getInt("catStep");
			mSubCategoriesName = bundleSubCategory.getString("sub_category_name");
		}
		mPopWindowValidationMessage = new PopWindowValidationMessage(this);
		initUI();
		getSubCategory();
	}

	private void initUI() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		WTextView mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
		recyclerView = (RecyclerView) findViewById(R.id.productSearchList);
		mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
		ImageView mImBurgerButtonPressed = (ImageView) findViewById(R.id.imBurgerButtonPressed);
		RelativeLayout searchStore = (RelativeLayout) findViewById(R.id.search_store_activity);
		mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
		ImageView mImSearch = (ImageView) findViewById(R.id.imSearch);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		mImSearch.setOnClickListener(this);
		mImBurgerButtonPressed.setOnClickListener(this);
		if (mCatStep == 0)
			mToolBarTitle.setText(mRootCategoryName);
		else
			mToolBarTitle.setText(mSubCategoriesName);

		mErrorHandlerView = new ErrorHandlerView(this
				, (RelativeLayout) findViewById(R.id.no_connection_layout));

		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(ProductSubCategoryActivity.this))
					getSubCategory();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imBurgerButtonPressed:
				onBackPressed();
				overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
				break;
			case R.id.imSearch:
				Intent openSearchActivity = new Intent(this, ProductSearchActivity.class);
				startActivity(openSearchActivity);
				overridePendingTransition(0, 0);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ps_search_icon, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				Intent openSearchBarActivity = new Intent(ProductSubCategoryActivity.this, ProductSearchActivity.class);
				startActivity(openSearchBarActivity);
				overridePendingTransition(0, 0);
				break;
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private HttpAsyncTask<String, String, SubCategories> subCategoryAPI(final String category_id) {
		return new HttpAsyncTask<String, String, SubCategories>() {
			@Override
			protected SubCategories httpDoInBackground(String... params) {
				return ((WoolworthsApplication) ProductSubCategoryActivity.this.getApplication())
						.getApi().getSubCategory(category_id);
			}

			@Override
			protected SubCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new SubCategories();
			}

			@Override
			protected Class<SubCategories> httpDoInBackgroundReturnType() {
				return SubCategories.class;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
				showProgressBar();
			}

			@Override
			protected void onPostExecute(SubCategories subCategories) {
				super.onPostExecute(subCategories);
				try {
					switch (subCategories.httpCode) {
						case 200:
							if (subCategories.subCategories != null && subCategories.subCategories.size() != 0) {
								mSubCategories = subCategories.subCategories;
								mPSRootCategoryAdapter = new PSSubCategoryAdapter(subCategories.subCategories, mContext);
								mLayoutManager = new LinearLayoutManager(ProductSubCategoryActivity.this);
								mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
								recyclerView.setLayoutManager(mLayoutManager);
								recyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
								recyclerView.setNestedScrollingEnabled(false);
								recyclerView.setAdapter(mPSRootCategoryAdapter);
								mPSRootCategoryAdapter.setCLIContent();
								hideNoProductFound();
							} else {
								showNoProductFound();
							}

							hideProgressBar();
							break;

						default:
							mPopWindowValidationMessage.displayValidationMessage(subCategories.response.desc,
									PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
							break;
					}
				} catch (NullPointerException ignored) {
				}
				mProgressBar.setVisibility(View.GONE);
			}
		};
	}

	private void showNoProductFound() {
		mTextNoProductFound.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);
	}

	private void hideNoProductFound() {
		mTextNoProductFound.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v, final int position) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SubCategory subCategory = mSubCategories.get(position);

				if (subCategory.hasChildren) {
					Intent openProductCategory = new Intent(ProductSubCategoryActivity.this, ProductSubCategoryActivity.class);
					openProductCategory.putExtra("root_category_id", subCategory.categoryId);
					openProductCategory.putExtra("sub_category_name", subCategory.categoryName);
					openProductCategory.putExtra("catStep", 1);
					startActivity(openProductCategory);
					overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
				} else {
					Intent openProductListIntent = new Intent(ProductSubCategoryActivity.this, ProductGridActivity.class);
					openProductListIntent.putExtra("sub_category_name", subCategory.categoryName);
					openProductListIntent.putExtra("sub_category_id", subCategory.categoryId);
					startActivity(openProductListIntent);
					overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
				}
			}
		}, 200);
	}

	public void getSubCategory() {
		subCategoryAPI(mRootCategoryId).execute();
	}

	private void hideProgressBar() {
		mProgressBar.setVisibility(View.GONE);
		mProgressBar.getIndeterminateDrawable().setColorFilter(null);
	}

	private void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPSRootCategoryAdapter != null)
			mPSRootCategoryAdapter.resetSelectedIndex();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}
}
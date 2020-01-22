package za.co.woolworths.financial.services.android.ui.activities.product;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.fragments.shop.ChanelMessageDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.product.shop.ShoppingListSearchResultActivity.SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE;

public class ProductSearchActivity extends AppCompatActivity
		implements View.OnClickListener, ChanelMessageDialogFragment.IChanelMessageDialogDismissListener {
	public LinearLayoutManager mLayoutManager;
	public Toolbar toolbar;
	private WEditTextView mEditSearchProduct;
	private LinearLayout recentSearchLayout;
	private LinearLayout recentSearchList;
	private String mSearchTextHint = "";
	private String mListID;
	public static int PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE = 1244;
	public final String SEARCH_VALUE_CHANEL = "chanel";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_search_activity);
		Utils.updateStatusBarBackground(this);
		setActionBar();
		initUI();
		showRecentSearchHistoryView(true);
		mEditSearchProduct.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					searchProduct(mEditSearchProduct.getText().toString());
					return true;
				}
				return false;
			}
		});

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (!TextUtils.isEmpty(bundle.getString("SEARCH_TEXT_HINT"))) {
				mListID = bundle.getString("listID");
				mSearchTextHint = getString(R.string.shopping_search_hint);
				mEditSearchProduct.setHint(mSearchTextHint);
			}
		}
	}

	private void initUI() {
		mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
		mEditSearchProduct = findViewById(R.id.toolbarText);
		recentSearchLayout = findViewById(R.id.recentSearchLayout);
		recentSearchList = findViewById(R.id.recentSearchList);
	}

	private void setActionBar() {
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(null);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_search);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


	@Override
	public void onBackPressed() {
		canGoBack();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				searchProduct(mEditSearchProduct.getText().toString());
				return true;
			case R.id.action_search:
				canGoBack();
				break;
		}
		return false;
	}

	private void searchProduct(String searchProductBrand) {
		if (searchProductBrand.length() > 2) {
			if (searchProductBrand.toLowerCase().contains(SEARCH_VALUE_CHANEL)) {
				ChanelMessageDialogFragment.Companion.newInstance().show(getSupportFragmentManager(), ChanelMessageDialogFragment.class.getSimpleName());
			} else {
				SearchHistory search = new SearchHistory();
				search.searchedValue = searchProductBrand;
				saveRecentSearch(search);
				if (TextUtils.isEmpty(mSearchTextHint)) {
					LoadState loadState = new LoadState();
					loadState.setSearchProduct(searchProductBrand);
					Utils.sendBus(loadState);
					mEditSearchProduct.setText("");
					finish();
					overridePendingTransition(0, 0);
				} else {
					ScreenManager.presentShoppingListSearchResult(this, search.searchedValue, mListID);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_item, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void saveRecentSearch(SearchHistory searchHistory) {
		List<SearchHistory> histories;
		histories = getRecentSearch();
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_PRODUCT_SEARCH);
		sessionDao.key = SessionDao.KEY.STORES_PRODUCT_SEARCH;
		Gson gson = new Gson();
		boolean isExist = false;
		if (histories == null) {
			histories = new ArrayList<>();
			histories.add(0, searchHistory);
			sessionDao.value = gson.toJson(histories);
			try {
				sessionDao.save();
			} catch (Exception e) {
				Log.e("TAG", e.getMessage());
			}
		} else {
			for (SearchHistory s : histories) {
				if (s.searchedValue.equalsIgnoreCase(searchHistory.searchedValue)) {
					isExist = true;
				}
			}
			if (!isExist) {
				histories.add(0, searchHistory);
				if (histories.size() > 5)
					histories.remove(5);
				sessionDao.value = gson.toJson(histories);
				try {
					sessionDao.save();
				} catch (Exception e) {
					Log.e("TAG", e.getMessage());
				}
			}
		}
	}

	public List<SearchHistory> getRecentSearch() {
		List<SearchHistory> historyList = null;
		try {
			SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_PRODUCT_SEARCH);
			if (sessionDao.value == null) {
				historyList = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<SearchHistory>>() {
				}.getType();
				historyList = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return historyList;
	}

	public void hideSoftKeyboard() {
		if (getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			assert inputMethodManager != null;
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	@SuppressLint("InflateParams")
	public void showRecentSearchHistoryView(boolean status) {
		recentSearchList.removeAllViews();
		View storeItem = getLayoutInflater().inflate(R.layout.stores_recent_search_header_row, null);
		recentSearchList.addView(storeItem);
		List<SearchHistory> searchHistories = getRecentSearch();
		if (status && searchHistories != null) {
			for (int i = 0; i < searchHistories.size(); i++) {
				View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
				WTextView recentSearchListitem = v.findViewById(R.id.recentSerachListItem);
				recentSearchListitem.setText(searchHistories.get(i).searchedValue);
				recentSearchList.addView(v);
				int position = recentSearchList.indexOfChild(v) - 1;
				v.setTag(position);
				v.setOnClickListener(this);
			}
			recentSearchLayout.setVisibility(View.VISIBLE);
		} else {
			recentSearchLayout.setVisibility(View.GONE);
		}
	}

	private void canGoBack() {
		this.finish();
		overridePendingTransition(0, R.anim.fade_out);
	}

	@Override
	public void onClick(View v) {
		int pos = (Integer) v.getTag();
		showRecentSearchHistoryView(false);
		searchProduct(getRecentSearch().get(pos).searchedValue);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH);
		showRecentSearchHistoryView(true);

	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE)
                || (requestCode == SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE)) {
            setActivityResult(data, ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE);
        } 

        if (requestCode == SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE && resultCode == ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            setActivityResult(data, ADDED_TO_SHOPPING_LIST_RESULT_CODE);
        }
    }

    private void setActivityResult(Intent data, int addToShoppingListResultCode) {
        setResult(addToShoppingListResultCode, data);
        finish();
        overridePendingTransition(0, 0);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideSoftKeyboard();
	}

	@Override
	public void onDialogDismiss() {
		mEditSearchProduct.setText("");
	}
}
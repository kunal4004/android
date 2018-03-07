package za.co.woolworths.financial.services.android.ui.activities.product;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductSearchActivity extends AppCompatActivity
		implements View.OnClickListener {
	public RecyclerView productListview;
	public LinearLayoutManager mLayoutManager;
	public Toolbar toolbar;
	private WEditTextView mEditSearchProduct;
	private LinearLayout recentSearchLayout;
	private LinearLayout recentSearchList;

	PopWindowValidationMessage mPopWindowValidationMessage;

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
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					searchProduct(mEditSearchProduct.getText().toString());
					return true;
				}
				return false;
			}
		});
	}

	private void initUI() {
		mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
		productListview = (RecyclerView) findViewById(R.id.productSearchList);
		mEditSearchProduct = (WEditTextView) findViewById(R.id.toolbarText);
		recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
		recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
		mPopWindowValidationMessage = new PopWindowValidationMessage(this);
	}

	private void setActionBar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_search);
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
			SearchHistory search = new SearchHistory();
			search.searchedValue = searchProductBrand;
			LoadState loadState = new LoadState();
			saveRecentSearch(search);
			loadState.setSearchProduct(searchProductBrand);
			(WoolworthsApplication.getInstance())
					.bus()
					.send(loadState);
			mEditSearchProduct.setText("");
			finish();
			overridePendingTransition(0, 0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_item, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void saveRecentSearch(SearchHistory searchHistory) {
		List<SearchHistory> histories = null;
		histories = new ArrayList<>();
		histories = getRecentSearch();
		SessionDao sessionDao = new SessionDao(ProductSearchActivity.this);
		sessionDao.key = SessionDao.KEY.STORES_PRODUCT_SEARCH;
		Gson gson = new Gson();
		boolean isExist = false;
		if (histories == null) {
			histories = new ArrayList<>();
			histories.add(0, searchHistory);
			String json = gson.toJson(histories);
			sessionDao.value = json;
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
			SessionDao sessionDao = new SessionDao(ProductSearchActivity.this, SessionDao.KEY.STORES_PRODUCT_SEARCH).get();
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
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	public void showRecentSearchHistoryView(boolean status) {
		recentSearchList.removeAllViews();
		View storeItem = getLayoutInflater().inflate(R.layout.stores_recent_search_header_row, null);
		recentSearchList.addView(storeItem);
		List<SearchHistory> searchHistories = getRecentSearch();
		if (status && searchHistories != null) {
			for (int i = 0; i < searchHistories.size(); i++) {
				View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
				WTextView recentSearchListitem = (WTextView) v.findViewById(R.id.recentSerachListItem);
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
		showRecentSearchHistoryView(true);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideSoftKeyboard();
	}
}
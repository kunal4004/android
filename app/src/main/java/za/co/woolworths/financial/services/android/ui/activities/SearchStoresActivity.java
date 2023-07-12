package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.adapters.StoreSearchListAdapter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.Utils;

public class SearchStoresActivity extends AppCompatActivity implements View.OnClickListener {
	public Toolbar toolbar;
	public RecyclerView recyclerView;
	StoreSearchListAdapter searchAdapter;
	public List<StoreDetails> storeDetailsList;
	Handler mHandler;
	SearchView searchView;
	LinearLayout recentSearchLayout;
	LinearLayout recentSearchList;
	RelativeLayout searchErrorLayout;
	TextView recentSearchListitem;
	SearchHistory search;
	public static final String TAG = "SearchStoresActivity";
	private ErrorHandlerView mErrorHandlerView;
	public String mSearchText = "";
	private Call<LocationResponse> mSearchRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_store_activity);
		Utils.updateStatusBarBackground(this);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		recyclerView = (RecyclerView) findViewById(R.id.storeList);
		recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
		recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
		searchErrorLayout = (RelativeLayout) findViewById(R.id.search_Error);
		mErrorHandlerView = new ErrorHandlerView(this
				, (RelativeLayout) findViewById(R.id.no_connection_layout));
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		showRecentSearchHistoryView(true);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mHandler = new Handler();
		recyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();
				return false;
			}
		});

		recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getApplicationContext(), recyclerView, new RecycleViewClickListner.ClickListener() {
			@Override
			public void onClick(View view, int position) {
				/*
				 *when the user clicks on one of the cards which resulted from the search
                 *the query is saved in the recent search list
                 */
				storeRecentSearch();

				Gson gson = new Gson();
				String store = gson.toJson(storeDetailsList.get(position));
				startActivity(new Intent(getApplicationContext(), StoreDetailsActivity.class).putExtra("store", store));
			}

			@Override
			public void onLongClick(View view, int position) {

			}
		}));
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


				super.onScrolled(recyclerView, dx, dy);
			}
		});

		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(SearchStoresActivity.this)) {
					if (mSearchText.length() >= 2)
						mSearchRequest = startSearch(mSearchText);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STORES_SEARCH);
	}

	@Override
	public void onClick(View v) {
		int pos = (Integer) v.getTag();
		showRecentSearchHistoryView(false);
		searchView.setQuery(getRecentSearch().get(pos).searchedValue, true);
	}

	public void storeRecentSearch() {
		String query = searchView.getQuery().toString();
		search = new SearchHistory();
		search.searchedValue = query;
		if (query.length() >= 2) {
			saveRecentSearch(search);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.w_search_store_menu, menu);
		final MenuItem searchViewItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
		searchView.setIconified(false);
		final ImageView mCloseButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
		mCloseButton.setImageResource(R.drawable.close_24);
		mCloseButton.setVisibility(View.GONE);
		SpannableMenuOption spannableMenuOption = new SpannableMenuOption(this);
		searchView.setQueryHint(spannableMenuOption.customSpannableSearch(getString(R.string.search_by_store_loc)));
		// ImageView searchCloseIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
		// searchCloseIcon.setImageResource(R.drawable.close_24);
		final TextView searchText = (TextView)
				searchView.findViewById(R.id.search_src_text);
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
		searchText.setTypeface(font);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				searchText.setText("");
				searchView.clearFocus();
				searchErrorLayout.setVisibility(View.GONE);
				recentSearchLayout.setVisibility(View.VISIBLE);
				if (storeDetailsList != null) {
					storeDetailsList.clear();
				}
				mCloseButton.setVisibility(View.GONE);
			}
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				//searchViewAndroidActionBar.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {

				mCloseButton.setVisibility(newText.isEmpty() ? View.GONE : View.VISIBLE);

				mHandler.removeCallbacksAndMessages(null);

				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						//clear the array and repopulate it each time user types in a letter
						if (storeDetailsList != null) {
							storeDetailsList.clear();
							if (searchAdapter != null)
								searchAdapter.notifyDataSetChanged();
						}
						if (newText.isEmpty()) {
							searchErrorLayout.setVisibility(View.GONE);
							//if storeDetailsList is null before clearing it we get NullPointerException
							//Therefore we check if it is null before clearing it
							if (storeDetailsList != null) {
								storeDetailsList.clear();
							}

							showRecentSearchHistoryView(true);
						} else {
							showRecentSearchHistoryView(false);
							if (newText.length() >= 2) {
								mSearchText = newText;
								mSearchRequest = startSearch(newText);
							}
						}
					}
				}, 600);

				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	public Call<LocationResponse> startSearch(final String searchTextField) {
		Location location = Utils.getLastSavedLocation();

		double latitude = (location == null) ? 0.0 : location.getLatitude();
		double longitude = (location == null) ? 0.0 : location.getLongitude();

		Call<LocationResponse> locationRequestCall = new OneAppService().queryServiceGetStore(latitude, longitude, searchTextField);
		locationRequestCall.enqueue(new CompletionHandler<>(new IResponseListener<LocationResponse>() {
			@Override
			public void onSuccess(LocationResponse locationResponse) {
				storeDetailsList = new ArrayList<>();
				storeDetailsList = locationResponse.Locations;
				if (storeDetailsList != null && storeDetailsList.size() != 0) {
					searchAdapter = new StoreSearchListAdapter(SearchStoresActivity.this, storeDetailsList);
					recyclerView.setAdapter(searchAdapter);
					final TextView searchText = searchView.findViewById(R.id.search_src_text);
					searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_SEARCH) {
								// Your piece of code on keyboard search click
								storeRecentSearch();
								return true;
							}
							return true;
						}
					});
				} else {
					final TextView searchText = searchView.findViewById(R.id.search_src_text);
					searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_SEARCH) {
								// Your piece of code on keyboard search click
								recentSearchLayout.setVisibility(View.GONE);
								searchErrorLayout.setVisibility(View.VISIBLE);
								if (storeDetailsList != null) {
									storeDetailsList.clear();
								}
								return true;
							}
							return true;
						}
					});
				}
			}

			@Override
			public void onFailure(Throwable error) {
				hideSoftKeyboard();
				if (error != null)
					mErrorHandlerView.networkFailureHandler(error.getMessage());
			}
		},LocationResponse.class));

		return locationRequestCall;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//   hideSoftKeyboard();
				onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}

	public void showRecentSearchHistoryView(boolean status) {
		recentSearchList.removeAllViews();
		View storeItem = getLayoutInflater().inflate(R.layout.stores_recent_search_header_row, null);
		recentSearchList.addView(storeItem);
		List<SearchHistory> searchHistories = getRecentSearch();
		if (status && searchHistories != null) {
			for (int i = 0; i < searchHistories.size(); i++) {
				View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
				recentSearchListitem = v.findViewById(R.id.recentSerachListItem);
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

	public void hideSoftKeyboard() {
		if (getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	public void saveRecentSearch(SearchHistory searchHistory) {
		List<SearchHistory> histories = null;
		histories = new ArrayList<>();
		histories = getRecentSearch();
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_USER_SEARCH);
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
				Log.e(TAG, e.getMessage());
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

				String json = gson.toJson(histories);
				sessionDao.value = json;
				try {
					sessionDao.save();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}


	}

	public List<SearchHistory> getRecentSearch() {
		List<SearchHistory> historyList = null;
		try {
			SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_USER_SEARCH);
			if (sessionDao.value == null) {
				historyList = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<SearchHistory>>() {
				}.getType();
				historyList = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return historyList;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSearchRequest!=null && !mSearchRequest.isCanceled()){
			mSearchRequest.cancel();
		}
	}
}

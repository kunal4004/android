package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.adapters.StoreSearchListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.R.attr.data;
import static java.security.AccessController.getContext;


public class SearchStoresActivity extends AppCompatActivity implements View.OnClickListener {
    public Toolbar toolbar;
    public RecyclerView recyclerView;
    StoreSearchListAdapter searchAdapter;
    public List<StoreDetails> storeDetailsList;
    Handler mHandler;
    SearchView searchView;
    LinearLayout recentSearchLayout;
    LinearLayout recentSearchList;
    WTextView recentSearchListitem;
    SearchHistory search;
    public static final String TAG = "SearchStoresActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_store_activity);
        Utils.updateStatusBarBackground(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.storeList);
        recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
        recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
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

    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        showRecentSearchHistoryView(false);
        searchView.setQuery(getRecentSearch().get(pos).searchedValue, true);
        //Toast.makeText(getApplicationContext(),recentSearchData[pos],Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.w_search_store_menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setIconified(false);
        SpannableMenuOption spannableMenuOption = new SpannableMenuOption(this);
        searchView.setQueryHint(spannableMenuOption.customSpannableSearch(getString(R.string.search_by_store_loc)));
        // ImageView searchCloseIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        // searchCloseIcon.setImageResource(R.drawable.close_24);
        TextView searchText = (TextView)
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Regular.otf");
        searchText.setTypeface(font);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                return true;
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

                mHandler.removeCallbacksAndMessages(null);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.isEmpty()) {
                            //if storeDetailsList is null before clearing it we get NullPointerException
                            //Therefore we check if it is null before clearing it
                            if (storeDetailsList != null) {
                                storeDetailsList.clear();
                            }

                            showRecentSearchHistoryView(true);
                        } else {
                            showRecentSearchHistoryView(false);
                            if (newText.length() >= 2) {
                                startSearch(newText);
                            }
                        }
                    }
                }, 600);

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void startSearch(final String query) {
        new HttpAsyncTask<String, String, LocationResponse>() {
            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                search = new SearchHistory();
                search.searchedValue = query;
                saveRecentSearch(search);
            }

            @Override
            protected LocationResponse httpDoInBackground(String... params) {
                Location location = Utils.getLastSavedLocation(SearchStoresActivity.this);
                if (location != null) {
                    return ((WoolworthsApplication) getApplication()).getApi().getLocations(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), query, "50000");

                } else {
                    return ((WoolworthsApplication) getApplication()).getApi().getLocations(null, null, query, null);

                }
            }

            @Override
            protected Class<LocationResponse> httpDoInBackgroundReturnType() {
                return LocationResponse.class;
            }

            @Override
            protected LocationResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                LocationResponse locationResponse = new LocationResponse();
                locationResponse.response = new Response();
                return locationResponse;
            }

            @Override
            protected void onPostExecute(LocationResponse locationResponse) {
                super.onPostExecute(locationResponse);
                storeDetailsList = new ArrayList<StoreDetails>();
                storeDetailsList = locationResponse.Locations;
                if (storeDetailsList != null && storeDetailsList.size() != 0) {
                    searchAdapter = new StoreSearchListAdapter(SearchStoresActivity.this, storeDetailsList);
                    recyclerView.setAdapter(searchAdapter);
                }


            }
        }.execute();
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
            String previous = "";
            String current = "";
            for (int i = 0; i < searchHistories.size(); i++) {
                current = searchHistories.get(i).searchedValue.toLowerCase();
                Log.e("echoTrue", current);
                if (previous.startsWith(current)) {
                    Log.e("echoTrue", "current " + current + " previous " + previous);
                    searchHistories.remove(i);
                }
                previous = current;
            }
//            SessionDao sessionDao = new SessionDao(SearchStoresActivity.this);
//            sessionDao.key = SessionDao.KEY.STORES_USER_SEARCH;
//            Gson gson = new Gson();
//            String json = gson.toJson(searchHistories);
//            sessionDao.value = json;
//            try {
//                sessionDao.save();
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage());
//            }
            for (int i = 0; i < searchHistories.size(); i++) {
                View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
                recentSearchListitem = (WTextView) v.findViewById(R.id.recentSerachListItem);
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
        SessionDao sessionDao = new SessionDao(SearchStoresActivity.this);
        sessionDao.key = SessionDao.KEY.STORES_USER_SEARCH;
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
            SessionDao sessionDao = new SessionDao(SearchStoresActivity.this, SessionDao.KEY.STORES_USER_SEARCH).get();
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
}

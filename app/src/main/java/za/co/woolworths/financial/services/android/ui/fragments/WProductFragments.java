package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.activities.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductSearchSubCategoryActivity;
import za.co.woolworths.financial.services.android.ui.adapters.PSRootCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.LDObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.barcode.scanner.FullScannerActivity;
import za.co.woolworths.financial.services.android.util.binder.view.RootCategoryBinder;

public class WProductFragments extends Fragment implements RootCategoryBinder.OnClickListener, View.OnClickListener,
        AppBarLayout.OnOffsetChangedListener,LDObservableScrollView.LDObservableScrollViewListener {

    private ConnectionDetector mConnectionDetector;
    public  LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private ImageView mImProductSearch;
    private ImageView mImBarcodeScanner;
    public  WTextView mTextProductSearch;
    private RecyclerView mRecycleProductSearch;
    private PSRootCategoryAdapter mPSRootCategoryAdapter;
    private LinearLayoutManager mLayoutManager;
    private WProductFragments mContext;
    private List<RootCategory> mRootCategories;
    private SharePreferenceHelper mSharePreferenceHelper;

    private Class<?> mClss;
    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private View view;
    private RelativeLayout mRelSearchRowLayout;
    private Toolbar mProductToolbar;
    private LDObservableScrollView mNestedScrollview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        view = inflater.inflate(R.layout.loan_app_layout, container, false);
        mContext = this;
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
        mConnectionDetector = new ConnectionDetector();
        mLayoutInflater = (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mSlidingUpViewLayout = new SlidingUpViewLayout(getActivity(),mLayoutInflater);
        mProductToolbar = (Toolbar)view.findViewById(R.id.productToolbar);
        initUI(view);
        setUIListener();
        getRootCategoryRequest();
        mNestedScrollview.getParent().requestChildFocus(mNestedScrollview, mNestedScrollview);
        return view;
    }

    private void setUIListener() {
        mImProductSearch.setOnClickListener(this);
        mImBarcodeScanner.setOnClickListener(this);
        mTextProductSearch.setOnClickListener(this);
    }

    private void initUI(View v) {
        mNestedScrollview = (LDObservableScrollView)v.findViewById(R.id.mNestedScrollview);
        mImProductSearch = (ImageView)v.findViewById(R.id.imProductSearch);
        mImBarcodeScanner = (ImageView)v.findViewById(R.id.imBarcodeScanner);
        mTextProductSearch = (WTextView)v.findViewById(R.id.textProductSearch);
        mRecycleProductSearch = (RecyclerView)v.findViewById(R.id.recycleProductSearch);
        mRelSearchRowLayout=(RelativeLayout)v.findViewById(R.id.relSearchRowLayout);
        mNestedScrollview.setScrollViewListener(this);
    }

    private void getRootCategoryRequest(){
        if(mConnectionDetector.isOnline(getActivity())) {
            new HttpAsyncTask<String, String, RootCategories>() {
                @Override
                protected RootCategories httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getActivity().getApplication()).getApi().getRootCategory();
                }

                @Override
                protected RootCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    RootCategories rootCategories = new RootCategories();
                    return rootCategories;
                }

                @Override
                protected Class<RootCategories> httpDoInBackgroundReturnType() {
                    return RootCategories.class;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(RootCategories rootCategories) {
                    super.onPostExecute(rootCategories);
                    try {
                        switch (rootCategories.httpCode) {
                            case 200:
                                if (rootCategories.rootCategories != null) {
                                    mRootCategories = rootCategories.rootCategories;
                                    mPSRootCategoryAdapter =
                                            new PSRootCategoryAdapter(rootCategories.rootCategories, mContext);
                                    AlphaInAnimationAdapter alphaAdapter =
                                            new AlphaInAnimationAdapter(mPSRootCategoryAdapter);
                                    mRecycleProductSearch.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
                                    mRecycleProductSearch.getItemAnimator().setAddDuration(1500);
                                    mRecycleProductSearch.getItemAnimator().setRemoveDuration(1500);
                                    mRecycleProductSearch.getItemAnimator().setMoveDuration(1500);
                                    mRecycleProductSearch.getItemAnimator().setChangeDuration(1500);
                                    mLayoutManager = new LinearLayoutManager(getActivity());
                                    mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    mRecycleProductSearch.setLayoutManager(mLayoutManager);
                                    mRecycleProductSearch.setNestedScrollingEnabled(false);
                                    mRecycleProductSearch.setAdapter(alphaAdapter);
                                    mPSRootCategoryAdapter.setCLIContent();
                                }
                                break;

                            default:
                                mSlidingUpViewLayout.openOverlayView(rootCategories.response.desc,
                                        SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                                break;
                        }
                    }catch (NullPointerException ex){}
                }
            }.execute();
        }else {
            mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
        }
    }

    @Override
    public void onClick(View v, int position) {
        RootCategory rootCategory = mRootCategories.get(position);
        mSharePreferenceHelper.save(rootCategory.categoryId,"root_category_id");
        mSharePreferenceHelper.save(rootCategory.categoryName,"root_category_name");
        mSharePreferenceHelper.save("0","catStep");
        Intent openSubCategory = new Intent(getActivity(), ProductSearchSubCategoryActivity.class);
        startActivity(openSubCategory);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.textProductSearch:
            case R.id.imProductSearch:
                    Intent openProductSearchActivity = new Intent(getActivity(), ProductSearchActivity.class);
                    startActivity(openProductSearchActivity);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.imBarcodeScanner:
                launchActivity(FullScannerActivity.class);
                break;
        }
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
             ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(getActivity(), clss);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZBAR_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(getActivity(), mClss);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(0, 0);
                    }
                } else {}
                return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
       // ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
       // ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }

    @Override
    public void onScrollChanged(LDObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        // TODO Auto-generated method stub
        int searchRowHeight = Math.round(mRelSearchRowLayout.getHeight()+(getToolBarHeight()/2));
        if (searchRowHeight>y){
            showSearchBar();
        }else {
            hideSearchBar();
        }
    }

    public int getToolBarHeight() {
        int[] attrs = new int[] {R.attr.actionBarSize};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    private void hideSearchBar(){
        mRelSearchRowLayout.setAlpha(0);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setElevation(0);
        mProductToolbar.setVisibility(View.VISIBLE);
        mRelSearchRowLayout.setEnabled(false);
        mImProductSearch.setEnabled(false);
        mImBarcodeScanner.setEnabled(false);
        mTextProductSearch.setEnabled(false);
    }

    private void showSearchBar(){
       // mProductToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        mRelSearchRowLayout.setAlpha(1);
        mRelSearchRowLayout.setEnabled(true);
        mImProductSearch.setEnabled(true);
        mImBarcodeScanner.setEnabled(true);
        mTextProductSearch.setEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setElevation(0);
        mProductToolbar.setAlpha(0);
        mProductToolbar
                .animate()
                .setDuration(200)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRelSearchRowLayout.animate().setListener(null);
                    }
                });
    }
}

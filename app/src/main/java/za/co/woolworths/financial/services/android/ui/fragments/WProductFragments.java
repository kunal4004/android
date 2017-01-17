package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.barcode.scanner.ProductCategoryBarcodeActivity;
import za.co.woolworths.financial.services.android.util.binder.view.RootCategoryBinder;

public class WProductFragments extends Fragment implements RootCategoryBinder.OnClickListener, View.OnClickListener,
        AppBarLayout.OnOffsetChangedListener, LDObservableScrollView.LDObservableScrollViewListener {

    public interface HideActionBarComponent {
        void onActionBarComponent(boolean actionbarIsVisible);

        void onBurgerButtonPressed();
    }

    private HideActionBarComponent hideActionBarComponent;

    private static final int PERMS_REQUEST_CODE = 123;

    private ImageView mBurgerButtonPressed;
    private ImageView mTBBarcodeScanner;
    private ImageView mImProductSearch;
    private ImageView mImBarcodeScanner;
    private ConnectionDetector mConnectionDetector;
    public LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    public WTextView mTextProductSearch;
    private RecyclerView mRecycleProductSearch;
    private PSRootCategoryAdapter mPSRootCategoryAdapter;
    private LinearLayoutManager mLayoutManager;
    private WProductFragments mContext;
    private List<RootCategory> mRootCategories;
    private WTextView mTextTBProductSearch;

    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private RelativeLayout mRelSearchRowLayout;
    private Toolbar mProductToolbar;
    private LDObservableScrollView mNestedScrollview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        View view = inflater.inflate(R.layout.loan_app_layout, container, false);
        mContext = this;
        mConnectionDetector = new ConnectionDetector();
        mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSlidingUpViewLayout = new SlidingUpViewLayout(getActivity(), mLayoutInflater);
        mProductToolbar = (Toolbar) view.findViewById(R.id.productToolbar);
        initUI(view);
        setUIListener();
        getRootCategoryRequest();
        mNestedScrollview.getParent().requestChildFocus(mNestedScrollview, mNestedScrollview);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            hideActionBarComponent = (HideActionBarComponent) getActivity();
        } catch (ClassCastException ex) {
            Log.e("Interface", ex.toString());
        }
    }

    private void initUI(View v) {
        mNestedScrollview = (LDObservableScrollView) v.findViewById(R.id.mNestedScrollview);
        mImProductSearch = (ImageView) v.findViewById(R.id.imProductSearch);
        mImBarcodeScanner = (ImageView) v.findViewById(R.id.imBarcodeScanner);
        mRecycleProductSearch = (RecyclerView) v.findViewById(R.id.recycleProductSearch);
        mRelSearchRowLayout = (RelativeLayout) v.findViewById(R.id.relSearchRowLayout);
        mBurgerButtonPressed = (ImageView) v.findViewById(R.id.imBurgerButtonPressed);
        mTBBarcodeScanner = (ImageView) v.findViewById(R.id.imTBBarcodeScanner);
        mTextTBProductSearch = (WTextView) v.findViewById(R.id.textTBProductSearch);
        mTextProductSearch = (WTextView) v.findViewById(R.id.textProductSearch);
    }

    private void setUIListener() {
        mImProductSearch.setOnClickListener(this);
        mImBarcodeScanner.setOnClickListener(this);
        mTextProductSearch.setOnClickListener(this);
        mBurgerButtonPressed.setOnClickListener(this);
        mTextTBProductSearch.setOnClickListener(this);
        mTBBarcodeScanner.setOnClickListener(this);
        mNestedScrollview.setScrollViewListener(this);
    }

    private void getRootCategoryRequest() {
        if (mConnectionDetector.isOnline(getActivity())) {
            new HttpAsyncTask<String, String, RootCategories>() {
                @Override
                protected RootCategories httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getActivity().getApplication()).getApi().getRootCategory();
                }

                @Override
                protected RootCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    return new RootCategories();
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
                                    mPSRootCategoryAdapter = new PSRootCategoryAdapter(rootCategories.rootCategories, mContext);
                                    AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mPSRootCategoryAdapter);
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
                    } catch (NullPointerException ex) {
                        Log.e("NullPointer", ex.toString());
                    }
                }
            }.execute();
        } else {
            mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
        }
    }

    @Override
    public void onClick(View v, int position) {
        RootCategory rootCategory = mRootCategories.get(position);
        Intent openSubCategory = new Intent(getActivity(), ProductSearchSubCategoryActivity.class);
        openSubCategory.putExtra("root_category_id", rootCategory.categoryId);
        openSubCategory.putExtra("root_category_name", rootCategory.categoryName);
        openSubCategory.putExtra("catStep", 0);
        startActivity(openSubCategory);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textTBProductSearch:
            case R.id.textProductSearch:
            case R.id.imProductSearch:
                Intent openProductSearchActivity = new Intent(getActivity(), ProductSearchActivity.class);
                startActivity(openProductSearchActivity);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.imTBBarcodeScanner:
            case R.id.imBarcodeScanner:
                openCamera(ProductCategoryBarcodeActivity.class);
                break;
            case R.id.imBurgerButtonPressed:
                hideActionBarComponent.onBurgerButtonPressed();
                break;
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
    }

    @Override
    public void onScrollChanged(LDObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        // TODO Auto-generated method stub
        int searchRowHeight = Math.round(mRelSearchRowLayout.getHeight() + (getToolBarHeight() / 2));
        if (searchRowHeight > y) {
            showSearchBar();
        } else {
            hideSearchBar();
        }
    }

    public int getToolBarHeight() {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    private void hideSearchBar() {
        mRelSearchRowLayout.setAlpha(0);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.hide();
        actionBar.setElevation(0);
        mProductToolbar.setVisibility(View.VISIBLE);
        mRelSearchRowLayout.setEnabled(false);
        mImProductSearch.setEnabled(false);
        mImBarcodeScanner.setEnabled(false);
        mTextProductSearch.setEnabled(false);
        hideActionBarComponent.onActionBarComponent(false);
    }

    private void showSearchBar() {
        mRelSearchRowLayout.setAlpha(1);
        mRelSearchRowLayout.setEnabled(true);
        mImProductSearch.setEnabled(true);
        mImBarcodeScanner.setEnabled(true);
        mTextProductSearch.setEnabled(true);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setElevation(0);
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
        hideActionBarComponent.onActionBarComponent(true);
    }

    public void setHideActionBarComponent(HideActionBarComponent hideActionBarComponent) {
        this.hideActionBarComponent = hideActionBarComponent;
    }

    public boolean hasPermissions() {
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.CAMERA};

        for (String perms : permissions) {
            res = getActivity().checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    public void openCamera(Class<?> clss) {
        if (hasPermissions()) {
            Intent intent = new Intent(getActivity(), clss);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
        } else {
            requestPerms();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case PERMS_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }
        if (allowed) {
            //user granted all permissions we can perform our task.
            openCamera(ProductCategoryBarcodeActivity.class);
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getActivity(), "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

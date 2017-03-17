package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;

import java.util.List;


import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.activities.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductSearchSubCategoryActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductViewGridActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.RootCategoryBinder;
import za.co.woolworths.financial.services.android.util.zxing.QRActivity;

public class WProductFragment extends Fragment implements RootCategoryBinder.OnClickListener, View.OnClickListener,
        AppBarLayout.OnOffsetChangedListener, SelectedProductView {

    private WTextView mToolbarText;
    private boolean actionBarIsHidden = false;
    private ActionBar mAppToolbar;

    @Override
    public void onSelectedProduct(View v, int position) {
        RootCategory rootCategory = mRootCategories.get(position);
        if (rootCategory.hasChildren) {
            Intent openSubCategory = new Intent(getActivity(), ProductSearchSubCategoryActivity.class);
            openSubCategory.putExtra("root_category_id", rootCategory.categoryId);
            openSubCategory.putExtra("root_category_name", rootCategory.categoryName);
            openSubCategory.putExtra("catStep", 0);
            startActivity(openSubCategory);
            getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            Intent openProductListIntent = new Intent(getActivity(), ProductViewGridActivity.class);
            openProductListIntent.putExtra("sub_category_name", rootCategory.categoryName);
            openProductListIntent.putExtra("sub_category_id", rootCategory.categoryId);
            startActivity(openProductListIntent);
            getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    }

    @Override
    public void onLongPressState(View v, int position) {

    }

    @Override
    public void onSelectedColor(View v, int position) {

    }

    public interface HideActionBarComponent {
        void onBurgerButtonPressed();
    }

    private HideActionBarComponent hideActionBarComponent;

    private static final int PERMS_REQUEST_CODE = 123;

    private ImageView mBurgerButtonPressed;
    private ImageView mTBBarcodeScanner;
    private ImageView mImProductSearch;
    private ImageView mImBarcodeScanner;
    private ConnectionDetector mConnectionDetector;
    public WTextView mTextProductSearch;
    private RecyclerView mRecycleProductSearch;
    private WProductFragment mContext;
    private List<RootCategory> mRootCategories;
    private WTextView mTextTBProductSearch;
    private RelativeLayout mRelSearchRowLayout;
    private Toolbar mProductToolbar;
    private NestedScrollView mNestedScrollview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = this;
        mAppToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mConnectionDetector = new ConnectionDetector();
        mProductToolbar = (Toolbar) view.findViewById(R.id.productToolbar);
        initUI(view);
        setUIListener();
        showAccountToolbar();
        mNestedScrollview.getParent().requestChildFocus(mNestedScrollview, mNestedScrollview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRootCategoryRequest();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            hideActionBarComponent = (HideActionBarComponent) getActivity();
        } catch (ClassCastException ignored) {
        }
    }

    private void initUI(View v) {
        mNestedScrollview = (NestedScrollView) v.findViewById(R.id.mNestedScrollview);
        mImProductSearch = (ImageView) v.findViewById(R.id.imProductSearch);
        mImBarcodeScanner = (ImageView) v.findViewById(R.id.imBarcodeScanner);
        mRecycleProductSearch = (RecyclerView) v.findViewById(R.id.recycleProductSearch);
        mRelSearchRowLayout = (RelativeLayout) v.findViewById(R.id.relSearchRowLayout);
        mBurgerButtonPressed = (ImageView) v.findViewById(R.id.imBurgerButtonPressed);
        mTBBarcodeScanner = (ImageView) v.findViewById(R.id.imTBBarcodeScanner);
        mTextTBProductSearch = (WTextView) v.findViewById(R.id.textTBProductSearch);
        mTextProductSearch = (WTextView) v.findViewById(R.id.textProductSearch);
        mToolbarText = (WTextView) v.findViewById(R.id.toolbarText);
    }

    private void setUIListener() {
        mImProductSearch.setOnClickListener(this);
        mImBarcodeScanner.setOnClickListener(this);
        mTextProductSearch.setOnClickListener(this);
        mBurgerButtonPressed.setOnClickListener(this);
        mTextTBProductSearch.setOnClickListener(this);
        mTBBarcodeScanner.setOnClickListener(this);
        mNestedScrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int searchRowHeight = Math.round(mRelSearchRowLayout.getHeight() + (getToolBarHeight()) + getToolBarHeight() / 2);
                if (scrollY < searchRowHeight) {
                    showViews();
                } else {
                    hideViews();
                }
            }
        });
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

                                    bindViewWithUI(mRootCategories);
                                }
                                break;

                            default:
                                if (!TextUtils.isEmpty(rootCategories.response.desc)) {
                                    Utils.displayValidationMessage(getActivity(),
                                            TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                            getString(R.string.connect_to_server));
                                }
                                break;
                        }
                    } catch (NullPointerException ignored) {
                    }
                }
            }.execute();
        } else {
            Utils.displayValidationMessage(getActivity(),
                    TransientActivity.VALIDATION_MESSAGE_LIST.INFO,
                    getString(R.string.connect_to_server));
        }
    }

    @Override
    public void onClick(View v, int position) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textTBProductSearch:
            case R.id.textProductSearch:
            case R.id.imProductSearch:
                Intent openProductSearchActivity = new Intent(getActivity(), ProductSearchActivity.class);
                startActivity(openProductSearchActivity);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.imTBBarcodeScanner:
            case R.id.imBarcodeScanner:
                onpenBarcodeScanner(QRActivity.class);
                break;
            case R.id.imBurgerButtonPressed:
                hideActionBarComponent.onBurgerButtonPressed();
                break;

        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {


    }

    public int getToolBarHeight() {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    public boolean hasPermissions() {
        int res;
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

    public void onpenBarcodeScanner(Class<?> clss) {
        if (hasPermissions()) {
            Intent intent = new Intent(getActivity(), clss);
            intent.putExtra("SCAN_MODE", "ONE_D_MODE");
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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
            onpenBarcodeScanner(QRActivity.class);
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getActivity(), "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void hideViews() {
        mRelSearchRowLayout.setAlpha(0);
        if (!actionBarIsHidden) {
            mProductToolbar.animate()
                    .translationY(-mProductToolbar.getBottom())
                    .setInterpolator(new AccelerateInterpolator())
                    //  .setDuration(ANIMATION_START_DURATION)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            showBarcodeToolbar();
                            actionBarIsHidden = true;
                            mProductToolbar
                                    .animate()
                                    .translationY(0)
                                    // .setDuration(ANIMATION_END_DURATION)
                                    .start();
                        }
                    }).start();
        }
    }

    private void showViews() {
        if (actionBarIsHidden) {
            mProductToolbar.animate()
                    .translationY(-mProductToolbar.getBottom())
                    .setInterpolator(new DecelerateInterpolator())
                    //  .setDuration(ANIMATION_START_DURATION)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            showAccountToolbar();
                            mRelSearchRowLayout.setAlpha(1);
                            mProductToolbar
                                    .animate()
                                    .translationY(0)
                                    //  .setDuration(ANIMATION_END_DURATION)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            showAccountToolbar();
                                        }
                                    })
                                    .start();
                            actionBarIsHidden = false;
                        }
                    }).start();
        }
    }

    private void showAccountToolbar() {
        mToolbarText.setVisibility(View.VISIBLE);
        mTBBarcodeScanner.setVisibility(View.GONE);
        mTextTBProductSearch.setVisibility(View.GONE);
        mImProductSearch.setEnabled(true);
        mImBarcodeScanner.setEnabled(true);
        mTextProductSearch.setEnabled(true);
    }

    private void showBarcodeToolbar() {
        mToolbarText.setVisibility(View.GONE);
        mTBBarcodeScanner.setVisibility(View.VISIBLE);
        mTextTBProductSearch.setVisibility(View.VISIBLE);
        mImProductSearch.setEnabled(false);
        mImBarcodeScanner.setEnabled(false);
        mTextProductSearch.setEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAppToolbar != null)
            mAppToolbar.hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAppToolbar != null)
            mAppToolbar.show();
    }

    private void bindViewWithUI(List<RootCategory> rootCategories) {
        mRootCategories = rootCategories;
        ProductCategoryAdapter myAdapter = new ProductCategoryAdapter(rootCategories, mContext);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleProductSearch.setHasFixedSize(true);
        mRecycleProductSearch.setLayoutManager(mLayoutManager);
        mRecycleProductSearch.setNestedScrollingEnabled(false);
        mRecycleProductSearch.setAdapter(myAdapter);
    }
}

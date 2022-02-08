package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ExpandableSubCategoryFragmentBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.ImageManager;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.expand.ExpandableRecyclerAdapter;
import za.co.woolworths.financial.services.android.util.expand.ParentSubCategoryViewHolder;
import za.co.woolworths.financial.services.android.util.expand.SubCategoryAdapter;
import za.co.woolworths.financial.services.android.util.expand.SubCategoryChild;
import za.co.woolworths.financial.services.android.util.expand.SubCategoryModel;

public class SubCategoryFragment extends BaseFragment<ExpandableSubCategoryFragmentBinding, SubCategoryViewModel> implements SubCategoryNavigator, View.OnClickListener {

    private List<SubCategory> mSubCategories;
    private RecyclerView rvCategoryDrill;
    private SubCategoryAdapter mAdapter;
    private int lastExpandedPosition = -1;
    private RootCategory mRootCategory;
    private SubCategoryViewModel mDrillDownCategoryViewModel;
    private ErrorHandlerView mErrorHandlerView;
    private int mSelectedHeaderPosition;
    private ParentSubCategoryViewHolder mParentViewHolder;
    private List<SubCategoryModel> mSubCategoryListModel;
    private String version;
    public static final int ERROR_DIALOG_REQUEST = 1456;

    @Override
    public int getLayoutId() {
        return R.layout.expandable_sub_category_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDrillDownCategoryViewModel = ViewModelProviders.of(this).get(SubCategoryViewModel.class);
        mDrillDownCategoryViewModel.setNavigator(this);
        Bundle bundle = this.getArguments();
        mSubCategories = new ArrayList<>();
        if (bundle != null) {
            String rootCategory = bundle.getString("ROOT_CATEGORY");
            version = bundle.getString("VERSION", "");
            if (rootCategory != null)
                mRootCategory = new Gson().fromJson(rootCategory, RootCategory.class);
            mRootCategory = new Gson().fromJson(rootCategory, RootCategory.class);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvCategoryDrill = getViewDataBinding().rcvDrillCategory;
        mErrorHandlerView = new ErrorHandlerView(getActivity(), getViewDataBinding().rlNoConnection);

        // RecyclerView has some built in animations to it, using the DefaultItemAnimator.
        // Specifically when you call notifyItemChanged() it does a fade animation for the changing
        // of the data in the ViewHolder. If you would like to disable this you can use the following:
        RecyclerView.ItemAnimator animator = rvCategoryDrill.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(true);
        }

        setHeader(mRootCategory);
        rvCategoryDrill.setLayoutManager(new LinearLayoutManager(getActivity()));

        onRetryConnectionClicked(mRootCategory.categoryId, false, version);
        getViewDataBinding().btnRetry.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.SHOP_SUB_CATEGORIES);
    }

    @Override
    public SubCategoryViewModel getViewModel() {
        return mDrillDownCategoryViewModel;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public void bindSubCategoryResult(List<SubCategory> subCategoryList) {
        if (getViewModel().childItem()) { // child item
            List<SubCategoryChild> subCategoryChildList = new ArrayList<>();
            for (SubCategory subCat : subCategoryList) {
                SubCategoryChild subCategoryChild = new SubCategoryChild();
                subCategoryChild.setSubCategory(subCat);
                subCategoryChildList.add(subCategoryChild);
            }
            SubCategoryModel subCategoryModel = mSubCategoryListModel.get(mSelectedHeaderPosition);
            subCategoryModel.setSubCategoryChildList(subCategoryChildList);
            if (mAdapter != null) {
                mAdapter.updateList(mSubCategoryListModel, mParentViewHolder, mSelectedHeaderPosition);
            }

            return;
        }

        setCategoryAdapter(subCategoryList); // header item
    }

    @Override
    public void unhandledResponseHandler(final Response response) {
        if (getViewModel().childItem()) {
            if (mAdapter != null)
                mAdapter.hideChildItemProgressBar();
            subcategoryOtherHttpResponse(response);
        } else {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideView(getViewDataBinding().pbSubCategory);
                    hideView(getViewDataBinding().rcvDrillCategory);
                    showView(getViewDataBinding().rootDrillDownCategory);
                    subcategoryOtherHttpResponse(response);
                }
            });
        }
    }

    private void subcategoryOtherHttpResponse(Response response) {
        if (!TextUtils.isEmpty(response.desc)) {
            Utils.displayValidationMessageForResult(getActivity(),
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, ERROR_DIALOG_REQUEST);
        }
    }

    @Override
    public void onFailureResponse(String e) {
        connectionFailureUI(e);
        getViewDataBinding().pbSubCategory.setVisibility(View.GONE);
        getViewDataBinding().rcvDrillCategory.setVisibility(View.GONE);
    }

    private void connectionFailureUI(String e) {
        getViewDataBinding().rootDrillDownCategory.setVisibility(View.VISIBLE);
        getViewDataBinding().rcvDrillCategory.setVisibility(View.GONE);
        mErrorHandlerView.networkFailureHandler(e);
    }

    @Override
    public void onLoad() {
        if (!getViewModel().childItem()) {
            showProgressBar(true);
        }
    }

    @Override
    public void onLoadComplete() {
        showProgressBar(false);
    }

    @Override
    public void onChildItemClicked(SubCategory subCategory) {
        //Navigate to product grid
        pushFragment(ProductListingFragment.Companion.newInstance(ProductsRequestParams.SearchType.NAVIGATE, subCategory.categoryName, subCategory.dimValId));
    }

    @Override
    public void noConnectionDetected() {
        if (mErrorHandlerView != null) {
            mErrorHandlerView.showToast();
            Utils.toggleStatusBarColor(getActivity(), R.color.red);
        }
    }

    @Override
    public void retrieveChildItem(ParentSubCategoryViewHolder holder, SubCategory subCategory, int selectedHeaderPosition) {
        this.mSelectedHeaderPosition = selectedHeaderPosition;
        this.mParentViewHolder = holder;
        onRetryConnectionClicked(subCategory.categoryId, true, version);
    }

    @Override
    public void onCloseIconPressed() {
        popFragmentSlideDown();
    }

    private void setHeader(RootCategory mRootCategory) {
        if (mRootCategory != null) {
            ImageManager.Companion.setPictureCenterInside(getViewDataBinding().imProductCategory, mRootCategory.imgUrl);
            getViewDataBinding().tvCategoryName.setText(mRootCategory.categoryName);
            getViewDataBinding().imClose.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imClose:
                popFragmentSlideDown();
                break;
            case R.id.btnRetry:
                onRetryConnectionClicked(mRootCategory.categoryId, false, version);
                break;
            default:
                break;
        }
    }

    private void onRetryConnectionClicked(String categoryId, boolean childItem, String version) {
        if (isNetworkConnected()) {
            mErrorHandlerView.hideErrorHandler();
            //ChildItem params determine whether to perform header or child operation
            getViewModel().setChildItem(childItem);
            getViewModel().executeSubCategory(categoryId, version);
        } else {
            if (!getViewModel().childItem()) {
                connectionFailureUI("e");
            }
        }
    }

    private void setCategoryAdapter(List<SubCategory> subCategories) {
        /***
         * Check for NullPointerException on Activity to prevent
         * Attempt to invoke virtual method 'java.lang.Object android.content.Context.getSystemService
         */
        Activity activity = getActivity();
        if (activity == null) return;
        this.mSubCategories = subCategories;
        SubCategory subHeaderCategory = new SubCategory();
        subHeaderCategory.setCategoryId(mRootCategory.categoryId);
        subHeaderCategory.setCategoryName(mRootCategory.categoryName);
        subHeaderCategory.setHasChildren(mRootCategory.hasChildren);
        subHeaderCategory.setImgUrl(mRootCategory.imgUrl);
        subHeaderCategory.setHasChildren(false);
        mSubCategories.add(0, subHeaderCategory);
        RecyclerView expandableSubCategory = getViewDataBinding().rcvDrillCategory;
        assert expandableSubCategory != null;
        mSubCategoryListModel = new ArrayList<>();
        for (SubCategory subCategory : mSubCategories) {
            mSubCategoryListModel.add(new SubCategoryModel(subCategory, null));
        }
        this.mAdapter = new SubCategoryAdapter(activity, this, mSubCategoryListModel);
        this.mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                if (lastExpandedPosition != -1
                        && position != lastExpandedPosition) {
                    mAdapter.collapseParent(lastExpandedPosition);
                }
                lastExpandedPosition = position;
                LinearLayoutManager llm = (LinearLayoutManager) rvCategoryDrill.getLayoutManager();
                llm.scrollToPositionWithOffset(position, 0);
            }

            @Override
            public void onListItemCollapsed(int position) {

            }
        });
        rvCategoryDrill.setAdapter(mAdapter);
    }

    private void showProgressBar(boolean visible) {
        getViewDataBinding().pbSubCategory.setVisibility(visible ? View.VISIBLE : View.GONE);
        getViewDataBinding().rootDrillDownCategory.setVisibility(visible ? View.VISIBLE : View.GONE);
        getViewDataBinding().rcvDrillCategory.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            hideToolbar();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Comment what's actually happening here.
        if (requestCode == ERROR_DIALOG_REQUEST && resultCode == Activity.RESULT_CANCELED) {
            Activity activity = getActivity();
            if (activity instanceof BottomNavigationActivity) {
                activity.onBackPressed();
                ((BottomNavigationActivity) activity).reloadDepartmentFragment();
            }
        }
    }

}
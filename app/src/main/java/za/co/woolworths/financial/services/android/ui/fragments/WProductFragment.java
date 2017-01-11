package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.awfs.coordination.R;

import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.ui.adapters.PSRootCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;

/**
 * Created by W7099877 on 02/12/2016.
 */

public class WProductFragment extends Fragment {

    private ConnectionDetector mConnectionDetector;
    private LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private ImageView mImProductSearch;
    private ImageView mImBarcodeScanner;
    private WEditTextView mEditProductSearch;
    private RecyclerView mRecycleProductSearch;
    private PSRootCategoryAdapter mPSRootCategoryAdapter;
    private LinearLayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_root_category_fragment, container, false);

        mConnectionDetector = new ConnectionDetector();
        mLayoutInflater = (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mSlidingUpViewLayout = new SlidingUpViewLayout(getActivity(),mLayoutInflater);

        initUI(view);
        getRootCategoryRequest();
        return view;
    }

    private void initUI(View v) {
        mImProductSearch = (ImageView)v.findViewById(R.id.imProductSearch);
        mImBarcodeScanner = (ImageView)v.findViewById(R.id.imBarcodeScanner);
        mEditProductSearch = (WEditTextView)v.findViewById(R.id.editProductSearch);
        mRecycleProductSearch = (RecyclerView)v.findViewById(R.id.recycleProductSearch);
    }

    private void getRootCategoryRequest(){
        new HttpAsyncTask<String, String, RootCategories>() {
            @Override
            protected RootCategories httpDoInBackground(String... params) {
                return ((WoolworthsApplication)getActivity().getApplication()).getApi().getRootCategory();
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

                switch (rootCategories.httpCode){
                    case 200:
                        if(rootCategories.rootCategories!=null) {
                            mPSRootCategoryAdapter = new PSRootCategoryAdapter(rootCategories.rootCategories);
                            mLayoutManager = new LinearLayoutManager(getActivity());
                            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            mRecycleProductSearch.setLayoutManager(mLayoutManager);
                            mRecycleProductSearch.setNestedScrollingEnabled(false);
                            mRecycleProductSearch.setAdapter(mPSRootCategoryAdapter);
                            mPSRootCategoryAdapter.setCLIContent();
                        }
                        break;

                    default:
                        mSlidingUpViewLayout.openOverlayView(rootCategories.response.desc,
                                SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                        break;
                }

                Log.e("rootCategories",String.valueOf(rootCategories));
            }

        }.execute();
    }


}

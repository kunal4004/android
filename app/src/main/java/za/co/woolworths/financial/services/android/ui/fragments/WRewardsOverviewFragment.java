package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.adapters.FeaturedPromotionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 05/01/2017.
 */


public class WRewardsOverviewFragment extends Fragment {
    public ImageView infoImage;
    public WTextView tireStatus;
    public WTextView savings;
    public WTextView toNextTire;
    public RelativeLayout toNextTireLayout;
    public VoucherResponse voucherResponse;
    public ViewPager promotionViewPager;
    public  String currentStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_overview_fragment, container, false);
        Bundle bundle=getArguments();
        voucherResponse=new Gson().fromJson(bundle.getString("WREWARDS"),VoucherResponse.class);
        currentStatus = voucherResponse.tierInfo.currentTier.toUpperCase();
        infoImage=(ImageView)view.findViewById(R.id.infoImage);
        tireStatus=(WTextView)view.findViewById(R.id.tireStatus);
        savings=(WTextView)view.findViewById(R.id.savings);
        toNextTire=(WTextView)view.findViewById(R.id.toNextTire);
        toNextTireLayout=(RelativeLayout) view.findViewById(R.id.toNextTireLayout);
        promotionViewPager=(ViewPager)view.findViewById(R.id.promotionViewPager);
        tireStatus.setText(voucherResponse.tierInfo.currentTier);
        savings.setText(WFormatter.formatAmount(voucherResponse.tierInfo.earned));
        if (currentStatus.equals(getString(R.string.valued)) || currentStatus.equals(getString(R.string.loyal))) {
            toNextTireLayout.setVisibility(View.VISIBLE);
            toNextTire.setText(WFormatter.formatAmount(voucherResponse.tierInfo.toSpend));
        }


        infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatus.equals(getString(R.string.valued))) {
                    redirectToWRewardsMemberActivity(0);
                }
                else  if (currentStatus.equals(getString(R.string.loyal))) {
                    redirectToWRewardsMemberActivity(1);
                }
                else  if (currentStatus.equals(getString(R.string.vip))) {
                    redirectToWRewardsMemberActivity(2);
                }
            }
        });
        promotionViewPager.setClipToPadding(false);
        loadPromotions();
        return view;
    }
    public void loadPromotions(){
        new HttpAsyncTask<String, String, PromotionsResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected PromotionsResponse httpDoInBackground(String... params) {

                return ((WoolworthsApplication) getActivity().getApplication()).getApi().getPromotions();
            }

            @Override
            protected Class<PromotionsResponse> httpDoInBackgroundReturnType() {
                return PromotionsResponse.class;
            }

            @Override
            protected PromotionsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                PromotionsResponse promotionsResponse = new PromotionsResponse();
                promotionsResponse.response = new Response();
                return promotionsResponse;
            }

            @Override
            protected void onPostExecute(PromotionsResponse promotionsResponse) {
                super.onPostExecute(promotionsResponse);
                handlePromotionResponse(promotionsResponse);

            }
        }.execute();
    }
    public void handlePromotionResponse(PromotionsResponse promotionsResponse)
    {
             switch (promotionsResponse.httpCode)
             {
                 case 200:
                     if(promotionsResponse.promotions.size()>0)
                     {
                         promotionViewPager.setAdapter(new FeaturedPromotionsAdapter(getActivity(),promotionsResponse.promotions));
                     }
                     break;
                 default:
                     break;
             }
    }
    public void redirectToWRewardsMemberActivity( int type)
    {
        startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class).putExtra("type",type));
    }
}
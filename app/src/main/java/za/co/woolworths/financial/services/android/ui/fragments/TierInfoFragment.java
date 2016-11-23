package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.text.ParseException;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.StatementHistoryActivity;
import za.co.woolworths.financial.services.android.ui.activities.StatusInfoActivity;
import za.co.woolworths.financial.services.android.ui.activities.WebViewActivity;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class TierInfoFragment extends Fragment {

    private VoucherResponse mVoucherResponse;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.tier_info_fragment, container, false);
        inflate.findViewById(R.id.tier_info_vip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(getActivity(), WebViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title","BLACK CREDIT CARD");
                bundle.putString("link", WoolworthsApplication.getRewardingLink());
                i.putExtra("Bundle",bundle);
                startActivity(i);
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.vip_url)));
                //startActivity(browserIntent);
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StatusInfoActivity.class);
                intent.putExtra(StatusInfoActivity.ID, v.getId());
                intent.putExtra(StatusInfoActivity.DATA, new Gson().toJson(mVoucherResponse));
                startActivity(intent);
            }
        };
        inflate.findViewById(R.id.tier_info_tier_1).setOnClickListener(onClickListener);
        inflate.findViewById(R.id.tier_info_tier_2).setOnClickListener(onClickListener);
        inflate.findViewById(R.id.tier_info_tier_3).setOnClickListener(onClickListener);
        inflate.findViewById(R.id.tier_info_bottom_bar_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), StatementHistoryActivity.class);
                intent.putExtra(StatementHistoryActivity.DATA, new Gson().toJson(mVoucherResponse));
                startActivity(intent);
            }
        });
        inflate.findViewById(R.id.bottomLayoutRewards).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), StatementHistoryActivity.class);
                intent.putExtra(StatementHistoryActivity.DATA, new Gson().toJson(mVoucherResponse));
                startActivity(intent);
            }
        });
        return inflate;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        update(mVoucherResponse);
    }

    public void update(VoucherResponse voucherResponse) {
        View view = getView();
        if (voucherResponse == null){
            if (view != null) {
                view.findViewById(R.id.tier_info_no_statements).setVisibility(View.VISIBLE);
            }
            return;
        }
        if (voucherResponse.tierInfo == null){
            if (view != null) {
                view.findViewById(R.id.tier_info_no_statements).setVisibility(View.VISIBLE);
            }
        } else {
            mVoucherResponse = voucherResponse;
            String currentStatus = voucherResponse.tierInfo.currentTier.toUpperCase();

            if (view != null) {
                view.findViewById(R.id.tier_info_no_statements).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.tier_info_status)).setText(currentStatus);
                TextView savedView = (TextView) view.findViewById(R.id.tier_info_saved);
                TextView sinceView = (TextView) view.findViewById(R.id.tier_info_since);
                if (currentStatus.equals(getString(R.string.valued))) {
                    view.findViewById(R.id.tier_info_image).setBackgroundResource(R.drawable.tier1);
                    savedView.setText(WFormatter.formatAmount(voucherResponse.tierInfo.earned));
                    try {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, WFormatter.formatDate(voucherResponse.tierInfo.earnedSince)), 1, getActivity()));
                    } catch (ParseException e) {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, voucherResponse.tierInfo.earnedSince), 1, getActivity()));
                    }
                    ((TextView) view.findViewById(R.id.credit_card_minimum_payment)).setText(WFormatter.formatAmount(voucherResponse.tierInfo.toSpend));
                    ((TextView) view.findViewById(R.id.tier_info_next_tier)).setText(R.string.get_loyal);
                } else if (currentStatus.equals(getString(R.string.loyal))) {
                    view.findViewById(R.id.tier_info_image).setBackgroundResource(R.drawable.tier2);
                    savedView.setText(WFormatter.formatAmount(voucherResponse.tierInfo.earned));
                    try {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, WFormatter.formatDate(voucherResponse.tierInfo.earnedSince)), 1, getActivity()));
                    } catch (ParseException e) {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, voucherResponse.tierInfo.earnedSince), 1, getActivity()));
                    }
                    ((TextView) view.findViewById(R.id.credit_card_minimum_payment)).setText(WFormatter.formatAmount(voucherResponse.tierInfo.toSpend));
                    ((TextView) view.findViewById(R.id.tier_info_next_tier)).setText(R.string.get_vip);
                } else {
                    view.findViewById(R.id.tier_info_non_vip).setVisibility(View.GONE);
                    view.findViewById(R.id.tier_info_non_vip_divider).setVisibility(View.GONE);
                    view.findViewById(R.id.tier_info_vip).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.tier_info_image).setBackgroundResource(R.drawable.tier3);
                    savedView.setText(WFormatter.formatAmount(voucherResponse.tierInfo.earned));
                    savedView.setGravity(Gravity.CENTER_HORIZONTAL);
                    sinceView.setGravity(Gravity.CENTER_HORIZONTAL);
                    ((TextView) view.findViewById(R.id.tier_info_saved_label)).setGravity(Gravity.CENTER_HORIZONTAL);
                    try {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, WFormatter.formatDate(voucherResponse.tierInfo.earnedSince)), 1, getActivity()));
                    } catch (ParseException e) {
                        sinceView.setText(FontHyperTextParser.getSpannable(getString(R.string.since, voucherResponse.tierInfo.earnedSince), 1, getActivity()));
                    }
                }
            }
        }
    }

    private WoolworthsApplication getWoolworthsApplication() {
        return (WoolworthsApplication) getActivity().getApplication();
    }
}

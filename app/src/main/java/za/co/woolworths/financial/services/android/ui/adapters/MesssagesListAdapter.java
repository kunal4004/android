package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageDetails;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class MesssagesListAdapter extends RecyclerSwipeAdapter<MesssagesListAdapter.SimpleViewHolder>{
    public Activity mContext;
    public List<MessageDetails> messageDetailsList;
    public MesssagesListAdapter(Activity mContext,List<MessageDetails> messageDetailsList)
    {
     this.mContext=mContext;
        this.messageDetailsList=messageDetailsList;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        WTextView txtTitle;
        WTextView txtDate;
        WTextView txtBody;
        ImageView imgdelete;
        RelativeLayout cardlayout;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            txtTitle=(WTextView)itemView.findViewById(R.id.msgTitle);
            txtDate=(WTextView)itemView.findViewById(R.id.date);
            txtBody=(WTextView)itemView.findViewById(R.id.bodyMessage);
            imgdelete=(ImageView)itemView.findViewById(R.id.msgDelete);
            cardlayout=(RelativeLayout)itemView.findViewById(R.id.cardLayout);

        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        viewHolder.txtTitle.setText(messageDetailsList.get(position).title);
        viewHolder.txtBody.setText(messageDetailsList.get(position).content);
        if(messageDetailsList.get(position).isRead)

             // viewHolder.cardlayout.getBackground().setAlpha(50);
            viewHolder.cardlayout.setBackgroundColor(Color.parseColor("#20000000"));
        else
            viewHolder.cardlayout.setBackgroundColor(Color.parseColor("#ffffff"));


        try{
            viewHolder.txtDate.setText(WFormatter.formatMessagingDate(messageDetailsList.get(position).createDate));

        }catch (ParseException e){

        }

        mItemManger.bindView(viewHolder.itemView, position);
        viewHolder.imgdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     deleteMessage(position,messageDetailsList.get(position).id);
            }
        });


    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_list_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return messageDetailsList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }


    public  void deleteMessage(final int msgPosition, final String id)
    {
        new HttpAsyncTask<String, String, DeleteMessageResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();


            }

            @Override
            protected DeleteMessageResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication)mContext.getApplication()).getApi().getDeleteMessagesResponse(id);
            }

            @Override
            protected Class<DeleteMessageResponse> httpDoInBackgroundReturnType() {
                return DeleteMessageResponse.class;
            }

            @Override
            protected DeleteMessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                DeleteMessageResponse deleteMessageResponse = new DeleteMessageResponse();
                deleteMessageResponse.response = new Response();

                return deleteMessageResponse;
            }

            @Override
            protected void onPostExecute(DeleteMessageResponse deleteMessageResponse) {
                super.onPostExecute(deleteMessageResponse);

               if(deleteMessageResponse.response.code.equals("-1"))
               {
                   messageDetailsList.remove(msgPosition);
                   notifyItemRemoved(msgPosition);
                   notifyItemRangeChanged(msgPosition,messageDetailsList.size());
                   notifyDataSetChanged();
                   mItemManger.closeAllItems();
               }


            }
        }.execute();

    }
}
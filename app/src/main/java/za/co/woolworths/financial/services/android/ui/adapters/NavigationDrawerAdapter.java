package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.NavListItem;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 17/11/2016.
 */

public class NavigationDrawerAdapter extends BaseAdapter {
    public Activity mContext;
    private List<NavListItem> navListItems;
    private int selectedPosition = Utils.DEFAULT_SELECTED_NAVIGATION_ITEM;

    public NavigationDrawerAdapter(Activity mContext, List<NavListItem> navListItems) {
        this.mContext = mContext;
        this.navListItems = navListItems;
    }

    @Override
    public int getCount() {
        return navListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = mContext.getLayoutInflater().inflate(R.layout.nav_drawer_row, null);

            mHolder.textInListView = (WTextView) convertView.findViewById(R.id.textView);
            mHolder.imageInListView = (ImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        NavListItem navItem = navListItems.get(position);
        try {

            mHolder.textInListView.setText(navItem.getName());
            mHolder.imageInListView.setImageResource(navItem.getImage());


        } catch (Exception ignored) {
        }

        if (position == selectedPosition) {
            convertView.setBackgroundResource(R.drawable.vertical_line);
        } else {
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        return convertView;
    }

    static class ViewHolder {
        WTextView textInListView;
        ImageView imageInListView;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }
}

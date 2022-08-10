package za.co.woolworths.financial.services.android.ui.views.maps.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.awfs.coordination.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;

    public GoogleMapWindowAdapter(Context context) {
        this.context = context;
    }

    // Hack to prevent info window from displaying: use a 0dp/0dp frame
    @Override
    public View getInfoWindow(Marker marker) {
        View v = ((Activity) context).getLayoutInflater().inflate(R.layout.no_window_info, null);
        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

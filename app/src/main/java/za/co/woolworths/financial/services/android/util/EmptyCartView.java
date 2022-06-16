package za.co.woolworths.financial.services.android.util;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

public class EmptyCartView implements View.OnClickListener {

    public interface EmptyCartInterface {
        void onEmptyCartRetry();
    }

    private EmptyCartInterface emptyCartInterface;
    private ImageView img_view;
    private Button btn_dash_set_address;
    private TextView txt_dash_sub_title;
    private TextView txt_dash_title;
    private int imageDrawable;
    private String description;
    private String title;


    public EmptyCartView(View view, EmptyCartInterface emptyCartInterface) {
        this.txt_dash_title = view.findViewById(R.id.txt_dash_title);
        this.txt_dash_sub_title = view.findViewById(R.id.txt_dash_sub_title);
        this.img_view = view.findViewById(R.id.img_view);
        this.btn_dash_set_address = view.findViewById(R.id.btn_dash_set_address);
        this.btn_dash_set_address.setOnClickListener(this);
        this.emptyCartInterface = emptyCartInterface;
    }

    public void setTitle(String title) {
        this.title = title;
        txt_dash_title.setText(TextUtils.isEmpty(this.title) ? "" : this.title);
    }

    public void setDescription(String description) {
        this.description = description;
        txt_dash_sub_title.setText(TextUtils.isEmpty(this.description) ? "" : this.description);
    }

    public void setImageUrl(int drawable) {
        this.imageDrawable = drawable;
        this.img_view.setImageResource(imageDrawable);
    }

    public void buttonVisibility(String text) {
        btn_dash_set_address.setText(text);
        btn_dash_set_address.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        MultiClickPreventer.preventMultiClick(view);
        switch (view.getId()) {
            case R.id.btnGoToProduct:
                emptyCartInterface.onEmptyCartRetry();
                break;

            default:
                break;
        }
    }

    public void setView(String title, String description, int drawable) {
        setTitle(title);
        setDescription(description);
        setImageUrl(drawable);
    }

    public void setView(String title, String description, String buttonText, int drawable) {
        setTitle(title);
        setDescription(description);
        buttonVisibility(buttonText);
        setImageUrl(drawable);
    }
}

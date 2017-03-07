package za.co.woolworths.financial.services.android.util.zxing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import com.awfs.coordination.R;
import com.pacific.mvc.ActivityView;

/**
 * Created by root on 16-5-8.
 */
public class QRView extends ActivityView<QRActivity> implements SurfaceHolder.Callback {
    private QRCodeView qrCodeView;
    private SurfaceView surfaceView;
    private ImageView snapImage;

    public QRView(QRActivity activity) {
        super(activity);
    }

    @Override
    protected void findView() {
        surfaceView = retrieveView(R.id.sv_preview);
        qrCodeView = retrieveView(R.id.qr_view);
        snapImage = retrieveView(R.id.snapImage);

    }

    @Override
    protected void setListener() {
        qrCodeView.setPickImageListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setHook(true);
                Intent galleryIntent = new Intent();
                if (Build.VERSION_CODES.KITKAT >= Build.VERSION.SDK_INT) {
                    galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                }
                galleryIntent.setType("image/*");
                Intent wrapperIntent = Intent.createChooser(galleryIntent, "选择二维码图片");
                activity.startIntentForResult(wrapperIntent, QRActivity.CODE_PICK_IMAGE, null);
            }
        });
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    protected void setAdapter() {

    }

    @Override
    protected void initialize() {

    }

    @Override
    public void onClick(View v) {

    }

    public void resultDialog(QRResult qrResult) {
//
//        snapImage.getLayoutParams().width = qrCodeView.getWidth();
//        snapImage.getLayoutParams().height = qrCodeView.getHeight();
//        snapImage.setImageBitmap(qrResult.getBitmap());

//        if (qrResult == null) {
//            new AlertDialog.Builder(activity)
//                    .setTitle("No Barcode Result")
//                    .setMessage("Can't decode barcode from target picture , \nplease confirm the picture has barcode value.")
//                    .setPositiveButton("Ok", null)
//                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            activity.setHook(false);
//                            activity.restartCapture();
//                        }
//                    })
//                    .create()
//                    .show();
//            return;
//        }
//        View view = activity.getLayoutInflater().inflate(R.layout.dialog_result, null);
//
//        if (qrResult.getBitmap() != null) {
//            ((ImageView) view.findViewById(R.id.img_barcode)).setImageBitmap(qrResult.getBitmap());
//        }
//        new AlertDialog.Builder(activity)
//                .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        activity.setHook(false);
//                        activity.restartCapture();
//                    }
//                })
//                .setView(view)
//                .create()
//                .show();
    }

    public void setEmptyViewVisible(boolean visible) {
        if (visible) {
            retrieveView(R.id.v_empty).setVisibility(View.VISIBLE);
        } else {
            retrieveView(R.id.v_empty).setVisibility(View.GONE);
        }
    }

    public void setSurfaceViewVisible(boolean visible) {
        if (visible) {
            surfaceView.setVisibility(View.VISIBLE);
        } else {
            surfaceView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        activity.onSurfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setEmptyViewVisible(true);
        activity.onSurfaceDestroyed();
    }
}

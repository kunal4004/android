package za.co.woolworths.financial.services.android.util.zxing;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.awfs.coordination.R;
import com.pacific.mvc.ActivityView;

public class QRView extends ActivityView<QRActivity> implements SurfaceHolder.Callback {
	private QRCodeView qrCodeView;
	private SurfaceView surfaceView;

	public QRView(QRActivity activity) {
		super(activity);
	}

	@Override
	protected void findView() {
		surfaceView = retrieveView(R.id.sv_preview);
		qrCodeView = retrieveView(R.id.qr_view);
	}

	@Override
	protected void setListener() {
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

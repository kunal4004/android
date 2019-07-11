package za.co.woolworths.financial.services.android.util.barcode;

import android.hardware.Camera;
import androidx.annotation.NonNull;

final class DecoderWrapper {
    private final Camera mCamera;
    private final Camera.CameraInfo mCameraInfo;
    private final Decoder mDecoder;
    private final Point mImageSize;
    private final Point mPreviewSize;
    private final Point mViewSize;
    private final int mDisplayOrientation;
    private final boolean mReverseHorizontal;
    private final boolean mAutoFocusSupported;
    private final boolean mFlashSupported;

    public DecoderWrapper(@NonNull Camera camera, @NonNull Camera.CameraInfo cameraInfo, @NonNull Decoder decoder,
						  @NonNull Point imageSize, @NonNull Point previewSize, @NonNull Point viewSize, int displayOrientation,
						  boolean autoFocusSupported, boolean flashSupported) {
        mCamera = camera;
        mCameraInfo = cameraInfo;
        mDecoder = decoder;
        mImageSize = imageSize;
        mPreviewSize = previewSize;
        mViewSize = viewSize;
        mDisplayOrientation = displayOrientation;
        mReverseHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mAutoFocusSupported = autoFocusSupported;
        mFlashSupported = flashSupported;
    }

    @NonNull
    public Camera getCamera() {
        return mCamera;
    }

    @NonNull
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    @NonNull
    public Decoder getDecoder() {
        return mDecoder;
    }

    @NonNull
    public Point getImageSize() {
        return mImageSize;
    }

    @NonNull
    public Point getPreviewSize() {
        return mPreviewSize;
    }

    @NonNull
    public Point getViewSize() {
        return mViewSize;
    }

    public int getDisplayOrientation() {
        return mDisplayOrientation;
    }

    public boolean shouldReverseHorizontal() {
        return mReverseHorizontal;
    }

    public boolean isAutoFocusSupported() {
        return mAutoFocusSupported;
    }

    public boolean isFlashSupported() {
        return mFlashSupported;
    }

    public void release() {
        mCamera.release();
        mDecoder.shutdown();
    }
}

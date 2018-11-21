package za.co.woolworths.financial.services.android.util.barcode;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;


import com.google.zxing.client.android.camera.CameraConfigurationUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class Utils {
    private static final float MIN_DISTORTION = 0.3f;
    private static final float MAX_DISTORTION = 1f;
    private static final float DISTORTION_STEP = 0.1f;
    private static final int MIN_PREVIEW_PIXELS = 589824;

    private Utils() {
    }

    public static void optimizeParameters(@NonNull Camera.Parameters parameters) {
        CameraConfigurationUtils.setBestPreviewFPS(parameters);
        CameraConfigurationUtils.setBarcodeSceneMode(parameters);
        CameraConfigurationUtils.setVideoStabilization(parameters);
        parameters.setPreviewFormat(ImageFormat.NV21);
    }

    @NonNull
    public static Point findSuitableImageSize(@NonNull Camera.Parameters parameters, int frameWidth, int frameHeight) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        if (sizes != null && !sizes.isEmpty()) {
            Collections.sort(sizes, new CameraSizeComparator());
            float frameRatio = (float) frameWidth / (float) frameHeight;
            for (float distortion = MIN_DISTORTION; distortion <= MAX_DISTORTION; distortion += DISTORTION_STEP) {
                for (Camera.Size size : sizes) {
                    int width = size.width;
                    int height = size.height;
                    if (width * height >= MIN_PREVIEW_PIXELS &&
                            Math.abs(frameRatio - (float) width / (float) height) <= distortion) {
                        return new Point(width, height);
                    }
                }
            }
        }
        Camera.Size defaultSize = parameters.getPreviewSize();
        if (defaultSize == null) {
            throw new CodeScannerException("Unable to configure camera preview size");
        }
        return new Point(defaultSize.width, defaultSize.height);
    }

    public static boolean disableAutoFocus(@NonNull Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes == null || focusModes.isEmpty()) {
            return false;
        }
        String focusMode = parameters.getFocusMode();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            if (Camera.Parameters.FOCUS_MODE_FIXED.equals(focusMode)) {
                return false;
            } else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                return true;
            }
        }
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            if (Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)) {
                return false;
            } else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                return true;
            }
        }
        return false;
    }

    public static boolean setAutoFocusMode(@NonNull Camera.Parameters parameters, AutoFocusMode autoFocusMode) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes == null || focusModes.isEmpty()) {
            return false;
        }
        if (autoFocusMode == AutoFocusMode.CONTINUOUS) {
            if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(parameters.getFocusMode())) {
                return false;
            }
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                return true;
            }
        }
        if (Camera.Parameters.FOCUS_MODE_AUTO.equals(parameters.getFocusMode())) {
            return false;
        }
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setFlashMode(@NonNull Camera.Parameters parameters, @NonNull String flashMode) {
        if (flashMode.equals(parameters.getFlashMode())) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(flashMode)) {
            parameters.setFlashMode(flashMode);
            return true;
        }
        return false;
    }

    public static int getDisplayOrientation(@NonNull Context context, @NonNull Camera.CameraInfo cameraInfo) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            throw new CodeScannerException("Unable to access window manager");
        }
        int degrees;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                if (rotation % 90 == 0) {
                    degrees = (360 + rotation) % 360;
                } else {
                    throw new CodeScannerException("Invalid display rotation");
                }
        }
        return ((cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? 180 : 360) + cameraInfo.orientation -
                degrees) % 360;
    }

    public static boolean isPortrait(int orientation) {
        return orientation == 90 || orientation == 270;
    }

    public static boolean isLaidOut(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return view.isLaidOut();
        } else {
            return view.getWidth() > 0 && view.getHeight() > 0;
        }
    }

    @NonNull
    public static Point getPreviewSize(int imageWidth, int imageHeight, int frameWidth, int frameHeight) {
        if (imageWidth == frameWidth && imageHeight == frameHeight) {
            return new Point(frameWidth, frameHeight);
        }
        int resultWidth = imageWidth * frameHeight / imageHeight;
        if (resultWidth < frameWidth) {
            return new Point(frameWidth, imageHeight * frameWidth / imageWidth);
        } else {
            return new Point(resultWidth, frameHeight);
        }
    }

    @NonNull
    public static Rect getImageFrameRect(int imageWidth, int imageHeight, @NonNull Rect viewFrameRect,
																		 @NonNull Point previewSize, @NonNull Point viewSize) {
        int previewWidth = previewSize.getX();
        int previewHeight = previewSize.getY();
        int viewWidth = viewSize.getX();
        int viewHeight = viewSize.getY();
        int wD = (previewWidth - viewWidth) / 2;
        int hD = (previewHeight - viewHeight) / 2;
        float wR = (float) imageWidth / (float) previewWidth;
        float hR = (float) imageHeight / (float) previewHeight;
        return new Rect(Math.max(Math.round((viewFrameRect.getLeft() + wD) * wR), 0),
                Math.max(Math.round((viewFrameRect.getTop() + hD) * hR), 0),
                Math.min(Math.round((viewFrameRect.getRight() + wD) * wR), imageWidth),
                Math.min(Math.round((viewFrameRect.getBottom() + hD) * hR), imageHeight));
    }

    public static byte[] rotateNV21(byte[] source, int width, int height, int rotation) {
        if (rotation == 0 || rotation == 360) {
            return source;
        }
        if (rotation % 90 != 0 || rotation < 0 || rotation > 270) {
            throw new IllegalArgumentException("Invalid rotation (0 <= rotation < 360, rotation % 90 == 0)");
        }
        byte[] output = new byte[source.length];
        int frameSize = width * height;
        boolean swap = rotation % 180 != 0;
        boolean flipX = rotation % 270 != 0;
        boolean flipY = rotation >= 180;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int yIn = j * width + i;
                int uIn = frameSize + (j >> 1) * width + (i & ~1);
                int vIn = uIn + 1;
                int wOut = swap ? height : width;
                int hOut = swap ? width : height;
                int iSwapped = swap ? j : i;
                int jSwapped = swap ? i : j;
                int iOut = flipX ? wOut - iSwapped - 1 : iSwapped;
                int jOut = flipY ? hOut - jSwapped - 1 : jSwapped;
                int yOut = jOut * wOut + iOut;
                int uOut = frameSize + (jOut >> 1) * wOut + (iOut & ~1);
                int vOut = uOut + 1;
                output[yOut] = (byte) (0xff & source[yIn]);
                output[uOut] = (byte) (0xff & source[uIn]);
                output[vOut] = (byte) (0xff & source[vIn]);
            }
        }
        return output;
    }

    public static final class SuppressErrorCallback implements ErrorCallback {
        @Override
        public void onError(@NonNull Exception error) {
            // Do nothing
        }
    }

    private static final class CameraSizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(@NonNull Camera.Size a, @NonNull Camera.Size b) {
            int aPixels = a.height * a.width;
            int bPixels = b.height * b.width;
            if (bPixels < aPixels) {
                return -1;
            } else if (bPixels > aPixels) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

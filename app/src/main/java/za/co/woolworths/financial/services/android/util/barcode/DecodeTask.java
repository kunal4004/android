package za.co.woolworths.financial.services.android.util.barcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

final class DecodeTask {
    private final byte[] mImage;
    private final Point mImageSize;
    private final Point mPreviewSize;
    private final Point mViewSize;
    private final Rect mViewFrameRect;
    private final int mOrientation;
    private final boolean mReverseHorizontal;

    public DecodeTask(@NonNull byte[] image, @NonNull Point imageSize, @NonNull Point previewSize,
					  @NonNull Point viewSize, @NonNull Rect viewFrameRect, int orientation, boolean reverseHorizontal) {
        mImage = image;
        mImageSize = imageSize;
        mPreviewSize = previewSize;
        mViewSize = viewSize;
        mViewFrameRect = viewFrameRect;
        mOrientation = orientation;
        mReverseHorizontal = reverseHorizontal;
    }

    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public Result decode(@NonNull MultiFormatReader reader) throws ReaderException {
        int imageWidth = mImageSize.getX();
        int imageHeight = mImageSize.getY();
        int orientation = mOrientation;
        byte[] image = Utils.rotateNV21(mImage, imageWidth, imageHeight, orientation);
        if (orientation == 90 || orientation == 270) {
            int width = imageWidth;
            imageWidth = imageHeight;
            imageHeight = width;
        }
        Rect frameRect = Utils.getImageFrameRect(imageWidth, imageHeight, mViewFrameRect, mPreviewSize, mViewSize);
        int frameWidth = frameRect.getWidth();
        int frameHeight = frameRect.getHeight();
        if (frameWidth < 1 || frameHeight < 1) {
            return null;
        }
        return reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(
                new PlanarYUVLuminanceSource(image, imageWidth, imageHeight, frameRect.getLeft(), frameRect.getTop(),
                        frameWidth, frameHeight, mReverseHorizontal))));
    }
}

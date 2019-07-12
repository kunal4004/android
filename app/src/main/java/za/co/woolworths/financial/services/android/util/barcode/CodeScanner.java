package za.co.woolworths.financial.services.android.util.barcode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Process;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.SurfaceHolder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.camera.CameraConfigurationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Code scanner.
 * Supports portrait and landscape screen orientations, back and front facing cameras,
 * auto focus and flash light control, viewfinder customization.
 *
 * @see CodeScanner#builder()
 * @see CodeScannerView
 * @see BarcodeFormat
 * barcodeFormat.add("EAN_8");
 * barcodeFormat.add("UPC_E");
 * barcodeFormat.add("UPC_A");
 * barcodeFormat.add("EAN_13");
 * barcodeFormat.add("ISBN_13");
 * barcodeFormat.add("CODE_128");
 */
public class CodeScanner {
	public static final List<BarcodeFormat> ALL_FORMATS = Arrays.asList(BarcodeFormat.values());
	public static final List<BarcodeFormat> ONE_DIMENSIONAL_FORMATS =
			Arrays.asList(BarcodeFormat.CODE_128,
					BarcodeFormat.EAN_8, BarcodeFormat.EAN_13,
					BarcodeFormat.UPC_A, BarcodeFormat.UPC_E);
	public static final List<BarcodeFormat> TWO_DIMENSIONAL_FORMATS =
			Arrays.asList(BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX, BarcodeFormat.MAXICODE, BarcodeFormat.PDF_417,
					BarcodeFormat.QR_CODE);
	private static final List<BarcodeFormat> DEFAULT_FORMATS = ALL_FORMATS;
	private static final ScanMode DEFAULT_SCAN_MODE = ScanMode.SINGLE;
	private static final AutoFocusMode DEFAULT_AUTO_FOCUS_MODE = AutoFocusMode.SAFE;
	private static final boolean DEFAULT_AUTO_FOCUS_ENABLED = true;
	private static final boolean DEFAULT_FLASH_ENABLED = false;
	private static final long DEFAULT_SAFE_AUTO_FOCUS_INTERVAL = 2000L;
	private static final int SAFE_AUTO_FOCUS_ATTEMPTS_THRESHOLD = 2;
	private static final int DEFAULT_CAMERA = -1;
	private final Lock mInitializeLock = new ReentrantLock();
	private final Context mContext;
	private final Handler mMainThreadHandler;
	private final CodeScannerView mScannerView;
	private final SurfaceHolder mSurfaceHolder;
	private final SurfaceHolder.Callback mSurfaceCallback;
	private final Camera.PreviewCallback mPreviewCallback;
	private final Camera.AutoFocusCallback mSafeAutoFocusCallback;
	private final Runnable mSafeAutoFocusTask;
	private final Runnable mStopPreviewTask;
	private final DecoderStateListener mDecoderStateListener;
	private volatile List<BarcodeFormat> mFormats = DEFAULT_FORMATS;
	private volatile ScanMode mScanMode = DEFAULT_SCAN_MODE;
	private volatile AutoFocusMode mAutoFocusMode = DEFAULT_AUTO_FOCUS_MODE;
	private volatile DecodeCallback mDecodeCallback;
	private volatile ErrorCallback mErrorCallback;
	private volatile DecoderWrapper mDecoderWrapper;
	private volatile boolean mInitialization;
	private volatile boolean mInitialized;
	private volatile boolean mStoppingPreview;
	private volatile boolean mAutoFocusEnabled = DEFAULT_AUTO_FOCUS_ENABLED;
	private volatile boolean mFlashEnabled = false;
	private volatile long mSafeAutoFocusInterval = DEFAULT_SAFE_AUTO_FOCUS_INTERVAL;
	private volatile int mCameraId = DEFAULT_CAMERA;
	private boolean mPreviewActive;
	private boolean mSafeAutoFocusing;
	private boolean mSafeAutoFocusTaskScheduled;
	private boolean mInitializationRequested;
	private int mSafeAutoFocusAttemptsCount;
	private int mViewWidth;
	private int mViewHeight;

	/**
	 * CodeScanner, associated with the first back-facing camera on the device
	 *
	 * @param context Context
	 * @param view    A view to display the preview
	 * @see CodeScannerView
	 */
	@MainThread
	public CodeScanner(@NonNull Context context, @NonNull CodeScannerView view) {
		mContext = context;
		mScannerView = view;
		mSurfaceHolder = view.getPreviewView().getHolder();
		mMainThreadHandler = new Handler();
		mSurfaceCallback = new SurfaceCallback();
		mPreviewCallback = new PreviewCallback();
		mSafeAutoFocusCallback = new SafeAutoFocusCallback();
		mSafeAutoFocusTask = new SafeAutoFocusTask();
		mStopPreviewTask = new StopPreviewTask();
		mDecoderStateListener = new DecoderStateListener();
		mScannerView.setCodeScanner(this);
		mScannerView.setLayoutListener(new ScannerLayoutListener());
	}

	/**
	 * CodeScanner, associated with particular hardware camera
	 *
	 * @param context  Context
	 * @param view     A view to display the preview
	 * @param cameraId Camera id (between {@code 0} and
	 *                 {@link Camera#getNumberOfCameras()} - {@code 1})
	 * @see CodeScannerView
	 */
	@MainThread
	public CodeScanner(@NonNull Context context, @NonNull CodeScannerView view, int cameraId) {
		this(context, view);
		mCameraId = cameraId;
	}

	/**
	 * Camera to use
	 *
	 * @param cameraId Camera id (between {@code 0} and
	 *                 {@link Camera#getNumberOfCameras()} - {@code 1})
	 */
	@MainThread
	public void setCamera(int cameraId) {
		mInitializeLock.lock();
		try {
			if (mCameraId != cameraId) {
				mCameraId = cameraId;
				if (mInitialized) {
					boolean previewActive = mPreviewActive;
					releaseResources();
					if (previewActive) {
						initialize();
					}
				}
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Formats, decoder to react to ({@link #ALL_FORMATS} by default)
	 *
	 * @param formats Formats
	 * @see BarcodeFormat
	 * @see #ALL_FORMATS
	 * @see #ONE_DIMENSIONAL_FORMATS
	 * @see #TWO_DIMENSIONAL_FORMATS
	 */
	@MainThread
	public void setFormats(@NonNull List<BarcodeFormat> formats) {
		mInitializeLock.lock();
		try {
			mFormats = formats;
			if (mInitialized) {
				mDecoderWrapper.getDecoder().setFormats(formats);
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Formats, decoder to react to ({@link #ALL_FORMATS} by default)
	 *
	 * @param formats Formats
	 * @see BarcodeFormat
	 * @see #ALL_FORMATS
	 * @see #ONE_DIMENSIONAL_FORMATS
	 * @see #TWO_DIMENSIONAL_FORMATS
	 */
	@MainThread
	public void setFormats(@NonNull BarcodeFormat... formats) {
		setFormats(Arrays.asList(formats));
	}

	/**
	 * Format, decoder to react to
	 *
	 * @param format Format
	 * @see BarcodeFormat
	 */
	@MainThread
	public void setFormat(@NonNull BarcodeFormat format) {
		setFormats(Collections.singletonList(format));
	}

	/**
	 * Callback of decoding process
	 *
	 * @param decodeCallback Callback
	 * @see DecodeCallback
	 */
	public void setDecodeCallback(@Nullable DecodeCallback decodeCallback) {
		mInitializeLock.lock();
		try {
			mDecodeCallback = decodeCallback;
			if (mInitialized) {
				mDecoderWrapper.getDecoder().setCallback(decodeCallback);
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Camera initialization error callback.
	 * If not set, an exception will be thrown when error will occur.
	 *
	 * @param errorCallback Callback
	 * @see ErrorCallback#SUPPRESS
	 * @see ErrorCallback
	 */
	public void setErrorCallback(@Nullable ErrorCallback errorCallback) {
		mErrorCallback = errorCallback;
	}

	/**
	 * Scan mode, {@link ScanMode#SINGLE} by default
	 *
	 * @see ScanMode
	 */
	public void setScanMode(@NonNull ScanMode scanMode) {
		mScanMode = scanMode;
	}

	/**
	 * Whether to enable or disable auto focus if it's supported, {@code true} by default
	 */
	@MainThread
	public void setAutoFocusEnabled(boolean autoFocusEnabled) {
		mInitializeLock.lock();
		try {
			boolean changed = mAutoFocusEnabled != autoFocusEnabled;
			mAutoFocusEnabled = autoFocusEnabled;
			if (mInitialized && mPreviewActive && changed && mDecoderWrapper.isAutoFocusSupported()) {
				setAutoFocusEnabledInternal(autoFocusEnabled);
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Whether if auto focus is currently enabled
	 */
	public boolean isAutoFocusEnabled() {
		return mAutoFocusEnabled;
	}

	/**
	 * Auto focus mode, {@link AutoFocusMode#SAFE} by default
	 *
	 * @see AutoFocusMode
	 */
	@MainThread
	public void setAutoFocusMode(@NonNull AutoFocusMode autoFocusMode) {
		mInitializeLock.lock();
		try {
			mAutoFocusMode = autoFocusMode;
			if (mInitialized && mAutoFocusEnabled) {
				setAutoFocusEnabledInternal(true);
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Auto focus interval in milliseconds for {@link AutoFocusMode#SAFE} mode, 2000 by default
	 *
	 * @see #setAutoFocusMode(AutoFocusMode)
	 */
	public void setAutoFocusInterval(long autoFocusInterval) {
		mSafeAutoFocusInterval = autoFocusInterval;
	}

	/**
	 * Whether to enable or disable flash light if it's supported, {@code false} by default
	 */
	@MainThread
	public void setFlashEnabled(boolean flashEnabled) {
		mInitializeLock.lock();
		try {
			boolean changed = false;
			flashEnabled = false;
			mFlashEnabled = false;
			//mScannerView.setFlashEnabled(flashEnabled);
			if (mInitialized && mPreviewActive && changed && mDecoderWrapper.isFlashSupported()) {
				setFlashEnabledInternal(flashEnabled);
			}
		} finally {
			mInitializeLock.unlock();
		}
	}

	/**
	 * Whether if flash light is currently enabled
	 */
	public boolean isFlashEnabled() {
		return mFlashEnabled;
	}

	/**
	 * Whether if preview is active
	 */
	public boolean isPreviewActive() {
		return mPreviewActive;
	}

	/**
	 * Start camera preview
	 * <br>
	 * Requires {@link Manifest.permission#CAMERA} permission
	 */
	@MainThread
	public void startPreview() {
		mInitializeLock.lock();
		try {
			if (!mInitialized && !mInitialization) {
				initialize();
				return;
			}
		} finally {
			mInitializeLock.unlock();
		}
		if (!mPreviewActive) {
			mSurfaceHolder.addCallback(mSurfaceCallback);
			startPreviewInternal(false);
		}
	}

	/**
	 * Stop camera preview
	 */
	@MainThread
	public void stopPreview() {
		if (mInitialized && mPreviewActive) {
			mSurfaceHolder.removeCallback(mSurfaceCallback);
			stopPreviewInternal(false);
		}
	}

	/**
	 * Release resources, and stop preview if needed; call this method in {@link Activity#onPause()}
	 */
	@MainThread
	public void releaseResources() {
		if (mInitialized) {
			if (mPreviewActive) {
				stopPreview();
			}
			releaseResourcesInternal();
		}
	}

	private void initialize() {
		initialize(mScannerView.getWidth(), mScannerView.getHeight());
	}

	private void initialize(int width, int height) {
		mViewWidth = width;
		mViewHeight = height;
		if (width > 0 && height > 0) {
			mInitialization = true;
			mInitializationRequested = false;
			new InitializationThread(width, height).start();
		} else {
			mInitializationRequested = true;
		}
	}

	private void startPreviewInternal(boolean internal) {
		try {
			DecoderWrapper decoderWrapper = mDecoderWrapper;
			Camera camera = decoderWrapper.getCamera();
			camera.setPreviewCallback(mPreviewCallback);
			camera.setPreviewDisplay(mSurfaceHolder);
			if (!internal && decoderWrapper.isFlashSupported() && mFlashEnabled) {
				setFlashEnabledInternal(false);
			}
			camera.startPreview();
			mStoppingPreview = false;
			mPreviewActive = true;
			mSafeAutoFocusing = false;
			mSafeAutoFocusAttemptsCount = 0;
			if (mAutoFocusMode == AutoFocusMode.SAFE) {
				scheduleSafeAutoFocusTask();
			}
		} catch (Exception ignored) {
		}
	}

	private void startPreviewInternalSafe() {
		if (mInitialized && !mPreviewActive) {
			startPreviewInternal(true);
		}
	}

	private void stopPreviewInternal(boolean internal) {
		try {
			DecoderWrapper decoderWrapper = mDecoderWrapper;
			Camera camera = decoderWrapper.getCamera();
			if (!internal && decoderWrapper.isFlashSupported() && mFlashEnabled) {
				Camera.Parameters parameters = camera.getParameters();
				if (parameters != null && Utils.setFlashMode(parameters, Camera.Parameters.FLASH_MODE_OFF)) {
					camera.setParameters(parameters);
				}
			}
			camera.setPreviewCallback(null);
			camera.stopPreview();
		} catch (Exception ignored) {
		}
		mStoppingPreview = false;
		mPreviewActive = false;
		mSafeAutoFocusing = false;
		mSafeAutoFocusAttemptsCount = 0;
	}

	private void stopPreviewInternalSafe() {
		if (mInitialized && mPreviewActive) {
			stopPreviewInternal(true);
		}
	}

	private void releaseResourcesInternal() {
		mInitialized = false;
		mInitialization = false;
		mStoppingPreview = false;
		mPreviewActive = false;
		mSafeAutoFocusing = false;
		DecoderWrapper decoderWrapper = mDecoderWrapper;
		if (decoderWrapper != null) {
			mDecoderWrapper = null;
			decoderWrapper.release();
		}
	}

	private void setFlashEnabledInternal(boolean flashEnabled) {
		try {
			DecoderWrapper decoderWrapper = mDecoderWrapper;
			Camera camera = decoderWrapper.getCamera();
			Camera.Parameters parameters = camera.getParameters();
			if (parameters == null) {
				return;
			}
			//   boolean changed;
//            if (flashEnabled) {
//                changed = Utils.setFlashMode(parameters, Camera.Parameters.FLASH_MODE_OFF);
//            } else {
//                changed = Utils.setFlashMode(parameters, Camera.Parameters.FLASH_MODE_OFF);
//            }
//            if (changed) {
//                CameraConfigurationUtils.setBestExposure(parameters, false);
//                camera.setParameters(parameters);
//            }
		} catch (Exception ignored) {
		}
	}

	private void setAutoFocusEnabledInternal(boolean autoFocusEnabled) {
		try {
			Camera camera = mDecoderWrapper.getCamera();
			Camera.Parameters parameters = camera.getParameters();
			if (parameters == null) {
				return;
			}
			boolean changed;
			AutoFocusMode autoFocusMode = mAutoFocusMode;
			if (autoFocusEnabled) {
				changed = Utils.setAutoFocusMode(parameters, autoFocusMode);
			} else {
				camera.cancelAutoFocus();
				changed = Utils.disableAutoFocus(parameters);
			}
			if (changed) {
				camera.setParameters(parameters);
			}
			if (autoFocusEnabled) {
				mSafeAutoFocusAttemptsCount = 0;
				mSafeAutoFocusing = false;
				if (autoFocusMode == AutoFocusMode.SAFE) {
					scheduleSafeAutoFocusTask();
				}
			}
		} catch (Exception ignored) {
		}
	}

	private void safeAutoFocusCamera() {
		if (!mInitialized || !mPreviewActive) {
			return;
		}
		if (!mDecoderWrapper.isAutoFocusSupported() || !mAutoFocusEnabled) {
			return;
		}
		if (mSafeAutoFocusing && mSafeAutoFocusAttemptsCount < SAFE_AUTO_FOCUS_ATTEMPTS_THRESHOLD) {
			mSafeAutoFocusAttemptsCount++;
		} else {
			try {
				Camera camera = mDecoderWrapper.getCamera();
				camera.cancelAutoFocus();
				camera.autoFocus(mSafeAutoFocusCallback);
				mSafeAutoFocusAttemptsCount = 0;
				mSafeAutoFocusing = true;
			} catch (Exception e) {
				mSafeAutoFocusing = false;
			}
		}
		scheduleSafeAutoFocusTask();
	}

	private void scheduleSafeAutoFocusTask() {
		if (mSafeAutoFocusTaskScheduled) {
			return;
		}
		mSafeAutoFocusTaskScheduled = true;
		mMainThreadHandler.postDelayed(mSafeAutoFocusTask, mSafeAutoFocusInterval);
	}

	boolean isAutoFocusSupportedOrUnknown() {
		DecoderWrapper wrapper = mDecoderWrapper;
		return wrapper == null || wrapper.isAutoFocusSupported();
	}

	boolean isFlashSupportedOrUnknown() {
		DecoderWrapper wrapper = mDecoderWrapper;
		return wrapper == null || wrapper.isFlashSupported();
	}

	private final class ScannerLayoutListener implements CodeScannerView.LayoutListener {
		@Override
		public void onLayout(int width, int height) {
			mInitializeLock.lock();
			try {
				if (width != mViewWidth || height != mViewHeight) {
					boolean previewActive = mPreviewActive;
					if (mInitialized) {
						releaseResources();
					}
					if (previewActive || mInitializationRequested) {
						initialize(width, height);
					}
				}
			} finally {
				mInitializeLock.unlock();
			}
		}
	}

	private final class PreviewCallback implements Camera.PreviewCallback {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!mInitialized || mStoppingPreview || mScanMode == ScanMode.PREVIEW || data == null) {
				return;
			}
			DecoderWrapper decoderWrapper = mDecoderWrapper;
			if (decoderWrapper == null) {
				return;
			}
			Decoder decoder = decoderWrapper.getDecoder();
			if (decoder.getState() != Decoder.State.IDLE) {
				return;
			}
			Rect frameRect = mScannerView.getFrameRect();
			if (frameRect == null || frameRect.getWidth() < 1 || frameRect.getHeight() < 1) {
				return;
			}
			decoder.decode(new DecodeTask(data, decoderWrapper.getImageSize(), decoderWrapper.getPreviewSize(),
					decoderWrapper.getViewSize(), frameRect, decoderWrapper.getDisplayOrientation(),
					decoderWrapper.shouldReverseHorizontal()));
		}
	}

	private final class SurfaceCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			startPreviewInternalSafe();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (holder.getSurface() == null) {
				mPreviewActive = false;
				return;
			}
			stopPreviewInternalSafe();
			startPreviewInternalSafe();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			stopPreviewInternalSafe();
		}
	}

	private final class DecoderStateListener implements Decoder.StateListener {
		@Override
		public boolean onStateChanged(@NonNull Decoder.State state) {
			if (state == Decoder.State.DECODED) {
				ScanMode scanMode = mScanMode;
				if (scanMode == ScanMode.PREVIEW) {
					return false;
				} else if (scanMode == ScanMode.SINGLE) {
					mStoppingPreview = true;
					mMainThreadHandler.post(mStopPreviewTask);
				}
			}
			return true;
		}
	}

	private final class InitializationThread extends Thread {
		private final int mWidth;
		private final int mHeight;

		public InitializationThread(int width, int height) {
			super("cs-init");
			mWidth = width;
			mHeight = height;
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			try {
				initialize();
			} catch (Exception e) {
				releaseResourcesInternal();
				ErrorCallback errorCallback = mErrorCallback;
				if (errorCallback != null) {
					errorCallback.onError(e);
				} else {
					throw e;
				}
			}
		}

		private void initialize() {
			Camera camera = null;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			int cameraId = mCameraId;
			if (cameraId == DEFAULT_CAMERA) {
				int numberOfCameras = Camera.getNumberOfCameras();
				for (int i = 0; i < numberOfCameras; i++) {
					Camera.getCameraInfo(i, cameraInfo);
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						camera = Camera.open(i);
						mCameraId = i;
						break;
					}
				}
			} else {
				camera = Camera.open(cameraId);
				Camera.getCameraInfo(cameraId, cameraInfo);
			}
			if (camera == null) {
				throw new CodeScannerException("Unable to access camera");
			}
			Camera.Parameters parameters = camera.getParameters();
			if (parameters == null) {
				throw new CodeScannerException("Unable to configure camera");
			}
			int orientation = Utils.getDisplayOrientation(mContext, cameraInfo);
			boolean portrait = Utils.isPortrait(orientation);
			Point imageSize =
					Utils.findSuitableImageSize(parameters, portrait ? mHeight : mWidth, portrait ? mWidth : mHeight);
			int imageWidth = imageSize.getX();
			int imageHeight = imageSize.getY();
			parameters.setPreviewSize(imageWidth, imageHeight);
			Point previewSize =
					Utils.getPreviewSize(portrait ? imageHeight : imageWidth, portrait ? imageWidth : imageHeight,
							mWidth, mHeight);
			List<String> focusModes = parameters.getSupportedFocusModes();
			boolean autoFocusSupported = focusModes != null &&
					(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ||
							focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE));
			if (!autoFocusSupported) {
				mAutoFocusEnabled = false;
			}
			if (autoFocusSupported && mAutoFocusEnabled) {
				Utils.setAutoFocusMode(parameters, mAutoFocusMode);
			}
			List<String> flashModes = parameters.getSupportedFlashModes();
			boolean flashSupported = flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
			if (!flashSupported) {
				mFlashEnabled = false;
			}
			Utils.optimizeParameters(parameters);
			CameraConfigurationUtils.setBestExposure(parameters, mFlashEnabled);
			camera.setParameters(parameters);
			camera.setDisplayOrientation(orientation);
			mInitializeLock.lock();
			try {
				Decoder decoder = new Decoder(mDecoderStateListener, mFormats, mDecodeCallback);
				mDecoderWrapper = new DecoderWrapper(camera, cameraInfo, decoder, imageSize, previewSize,
						new Point(mWidth, mHeight), orientation, autoFocusSupported, flashSupported);
				decoder.start();
				mInitialization = false;
				mInitialized = true;
			} finally {
				mInitializeLock.unlock();
			}
			mMainThreadHandler.post(new FinishInitializationTask(previewSize));
		}
	}

	private final class SafeAutoFocusCallback implements Camera.AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			mSafeAutoFocusing = false;
		}
	}

	private final class SafeAutoFocusTask implements Runnable {
		@Override
		public void run() {
			mSafeAutoFocusTaskScheduled = false;
			if (mAutoFocusMode == AutoFocusMode.SAFE) {
				safeAutoFocusCamera();
			}
		}
	}

	private final class StopPreviewTask implements Runnable {
		@Override
		public void run() {
			stopPreview();
		}
	}

	private final class FinishInitializationTask implements Runnable {
		private final Point mPreviewSize;

		private FinishInitializationTask(@NonNull Point previewSize) {
			mPreviewSize = previewSize;
		}

		@Override
		public void run() {
			if (!mInitialized) {
				return;
			}
			mScannerView.setPreviewSize(mPreviewSize);
			startPreview();
		}
	}

	/**
	 * New builder instance. Use it to pre-configure scanner. Note that all parameters
	 * also can be changed after scanner created and when preview is active.
	 * <p>
	 * Call {@link Builder#build(Context, CodeScannerView)} to create
	 * scanner instance with specified parameters.
	 */
	@NonNull
	@MainThread
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Code scanner builder
	 */
	public static final class Builder {
		private int mCameraId = DEFAULT_CAMERA;
		private List<BarcodeFormat> mFormats = DEFAULT_FORMATS;
		private DecodeCallback mDecodeCallback;
		private ErrorCallback mErrorCallback;
		private boolean mAutoFocusEnabled = DEFAULT_AUTO_FOCUS_ENABLED;
		private ScanMode mScanMode = DEFAULT_SCAN_MODE;
		private AutoFocusMode mAutoFocusMode = DEFAULT_AUTO_FOCUS_MODE;
		private long mAutoFocusInterval = DEFAULT_SAFE_AUTO_FOCUS_INTERVAL;
		private boolean mFlashEnabled = DEFAULT_FLASH_ENABLED;

		private Builder() {
		}

		/**
		 * Camera that will be used by scanner.
		 * First back-facing camera on the device by default.
		 *
		 * @param cameraId Camera id (between {@code 0} and
		 *                 {@link Camera#getNumberOfCameras()} - {@code 1})
		 */
		@NonNull
		@MainThread
		public Builder camera(int cameraId) {
			mCameraId = cameraId;
			return this;
		}

		/**
		 * Formats, decoder to react to ({@link #ALL_FORMATS} by default)
		 *
		 * @param formats Formats
		 * @see BarcodeFormat
		 * @see #ALL_FORMATS
		 * @see #ONE_DIMENSIONAL_FORMATS
		 * @see #TWO_DIMENSIONAL_FORMATS
		 */
		@NonNull
		@MainThread
		public Builder formats(@NonNull List<BarcodeFormat> formats) {
			mFormats = formats;
			return this;
		}

		/**
		 * Formats, decoder to react to ({@link #ALL_FORMATS} by default)
		 *
		 * @param formats Formats
		 * @see BarcodeFormat
		 * @see #ALL_FORMATS
		 * @see #ONE_DIMENSIONAL_FORMATS
		 * @see #TWO_DIMENSIONAL_FORMATS
		 */
		@NonNull
		@MainThread
		public Builder formats(@NonNull BarcodeFormat... formats) {
			mFormats = Arrays.asList(formats);
			return this;
		}

		/**
		 * Format, decoder to react to
		 *
		 * @param format Format
		 * @see BarcodeFormat
		 */
		@NonNull
		@MainThread
		public Builder format(@NonNull BarcodeFormat format) {
			mFormats = Collections.singletonList(format);
			return this;
		}

		/**
		 * Callback of decoding process
		 *
		 * @param callback Callback
		 * @see DecodeCallback
		 */
		@NonNull
		@MainThread
		public Builder onDecoded(@Nullable DecodeCallback callback) {
			mDecodeCallback = callback;
			return this;
		}

		/**
		 * Camera initialization error callback.
		 * If not set, an exception will be thrown when error will occur.
		 *
		 * @param callback Callback
		 * @see ErrorCallback#SUPPRESS
		 * @see ErrorCallback
		 */
		@NonNull
		@MainThread
		public Builder onError(@Nullable ErrorCallback callback) {
			mErrorCallback = callback;
			return this;
		}

		/**
		 * Scan mode, {@link ScanMode#SINGLE} by default
		 *
		 * @see ScanMode
		 */
		@NonNull
		@MainThread
		public Builder scanMode(@NonNull ScanMode mode) {
			mScanMode = mode;
			return this;
		}

		/**
		 * Whether to enable or disable auto focus if it's supported, {@code true} by default
		 */
		@NonNull
		@MainThread
		public Builder autoFocus(boolean enabled) {
			mAutoFocusEnabled = enabled;
			return this;
		}

		/**
		 * Set auto focus mode, {@link AutoFocusMode#SAFE} by default
		 *
		 * @see AutoFocusMode
		 */
		@NonNull
		@MainThread
		public Builder autoFocusMode(@NonNull AutoFocusMode mode) {
			mAutoFocusMode = mode;
			return this;
		}

		/**
		 * Set auto focus interval in milliseconds for {@link AutoFocusMode#SAFE} mode,
		 * 2000 by default
		 *
		 * @see #autoFocusMode(AutoFocusMode)
		 */
		@NonNull
		@MainThread
		public Builder autoFocusInterval(long interval) {
			mAutoFocusInterval = interval;
			return this;
		}

		/**
		 * Whether to enable or disable flash light if it's supported, {@code false} by default
		 */
		@NonNull
		@MainThread
		public Builder flash(boolean enabled) {
			mFlashEnabled = false;
			return this;
		}

		/**
		 * Create new {@link CodeScanner} instance with specified parameters
		 *
		 * @param context Context
		 * @param view    A view to display the preview
		 * @see CodeScannerView
		 */
		@NonNull
		@MainThread
		public CodeScanner build(@NonNull Context context, @NonNull CodeScannerView view) {
			CodeScanner scanner = new CodeScanner(context, view);
			scanner.mCameraId = mCameraId;
			scanner.mFormats = mFormats;
			scanner.mDecodeCallback = mDecodeCallback;
			scanner.mErrorCallback = mErrorCallback;
			scanner.mAutoFocusEnabled = mAutoFocusEnabled;
			scanner.mSafeAutoFocusInterval = mAutoFocusInterval;
			scanner.mScanMode = mScanMode;
			scanner.mAutoFocusMode = mAutoFocusMode;
			scanner.mFlashEnabled = false;
			return scanner;
		}
	}
}

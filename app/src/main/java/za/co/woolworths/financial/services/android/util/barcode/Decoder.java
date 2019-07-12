package za.co.woolworths.financial.services.android.util.barcode;

import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

final class Decoder {
    private final BlockingQueue<DecodeTask> mDecodeQueue = new SynchronousQueue<>();
    private final MultiFormatReader mReader;
    private final DecoderThread mDecoderThread;
    private final StateListener mStateListener;
    private final Map<DecodeHintType, Object> mHints;
    private volatile DecodeCallback mCallback;
    private volatile State mState;

    public Decoder(@NonNull StateListener stateListener, @NonNull List<BarcodeFormat> formats,
            @Nullable DecodeCallback callback) {
        mReader = new MultiFormatReader();
        mDecoderThread = new DecoderThread();
        mHints = new EnumMap<>(DecodeHintType.class);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        mReader.setHints(mHints);
        mCallback = callback;
        mStateListener = stateListener;
        mState = State.INITIALIZED;
    }

    public void setFormats(@NonNull List<BarcodeFormat> formats) {
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        mReader.setHints(mHints);
    }

    public void setCallback(@Nullable DecodeCallback callback) {
        mCallback = callback;
    }

    public void decode(@NonNull DecodeTask task) {
        mDecodeQueue.offer(task);
    }

    public void start() {
        if (mState != State.INITIALIZED) {
            throw new IllegalStateException("Illegal decoder state");
        }
        mDecoderThread.start();
    }

    public void shutdown() {
        mDecoderThread.interrupt();
        mDecodeQueue.clear();
    }

    @NonNull
    public State getState() {
        return mState;
    }

    private boolean setState(@NonNull State state) {
        mState = state;
        return mStateListener.onStateChanged(state);
    }

    private final class DecoderThread extends Thread {
        public DecoderThread() {
            super("cs-decoder");
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            for (; ; ) {
                try {
                    setState(Decoder.State.IDLE);
                    Result result = null;
                    try {
                        DecodeTask task = mDecodeQueue.take();
                        setState(Decoder.State.DECODING);
                        result = task.decode(mReader);
                    } catch (ReaderException ignored) {
                    } finally {
                        if (result != null) {
                            mDecodeQueue.clear();
                            if (setState(Decoder.State.DECODED)) {
                                DecodeCallback callback = mCallback;
                                if (callback != null) {
                                    callback.onDecoded(result);
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    setState(Decoder.State.STOPPED);
                    break;
                }
            }
        }
    }

    public interface StateListener {
        boolean onStateChanged(@NonNull State state);
    }

    public enum State {
        INITIALIZED,
        IDLE,
        DECODING,
        DECODED,
        STOPPED
    }
}

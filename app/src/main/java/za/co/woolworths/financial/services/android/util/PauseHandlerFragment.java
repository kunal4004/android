package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.Vector;

public class PauseHandlerFragment extends Fragment {

    private AppCompatActivity context;
    private boolean isPaused = true;
    private Vector<MyRunnable> buffer = new Vector<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (AppCompatActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        isPaused = false;
        playback();
        super.onResume();
    }

    private void playback() {
        while (buffer.size() > 0) {
            final MyRunnable runnable = buffer.elementAt(0);
            buffer.removeElementAt(0);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //execute run block, providing new context, incase
                    //Android re-creates the parent activity
                    runnable.run(context);
                }
            });
        }
    }

    public final void runProtected(final MyRunnable runnable) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    buffer.add(runnable);
                } else {
                    runnable.run(context);
                }
            }
        });
    }
}
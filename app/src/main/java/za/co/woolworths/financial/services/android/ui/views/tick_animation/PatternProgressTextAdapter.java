package za.co.woolworths.financial.services.android.ui.views.tick_animation;

import android.support.annotation.NonNull;

public final class PatternProgressTextAdapter implements CircularProgressIndicator.ProgressTextAdapter {

    private String pattern;

    public PatternProgressTextAdapter(String pattern) {
        this.pattern = pattern;
    }

    @NonNull
    @Override
    public String formatText(double currentProgress) {
        return String.format(pattern, currentProgress);
    }
}

package za.co.woolworths.financial.services.android.ui.views.tick_animation;


public final class DefaultProgressTextAdapter implements CircularProgressIndicator.ProgressTextAdapter {

    @Override
    public String formatText(double currentProgress) {
        return String.valueOf((int) currentProgress);
    }

}
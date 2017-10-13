package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class YoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

	private static final int RECOVERY_REQUEST = 1;
	private YouTubePlayerView youTubeView;
	private MyPlayerStateChangeListener playerStateChangeListener;
	private MyPlaybackEventListener playbackEventListener;
	private YouTubePlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_api_player);

		youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		youTubeView.initialize(IncreaseLimitController.YOUTUBE_API_KEY, this);

		playerStateChangeListener = new MyPlayerStateChangeListener();
		playbackEventListener = new MyPlaybackEventListener();

//		final EditText seekToText = (EditText) findViewById(R.id.seek_to_text);
//		Button seekToButton = (Button) findViewById(R.id.seek_to_button);
//		seekToButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				int skipToSecs = Integer.valueOf(seekToText.getText().toString());
//				player.seekToMillis(skipToSecs * 1000);
//			}
//		});
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
		this.player = player;
		player.setPlayerStateChangeListener(playerStateChangeListener);
		player.setPlaybackEventListener(playbackEventListener);

		if (!wasRestored) {
			player.loadVideo("kbgu50dKXrM");
		}

	}

	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
		if (errorReason.isUserRecoverableError()) {
		} else {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RECOVERY_REQUEST) {
			// Retry initialization if user performed a recovery action
			getYouTubePlayerProvider().initialize(IncreaseLimitController.YOUTUBE_API_KEY, this);
		}
	}

	protected Provider getYouTubePlayerProvider() {
		return youTubeView;
	}

	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

		@Override
		public void onPlaying() {
			// Called when playback starts, either due to user action or call to play().
			showMessage("Playing");
		}

		@Override
		public void onPaused() {
			// Called when playback is paused, either due to user action or call to pause().
			showMessage("Paused");
		}

		@Override
		public void onStopped() {
			// Called when playback stops for a reason other than being paused.
			showMessage("Stopped");
		}

		@Override
		public void onBuffering(boolean b) {
			// Called when buffering starts or ends.
		}

		@Override
		public void onSeekTo(int i) {
			// Called when a jump in playback position occurs, either
			// due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
		}
	}

	private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

		@Override
		public void onLoading() {
			// Called when the player is loading a video
			// At this point, it's not ready to accept commands affecting playback such as play() or pause()
		}

		@Override
		public void onLoaded(String s) {
			// Called when a video is done loading.
			// Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
		}

		@Override
		public void onAdStarted() {
			// Called when playback of an advertisement starts.
		}

		@Override
		public void onVideoStarted() {
			// Called when playback of the video starts.
		}

		@Override
		public void onVideoEnded() {
			// Called when the video reaches its end.
		}

		@Override
		public void onError(YouTubePlayer.ErrorReason errorReason) {
			// Called when an error occurs.
		}
	}
}

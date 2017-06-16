package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 04/06/2017.
 */

public class WGlobalState {

	private boolean cardGestureIsEnabled;

	// Account state
	private boolean accountSignInState;
	private boolean rewardSignInState;
	private boolean onBackPressed;

	public boolean cardGestureIsEnabled() {
		return cardGestureIsEnabled;
	}

	public void setCardGestureIsEnabled(boolean pCardGestureIsEnabled) {
		cardGestureIsEnabled = pCardGestureIsEnabled;
	}

	public boolean getAccountSignInState() {
		return accountSignInState;
	}

	public void setAccountSignInState(boolean pAccountSignInState) {
		accountSignInState = pAccountSignInState;
	}

	public boolean getRewardSignInState() {
		return rewardSignInState;
	}

	public void setRewardSignInState(boolean pRewardSignInState) {
		rewardSignInState = pRewardSignInState;
	}

	public boolean getOnBackPressed() {
		return onBackPressed;
	}

	public void setOnBackPressed(boolean pOnBackPressed) {
		onBackPressed = pOnBackPressed;
	}
}

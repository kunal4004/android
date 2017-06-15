package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 04/06/2017.
 */

public class WGlobalState {

	private boolean cardGestureIsEnabled;
	private boolean accountSignInState;
	private boolean rewardSignInState;

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
}

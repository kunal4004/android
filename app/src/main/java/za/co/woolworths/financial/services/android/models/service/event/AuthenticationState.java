package za.co.woolworths.financial.services.android.models.service.event;



import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AuthenticationState {

	private final int authStateTypeDef;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({SIGN_IN, SIGN_OUT, SESSION_TIME_OUT})
	public @interface AuthStateTypeDef {
	}

	public static final int SIGN_IN = 0;
	public static final int SIGN_OUT = 1;
	public static final int SESSION_TIME_OUT = 2;

	public AuthenticationState(@AuthStateTypeDef int authStateTypeDef) {
		this.authStateTypeDef = authStateTypeDef;
	}

	public int getAuthStateTypeDef() {
		return authStateTypeDef;
	}
}

package za.co.woolworths.financial.services.android.util;

/**
 * Created by W7099877 on 2017/10/30.
 */

public enum CardType {
	WREWARDS("WRewards Card"),
	MYSCHOOL("MySchool");

	private final String cardType;

	 CardType(String type)
	{
		this.cardType=type;
	}

	public String getType()
	{
		return cardType;
	}

}

package za.co.woolworths.financial.services.android.models.dto;


public class Statement {

	private String month;
	private boolean selectedByUser;

	public Statement(String month, boolean selectedByUser) {
		this.month = month;
	}

	public String getMonth() {
		return month;
	}

	public void setSelectedByUser(boolean value) {
		this.selectedByUser = value;
	}

	public boolean selectedByUser() {
		return selectedByUser;
	}

}

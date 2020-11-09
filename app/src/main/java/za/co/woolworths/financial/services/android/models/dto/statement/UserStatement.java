package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserStatement {

	/*
		Request body params
	 */
	private String productOfferingId;
	private String accountNumber;
	private String startDate;
	private String endDate;

	/*
		Response body params
	 */
	@SerializedName("docType")
	@Expose
	public String docType;
	@SerializedName("docId")
	@Expose
	public String docId;
	@SerializedName("size")
	@Expose
	public String size;
	@SerializedName("docDesc")
	@Expose
	public String docDesc;

	private boolean selectedByUser = false;
	private boolean rowContainHeader = false;
	private boolean hideView = false;
	private boolean viewIsLoading = false;

	public UserStatement() {
	}

	public UserStatement(String productOfferingId, String startDate, String endDate) {
		this.productOfferingId = productOfferingId;
		this.startDate = startDate;
		this.endDate = endDate;
	}


	public String getProductOfferingId() {
		return productOfferingId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setSelectedByUser(boolean value) {
		this.selectedByUser = value;
	}

	public boolean selectedByUser() {
		return selectedByUser;
	}

	public void setHeader(boolean header) {
		this.rowContainHeader = header;
	}


	public void showStatementView(boolean view) {
		this.hideView = view;
	}

	public boolean getStatementView() {
		return hideView;
	}

	public boolean viewIsLoading() {
		return viewIsLoading;
	}

	public void setViewIsLoading(boolean viewIsLoading) {
		this.viewIsLoading = viewIsLoading;
	}
}

package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Offer {

	@SerializedName("offerId")
	@Expose
	public Integer offerId;
	@SerializedName("status")
	@Expose
	public String status;
	@SerializedName("currCredit")
	@Expose
	public Integer currCredit = 0;
	@SerializedName("creditReqestMin")
	@Expose
	public Integer creditReqestMin;
	@SerializedName("creditRequestMax")
	@Expose
	public Integer creditRequestMax;
	@SerializedName("creditOffered")
	@Expose
	public Integer creditOffered;
	@SerializedName("disposableIncome")
	@Expose
	public Integer disposableIncome;
	@SerializedName("createDate")
	@Expose
	public String createDate;
	@SerializedName("createdBy")
	@Expose
	public String createdBy;
	@SerializedName("creditAccepted")
	@Expose
	public Integer creditAccepted;
	@SerializedName("offerRecalc")
	@Expose
	public Boolean offerRecalc;
	@SerializedName("offerData")
	@Expose
	public List<OfferDatum> offerData = null;
	@SerializedName("declineMessage")
	@Expose
	public String declineMessage;
	@SerializedName("declineReasons")
	@Expose
	public List<DeclineReason> declineReasons = null;
	@SerializedName("readOnly")
	@Expose
	public Boolean readOnly;

}
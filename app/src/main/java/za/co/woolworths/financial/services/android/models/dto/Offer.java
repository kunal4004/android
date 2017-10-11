package za.co.woolworths.financial.services.android.models.dto;


import java.util.List;

public class Offer {
	public Integer offerId;
	public String status;
	public Integer currCredit;
	public Integer creditReqestMin;
	public Integer creditRequestMax;
	public Integer creditOffered;
	public Integer disposableIncome;
	public String createDate;
	public String createdBy;
	public Integer creditAccepted;
	public Boolean offerRecalc;
	public List<OfferDatum> offerData = null;
	public String declineMessage;
	public List<DeclineReason> declineReasons = null;
	public Boolean readOnly;

}
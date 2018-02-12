package za.co.woolworths.financial.services.android.models.dto.statement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class USDocuments {
	@SerializedName("document")
	@Expose
	public List<USDocument> document = null;
}

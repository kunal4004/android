package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubCategory {

	@SerializedName("categoryName")
	@Expose
	public String categoryName;
	@SerializedName("categoryId")
	@Expose
	public String categoryId;
	@SerializedName("hasChildren")
	@Expose
	public Boolean hasChildren;

	public List<SubCategory> subCategoryList;
	public boolean singleViewLoading;
}
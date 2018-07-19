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

	@SerializedName("dimValId")
	@Expose
	public String dimValId;

	public String imgUrl;
	public List<SubCategory> subCategoryList;
	public boolean singleProductItemIsLoading;

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setHasChildren(Boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public Boolean getHasChildren() {
		return hasChildren;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setSingleProductItemIsLoading(boolean singleProductItemIsLoading) {
		this.singleProductItemIsLoading = singleProductItemIsLoading;
	}

	public boolean SingleProductItemIsLoading() {
		return singleProductItemIsLoading;
	}
}
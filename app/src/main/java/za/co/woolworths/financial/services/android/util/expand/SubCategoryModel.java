package za.co.woolworths.financial.services.android.util.expand;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;

public class SubCategoryModel implements ParentListItem {
	private SubCategory subCategory;
	private List<SubCategoryChild> subCategoryChildList;

	public SubCategoryModel(SubCategory subCategory, List<SubCategoryChild> subCategoryChildList) {
		this.subCategory = subCategory;
		this.subCategoryChildList = subCategoryChildList;
	}

	public void setSubCategoryChildList(List<SubCategoryChild> subCategoryChildList) {
		this.subCategoryChildList = subCategoryChildList;
	}

	public String getName() {
		return subCategory.categoryName;
	}

	public String getCategoryId() {
		return subCategory.categoryId;
	}

	public String getImageUrl() {
		return subCategory.imgUrl;
	}

	@Override
	public List<?> getChildItemList() {
		return subCategoryChildList;
	}

	@Override
	public boolean isInitiallyExpanded() {
		return false;
	}

	public void setSubCategory(SubCategory subCategory) {
		this.subCategory = subCategory;
	}

	public SubCategory getSubCategory() {
		return subCategory;
	}
}

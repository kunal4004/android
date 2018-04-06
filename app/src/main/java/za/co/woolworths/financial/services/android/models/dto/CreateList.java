package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

public class CreateList {

	private String name;
	private List<AddToListRequest> items;

	public CreateList(String name, List<AddToListRequest> items) {
		this.name = name;
		this.items = items;
	}
}

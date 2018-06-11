package za.co.woolworths.financial.services.android.models.dao;

import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;

/**
 * Created by w7099877 on 2018/06/11.
 */

public class AppInstanceObject {

	private ShoppingDeliveryLocation shoppingDeliveryLocation;
	private String jwt;

	public AppInstanceObject(ShoppingDeliveryLocation shoppingDeliveryLocation) {
		this.shoppingDeliveryLocation = shoppingDeliveryLocation;
	}

	public AppInstanceObject(){

	}

	public void save() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
		sessionDao.value = new Gson().toJson(new AppInstanceObject(this.shoppingDeliveryLocation));
		try {
			sessionDao.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AppInstanceObject get() {
		AppInstanceObject appInstanceObject = null;
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
		if (sessionDao.value != null)
			appInstanceObject = new Gson().fromJson(sessionDao.value, AppInstanceObject.class);
		return appInstanceObject;
	}

	public ShoppingDeliveryLocation getShoppingDeliveryLocation() {
		return shoppingDeliveryLocation;
	}

	public String getJwt() {
		return jwt;
	}
}

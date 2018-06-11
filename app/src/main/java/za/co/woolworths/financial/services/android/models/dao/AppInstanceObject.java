package za.co.woolworths.financial.services.android.models.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

/**
 * Created by w7099877 on 2018/06/11.
 */

public class AppInstanceObject {

	private ShoppingDeliveryLocation shoppingDeliveryLocation;
	private JWTDecodedModel jwt;
	private String userEmailId;


	public AppInstanceObject(ShoppingDeliveryLocation shoppingDeliveryLocation) {
		this.shoppingDeliveryLocation = shoppingDeliveryLocation;
		this.jwt = this.getCurrentUserJWT();
		this.userEmailId = jwt.email.get(0);
	}

	public AppInstanceObject() {
		this.jwt = this.getCurrentUserJWT();
		this.userEmailId = jwt.email.get(0);
	}

	public void save() {

		List<AppInstanceObject> appInstanceObjects = new ArrayList<>();
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
		if (sessionDao.value == null) {
			appInstanceObjects.add(new AppInstanceObject(this.shoppingDeliveryLocation));
		} else {
			Type type = new TypeToken<List<AppInstanceObject>>() {
			}.getType();
			appInstanceObjects = new Gson().fromJson(sessionDao.value, type);
			boolean isUSerFound = false;
			for (AppInstanceObject object : appInstanceObjects) {
				if (object.userEmailId.equalsIgnoreCase(this.userEmailId)) {
					object.shoppingDeliveryLocation = this.shoppingDeliveryLocation;
					isUSerFound = true;
					break;
				}
			}
			if (!isUSerFound)
				appInstanceObjects.add(new AppInstanceObject(this.shoppingDeliveryLocation));

		}
		sessionDao.value = new Gson().toJson(appInstanceObjects);
		try {
			sessionDao.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AppInstanceObject get() {
		AppInstanceObject appInstanceObject = null;
		List<AppInstanceObject> appInstanceObjects;
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
		if (sessionDao.value != null) {
			Type type = new TypeToken<List<AppInstanceObject>>() {
			}.getType();
			appInstanceObjects = new Gson().fromJson(sessionDao.value, type);
			for (AppInstanceObject appInstance : appInstanceObjects) {
				if (appInstance.userEmailId.equalsIgnoreCase(this.userEmailId))
					return appInstance;
			}
		}
		return appInstanceObject;
	}

	public ShoppingDeliveryLocation getShoppingDeliveryLocation() {
		return shoppingDeliveryLocation;
	}

	private JWTDecodedModel getCurrentUserJWT() {
		return SessionUtilities.getInstance().getJwt();
	}
}

package za.co.woolworths.financial.services.android.models.dao;

import com.google.gson.Gson;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

/**
 * Created by w7099877 on 2018/06/12.
 */

public class AppInstanceObject {

	public ArrayList<User> users;

	public AppInstanceObject() {
		users = new ArrayList<>();
	}

	public static AppInstanceObject get() {
		try {
			SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
			if (sessionDao.value != null) {
				return new Gson().fromJson(sessionDao.value, AppInstanceObject.class);
			}
		} catch (Exception e) {

		}

		return new AppInstanceObject();
	}

	public User getCurrentUserObject() {

		if (this.users.size() == 0) {
			return new User();
		}else {
			for (User user : this.users)
			{
				if(user.id.equalsIgnoreCase(getCurrentUsersID())){
					return user;
				}
			}
		}

		return new User();
	}

	public void save() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
		sessionDao.value = new Gson().toJson(this);
		try {
			sessionDao.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class User {
		public String id;
		public ShoppingDeliveryLocation preferredShoppingDeliveryLocation;
		public ArrayList<ShoppingDeliveryLocation> shoppingDeliveryLocationHistory;

		public User() {
			id = AppInstanceObject.getCurrentUsersID();
			shoppingDeliveryLocationHistory = new ArrayList<>();
		}

		public void save() {
			AppInstanceObject appInstanceObject = AppInstanceObject.get();
			if (appInstanceObject.users.size() == 0) {
				appInstanceObject.users.add(this);
			} else {
				int index = -1;
				for (int i = 0; i <= appInstanceObject.users.size(); i++) {
					if (appInstanceObject.users.get(i).id.equalsIgnoreCase(this.id)) {
						index = i;
						break;
					}
				}
				if (index == -1){
					appInstanceObject.users.add(this);
				}
				else
					appInstanceObject.users.set(index, this);

			}
			appInstanceObject.save();
		}

	}
	public static String getCurrentUsersID() {
		return SessionUtilities.getInstance().getJwt().email.get(0);
	}

}

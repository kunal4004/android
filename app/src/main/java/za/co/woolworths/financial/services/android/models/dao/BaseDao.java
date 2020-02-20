package za.co.woolworths.financial.services.android.models.dao;

import android.util.Log;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public abstract class BaseDao {
    private final String TAG = "BaseDao";

    public String id;
    public String dateCreated;
    public String dateUpdated;

    //subclass must override
    public String getTableName(){
        throw new RuntimeException("["+TAG+"]: method get() was not overridden!");
    }

    /*
    //subclass must override
    public BaseDao get() throws Exception{
        throw new RuntimeException("["+TAG+"]: method get() was not overridden!");
    }
    */

    //subclass must override
    public void update() throws Exception{
        throw new RuntimeException("["+TAG+"]: method update() was not overridden!");
    }

    //subclass must override
    public void insert() throws Exception{
        throw new RuntimeException("["+TAG+"]: method insert() was not overridden!");
    }


    public void save() throws Exception {
        try {
            //perform update first. If update fails, perform insert
            this.update();
            return;

        } catch (Exception e) {
        }

        try {
            //perform insert
            this.insert();
            return;

        } catch (Exception e) {
        }
    }
}

package za.co.woolworths.financial.services.android.models.dao;

import android.content.Context;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public abstract class BaseDao {

    protected Context mContext;

    public String id;
    public String dateCreated;
    public String dateUpdated;

    public BaseDao(){ }

    public BaseDao(Context mContext){
        this.mContext = mContext;
    }

    //subclass must override
    public String getTableName(){
        throw new RuntimeException("[BaseDao]: method getTableName() was not overridden!");
    }

    //subclass must override
    public BaseDao get() throws Exception{
        throw new RuntimeException("[BaseDao]: method get() was not overridden!");
    }
}

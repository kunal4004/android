package za.co.woolworths.financial.services.android.models.dao;

import android.content.Context;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public abstract class BaseDao {

    protected Context mContext;

    public BaseDao(){ }

    public BaseDao(Context mContext){
        this.mContext = mContext;
    }

    //subclass must override
    public String getTableName(){
        throw new RuntimeException("[BaseDao]: method getTableName was not overriden!");
    }
}

package za.co.woolworths.financial.services.android.util.binder;

/**
 * Created by yqritc on 2015/04/20.
 */
public abstract class EnumListBindAdapter<E extends Enum<E>> extends ListBindAdapter {

    public <T extends DataBinder> T getDataBinder(E e) {
        return getDataBinder(e.ordinal());
    }
}

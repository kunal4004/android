package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

public class DyKeywordSearchRequestEvent implements Serializable {
    private Session session;

    private Context context;

    private User user;

    private List<Events> events;

    public DyKeywordSearchRequestEvent(Session session, Context context, User user, List<Events> events) {
        this.session = session;
        this.context = context;
        this.user = user;
        this.events = events;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Events> getEvents() {
        return this.events;
    }

    public void setEvents(List<Events> events) {
        this.events = events;
    }

}


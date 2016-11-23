package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by denysvera on 2016/04/29.
 */
public class Expiry {
    public long expiryDate;
    public String updateUrl;
    public String expiryMsg;
    public String reminderMessage;
    public long reminderInterval;

    public long getExpiry_date() {
        return expiryDate;
    }

    public long getReminder_interval() {
        return reminderInterval;
    }

    public String getExpiry_msg() {
        return expiryMsg;
    }

    public String getReminder_msg() {
        return reminderMessage;
    }

    public String getUpdate_url() {
        return updateUrl;
    }

    public void setExpiry_date(long expiry_date) {
        this.expiryDate = expiry_date;
    }

    public void setExpiry_msg(String expiry_msg) {
        this.expiryMsg = expiry_msg;
    }

    public void setReminder_interval(long reminder_interval) {
        this.reminderInterval = reminder_interval;
    }

    public void setReminder_msg(String reminder_msg) {
        this.reminderMessage = reminder_msg;
    }

    public void setUpdate_url(String update_url) {
        this.updateUrl = update_url;
    }
}

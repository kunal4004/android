package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

/**
 * Created by W7099877 on 02/11/2016.
 */

public class MessageResponse {
    public int httpCode;
    public Response response;
    public int unreadCount;
    public List<MessageDetails> messagesList;
}

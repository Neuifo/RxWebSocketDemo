package com.neuifo.appkit.model.socket;

import java.util.List;

/**
 * Created by neuifo on 2017/10/30.
 */

public class RegistInfo {

    public String requestId;
    public String sendTime;
    public List<String> msgIds;


    @Override public String toString() {
        return "RegistInfo{"
            + "requestId='"
            + requestId
            + '\''
            + ", sendTime='"
            + sendTime
            + '\''
            + '}';
    }
}

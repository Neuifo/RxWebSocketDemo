/*
 * Copyright (C) 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.neuifo.appkit.websocket.core.object.event;



import com.neuifo.appkit.notification.util.ObjectSerializer;
import com.neuifo.appkit.websocket.core.object.ObjectParseException;
import com.neuifo.appkit.websocket.core.object.ObjectWebSocketSender;
import javax.annotation.Nonnull;

/**
 * Event indicating that data returned by server was not correctly parsed
 *
 * This means {@link ObjectParseException} was returned via {@link ObjectSerializer}
 */
public abstract class RxObjectEventWrongMessageFormat extends RxObjectEventConn {
    @Nonnull
    private final ObjectParseException exception;

    public RxObjectEventWrongMessageFormat(@Nonnull ObjectWebSocketSender sender,
                                           @Nonnull ObjectParseException exception) {
        super(sender);
        this.exception = exception;
    }

    @Nonnull
    public ObjectParseException exception() {
        return exception;
    }

}

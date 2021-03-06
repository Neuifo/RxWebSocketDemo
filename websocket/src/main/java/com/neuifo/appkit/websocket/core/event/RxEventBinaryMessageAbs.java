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

package com.neuifo.appkit.websocket.core.event;

import javax.annotation.Nonnull;

import okhttp3.ws.WebSocket;

/**
 * Abstract class for binary messages returned by server
 *
 * @see RxEventBinaryMessage
 * @see RxEventPong
 */
public abstract class RxEventBinaryMessageAbs extends RxEventConn {

    @Nonnull
    private final byte[] message;

    public RxEventBinaryMessageAbs(@Nonnull WebSocket sender, @Nonnull byte[] message) {
        super(sender);
        this.message = message;
    }

    /**
     * Binary message that was returned by server
     *
     * @return binary message
     */
    @Nonnull
    public byte[] message() {
        return message;
    }

}

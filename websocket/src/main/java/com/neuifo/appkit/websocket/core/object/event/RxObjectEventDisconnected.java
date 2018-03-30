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



import com.neuifo.appkit.websocket.core.event.RxEventDisconnected;
import com.neuifo.appkit.websocket.core.object.ObjectWebSocketSender;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Event indicate that client was disconnected to the server
 *
 * since then all execution on previosly returned {@link ObjectWebSocketSender} will cause throwing
 * {@link IOException}
 *
 * See: {@link RxEventDisconnected}
 */
public class RxObjectEventDisconnected extends RxObjectEvent {
    @Nonnull
    private final IOException exception;

    public RxObjectEventDisconnected(@Nonnull IOException exception) {
        super();
        this.exception = exception;
    }

    /**
     * See: {@link RxEventDisconnected#exception()}
     */
    @Nonnull
    public IOException exception() {
        return exception;
    }

    @Override
    public String toString() {
        return "RxJsonEventDisconnected{" + "exception=" + exception + '}';
    }
}

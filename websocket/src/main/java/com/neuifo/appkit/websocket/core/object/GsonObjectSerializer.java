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

package com.neuifo.appkit.websocket.core.object;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GsonObjectSerializer implements ObjectSerializer {

    @NonNull
    private final Gson gson;
    @NonNull
    private final Type typeOfT;

    public GsonObjectSerializer(@NonNull Gson gson, @NonNull Type typeOfT) {
        this.gson = gson;
        this.typeOfT = typeOfT;
    }

    @NonNull
    @Override
    public Object serialize(@NonNull String message) throws ObjectParseException {
        try {
            return gson.fromJson(message, typeOfT);
        } catch (JsonParseException e) {
            throw new ObjectParseException("Could not parse", e);
        }
    }

    @NonNull
    @Override
    public Object serialize(@NonNull byte[] message) throws ObjectParseException {
        throw new ObjectParseException("Could not parse binary messages");
    }

    @NonNull
    @Override
    public byte[] deserializeBinary(@NonNull Object message) throws ObjectParseException {
        throw new IllegalStateException("Only serialization to string is available");
    }

    @NonNull
    @Override
    public String deserializeString(@NonNull Object message) throws ObjectParseException {
        try {
            return gson.toJson(message);
        } catch (JsonParseException e) {
            throw new ObjectParseException("Could not parse", e);
        }
    }

    @Override
    public boolean isBinary(@NonNull Object message) {
        return false;
    }
}

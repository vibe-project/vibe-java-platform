/*
 * Copyright 2014 The Vibe Project
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
 * limitations under the License.
 */
package org.atmosphere.vibe.platform.server;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.Actions;
import org.atmosphere.vibe.platform.HttpStatus;
import org.atmosphere.vibe.platform.SimpleActions;
import org.atmosphere.vibe.platform.VoidAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link ServerHttpExchange}.
 *
 * @author Donghwan Kim
 */
public abstract class AbstractServerHttpExchange implements ServerHttpExchange {

    protected final Actions<Object> bodyActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
    protected final Actions<Void> closeActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
    protected final Actions<Throwable> errorActions = new SimpleActions<>();

    private final Logger logger = LoggerFactory.getLogger(AbstractServerHttpExchange.class);
    private boolean read;
    private boolean ended;

    public AbstractServerHttpExchange() {
        errorActions.add(new Action<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                logger.trace("{} has received a throwable {}", AbstractServerHttpExchange.this, throwable);
            }
        });
        closeActions.add(new VoidAction() {
            @Override
            public void on() {
                logger.trace("{} has been closed", AbstractServerHttpExchange.this);
                bodyActions.disable();
                errorActions.disable();
            }
        });
    }

    @Override
    public String header(String name) {
        List<String> headers = headers(name);
        return headers != null && headers.size() > 0 ? headers.get(0) : null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ServerHttpExchange bodyAction(Action action) {
        // TODO move to read method https://github.com/vibe-project/vibe-java-platform/issues/12
        if (!read) {
            read = true;
            String contentType = header("content-type");
            // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html#sec7.2.1
            boolean isText = contentType != null && contentType.startsWith("text/");
            if (isText) {
                readAsText();
            } else {
                readAsBinary();
            }
        }
        bodyActions.add(action);
        if (ended) {
            // TODO use endAction https://github.com/vibe-project/vibe-java-platform/issues/14
            bodyActions.add(new Action<Object>() {
                @Override
                public void on(Object _) {
                    closeActions.fire();
                }
            });
        }
        return this;
    }

    protected abstract void readAsText();

    protected abstract void readAsBinary();

    @Override
    public final ServerHttpExchange setHeader(String name, Iterable<String> value) {
        // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        Iterator<String> iterator = value.iterator();
        StringBuilder builder = new StringBuilder(iterator.next());
        while (iterator.hasNext()) {
            builder.append(", ").append(iterator.next());
        }
        return setHeader(name, builder.toString());
    }

    @Override
    public ServerHttpExchange setHeader(String name, String value) {
        logger.trace("{} sets a response header {} to {}", this, name, value);
        doSetHeader(name, value);
        return this;
    }

    protected abstract void doSetHeader(String name, String value);

    @Override
    public ServerHttpExchange write(String data) {
        logger.trace("{} sends a text chunk {}", this, data);
        doWrite(data);
        return this;
    }

    protected abstract void doWrite(String data);

    @Override
    public ServerHttpExchange write(ByteBuffer byteBuffer) {
        logger.trace("{} sends a binary chunk {}", this, byteBuffer);
        doWrite(byteBuffer);
        return this;
    }

    protected abstract void doWrite(ByteBuffer byteBuffer);

    @Override
    public ServerHttpExchange end() {
        logger.trace("{} has started to close the connection", this);
        if (!ended) {
            ended = true;
            doEnd();
            if (read) {
                closeActions.fire();
            }
        }
        return this;
    }

    protected abstract void doEnd();

    @Override
    public ServerHttpExchange end(String data) {
        return write(data).end();
    }
    
    @Override
    public ServerHttpExchange end(ByteBuffer data) {
        return write(data).end();
    }

    @Override
    public ServerHttpExchange setStatus(HttpStatus status) {
        logger.trace("{} sets a response status, {}", this, status);
        doSetStatus(status);
        return this;
    }

    protected abstract void doSetStatus(HttpStatus status);

    @Override
    public ServerHttpExchange closeAction(Action<Void> action) {
        closeActions.add(action);
        return this;
    }

    @Override
    public ServerHttpExchange errorAction(Action<Throwable> action) {
        errorActions.add(action);
        return this;
    }

}

/*
 * Copyright (c) 2025 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.quartz.nip01Core.relay.sockets.browser

import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import com.vitorpamplona.quartz.nip01Core.relay.sockets.WebSocket
import com.vitorpamplona.quartz.nip01Core.relay.sockets.WebSocketListener
import com.vitorpamplona.quartz.nip01Core.relay.sockets.WebsocketBuilder

/** Send data on an existing JS WebSocket object. */
@JsFun("(ws, data) => ws.send(data)")
private external fun jsSend(
    ws: JsAny,
    data: String,
)

/** Close an existing JS WebSocket object. */
@JsFun("(ws) => ws.close()")
private external fun jsClose(ws: JsAny)

/** Return the readyState of a JS WebSocket (0=CONNECTING, 1=OPEN, 2=CLOSING, 3=CLOSED). */
@JsFun("(ws) => ws.readyState")
private external fun jsReadyState(ws: JsAny): Int

/**
 * Create a browser-native WebSocket and wire up Kotlin callbacks.
 *
 * All callbacks are simple Kotlin lambdas; the Kotlin/wasmJs compiler generates
 * the required JS adapter stubs automatically.
 */
@JsFun(
    """
(url, onOpen, onMessage, onClose, onError) => {
    const ws = new WebSocket(url);
    ws.onopen    = function(e)  { onOpen(); };
    ws.onmessage = function(e)  { onMessage(e.data); };
    ws.onclose   = function(e)  { onClose(e.code | 0, e.reason || ''); };
    ws.onerror   = function(e)  { onError(); };
    return ws;
}
""",
)
private external fun jsNewWebSocket(
    url: String,
    onOpen: () -> Unit,
    onMessage: (String) -> Unit,
    onClose: (Int, String) -> Unit,
    onError: () -> Unit,
): JsAny

/**
 * [WebSocket] implementation for the browser using the native JS WebSocket API.
 *
 * Creates a new connection on [connect] and closes it on [disconnect]. The
 * [needsReconnect] flag is set when the underlying socket closes unexpectedly so
 * the relay client knows to re-attempt the connection.
 */
class BrowserWebSocket(
    private val url: NormalizedRelayUrl,
    private val listener: WebSocketListener,
) : WebSocket {
    private var jsSocket: JsAny? = null
    private var needsReconnectFlag = false

    override fun needsReconnect(): Boolean = needsReconnectFlag

    override fun connect() {
        needsReconnectFlag = false
        jsSocket =
            jsNewWebSocket(
                url = url.url,
                onOpen = {
                    listener.onOpen(pingMillis = 0, compression = false)
                },
                onMessage = { data ->
                    listener.onMessage(data)
                },
                onClose = { code, reason ->
                    needsReconnectFlag = true
                    listener.onClosed(code, reason)
                },
                onError = {
                    needsReconnectFlag = true
                    listener.onFailure(
                        t = RuntimeException("WebSocket error"),
                        code = null,
                        response = null,
                    )
                },
            )
    }

    override fun disconnect() {
        val ws = jsSocket ?: return
        jsSocket = null
        needsReconnectFlag = false
        if (jsReadyState(ws) < 2) jsClose(ws)
    }

    override fun send(msg: String): Boolean {
        val ws = jsSocket ?: return false
        return if (jsReadyState(ws) == 1) {
            jsSend(ws, msg)
            true
        } else {
            false
        }
    }
}

/** [WebsocketBuilder] that creates [BrowserWebSocket] instances. */
class BrowserWebsocketBuilder : WebsocketBuilder {
    override fun build(
        url: NormalizedRelayUrl,
        out: WebSocketListener,
    ): WebSocket = BrowserWebSocket(url, out)
}

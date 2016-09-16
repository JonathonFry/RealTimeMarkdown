package com.iamjonfry.androidrealtime;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

import static okhttp3.ws.WebSocket.TEXT;

/**
 * @author Jonathon Fry on 15/09/2016.
 */

public final class WebSocketEcho implements WebSocketListener {
    private final Executor writeExecutor = Executors.newSingleThreadExecutor();
    private Callback callback;
    private WebSocket webSocket;

    public void run(Callback callback) throws IOException {
        this.callback = callback;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();

        Request request = new Request.Builder()
                .url("ws://10.0.2.2:5432/ws")
                .build();
        WebSocketCall.create(client, request).enqueue(this);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    public void sendMessage(final String message) {
        if (webSocket != null) {
            writeExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        webSocket.sendMessage(RequestBody.create(TEXT, message));
                    } catch (IOException e) {
                        System.err.println("Unable to send messages: " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onOpen(final WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        if (message.contentType() == TEXT) {
            String messageString = message.string();
            System.out.println("MESSAGE: " + messageString);
            callback.onMessage(messageString);
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        message.close();
    }

    @Override
    public void onPong(Buffer payload) {
        System.out.println("PONG: " + payload.readUtf8());
    }

    @Override
    public void onClose(int code, String reason) {
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(IOException e, Response response) {
        e.printStackTrace();
    }

    public interface Callback {
        void onMessage(String message);
    }

}

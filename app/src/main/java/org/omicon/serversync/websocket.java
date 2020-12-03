package org.omicon.serversync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Created by user on 9/4/14.
 */
public class websocket {
    Context context;
    WebSocketClient mWebSocketClient;

    public websocket(Context context) {
        this.context = context;
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://ogrpcsrv.arobil.com:8080/omicon_ws-1.0/omicon_endpoint");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.e("Websocket", "Opened");
                SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
                mWebSocketClient.send(prefs.getString("user_email", ""));
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView textView = (TextView)findViewById(R.id.messages);
                        //textView.setText(textView.getText() + "\n" + message);
                        Log.i("message from ws", message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }
}

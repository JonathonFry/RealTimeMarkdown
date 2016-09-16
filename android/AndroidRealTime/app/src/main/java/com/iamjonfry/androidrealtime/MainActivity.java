package com.iamjonfry.androidrealtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView markdownPreviewTextView = (TextView) findViewById(R.id.markdown_preview_text_view);
        final EditText markdownEditText = (EditText) findViewById(R.id.markdown_edit_text);

        final WebSocketEcho webSocketEcho = new WebSocketEcho();
        try {
            webSocketEcho.run(new WebSocketEcho.Callback() {
                @Override
                public void onMessage(String message) {
                    String[] parts = message.split(">>");
                    if (parts.length > 1) {
                        final String raw = parts[0];
                        final String html = parts[1];

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markdownPreviewTextView.setText(Html.fromHtml(html));
                                markdownEditText.removeTextChangedListener(textWatcher);
                                if (!markdownEditText.getText().toString().equals(raw)) {
                                    markdownEditText.setText(raw); // Update character position
                                }
                                markdownEditText.addTextChangedListener(textWatcher);
                            }
                        });
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                webSocketEcho.sendMessage(s.toString());
            }
        };

        markdownEditText.addTextChangedListener(textWatcher);
    }
}

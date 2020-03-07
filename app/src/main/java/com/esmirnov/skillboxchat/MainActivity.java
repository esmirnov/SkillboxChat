package com.esmirnov.skillboxchat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Server server;
    MessageController messageController;
    String myName;
    TextView onlineCount;

    @Override
    protected void onStart() {
        super.onStart();

        server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> stringStringPair) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageController.addMessage(new MessageController.Message(
                                stringStringPair.first,
                                stringStringPair.second,
                                false));
                    }
                });
            }
        }, new Consumer<Server.StatusNotification>() {
            @Override
            public void accept(final Server.StatusNotification note) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onlineCount.setText("Users online: " + note.getOnlineCount());
                        String toastText = "User " + note.getUserName();
                        if (note.isConnected())
                            toastText += " is connected";
                        else
                            toastText += " is disconnected";

                        Toast t = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 150);
                        t.show();
                    }
                });
            }
        });
        server.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        server.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText chatMessage = findViewById(R.id.chatMessage);
        Button sendButton = findViewById(R.id.sendButton);
        RecyclerView chatWindow = findViewById(R.id.chatWindow);

        onlineCount = findViewById(R.id.onlineCount);

        messageController = new MessageController();
        messageController
                .setIncomingLayout(R.layout.incoming_message)
                .setOutgoingLayout(R.layout.outgoing_message)
                .setMessageTextId(R.id.messageText)
                .setUserNameId(R.id.userName)
                .setMessageTimeId(R.id.messageTime)
                .appendTo(chatWindow, this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageController.addMessage(new MessageController.Message(
                        chatMessage.getText().toString(),
                        myName,
                        true));
                server.sendMessage(chatMessage.getText().toString());
                chatMessage.setText("");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your name:");
        final EditText nameInput = new EditText(this);
        nameInput.setMaxLines(1);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(nameInput);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myName = nameInput.getText().toString();
                server.sendName(myName);
            }
        });
        builder.show();
    }
}

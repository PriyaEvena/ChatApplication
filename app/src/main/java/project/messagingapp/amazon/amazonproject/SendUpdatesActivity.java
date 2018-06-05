package project.messagingapp.amazon.amazonproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;

public class SendUpdatesActivity extends AppCompatActivity {

    private Button send;
    private EditText message;
    private ArrayList<String> uidlist;
    private Firebase ref1,ref2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_updates);

        uidlist = (ArrayList<String>)getIntent().getSerializableExtra("uidarray");
        send = findViewById(R.id.updatesendingbutton);
        message = findViewById(R.id.content);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!message.getText().toString().trim().equals("")){
                    String msg = message.getText().toString().trim();
                    sendmessagestosubs(msg);
                    message.setText("");

                }
            }
        });
    }
    void sendmessagestosubs(String msg){
        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences sharedPreferences = getSharedPreferences("Cust",MODE_PRIVATE);
        String uname = sharedPreferences.getString("username","username");
        Firebase.setAndroidContext(this);
        for(int i=0;i<uidlist.size();i++){
            String partnerid = uidlist.get(i);
            ref1 = new Firebase("https://messagingapp-48a08.firebaseio.com/messages/" + myid + "_" + partnerid + "/msg");
            ref2 = new Firebase("https://messagingapp-48a08.firebaseio.com/messages/" + partnerid + "_" + myid + "/msg");
            HashMap<String,String> msgmap = new HashMap<String,String>();
            msgmap.put("user",uname);
            msgmap.put("message",msg);
            ref1.push().setValue(msgmap);
            ref2.push().setValue(msgmap);
            Firebase notificationReference = new Firebase("https://messagingapp-48a08.firebaseio.com/notifications/Clients/" + partnerid);
            notificationReference.child("Message").setValue(System.currentTimeMillis());
            Toast.makeText(SendUpdatesActivity.this, "Update sent", Toast.LENGTH_SHORT).show();
        }
    }
}

package project.messagingapp.amazon.amazonproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;

public class ChatActivity extends AppCompatActivity {

    String uname;
    String type;
    String partneruname;
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    ImageButton imgButton;
    String myid;
    private AIDataService aiDataService;
    Firebase reference1, reference2;
    SharedPreferences sharedPreferences;
    private static final int GALLERY_INTENT = 2;
    private String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        imgButton = (ImageButton) findViewById(R.id.camerabutton);


        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("clicked syucc1111", "");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                Log.e("clicked syucces", "");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(0, scrollView.getBottom());
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        Firebase.setAndroidContext(this);
        myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final HashMap<String,String> details = (HashMap<String, String>)getIntent().getSerializableExtra("HashMap");
        final String partnerid = intent.getStringExtra("UID");
        partneruname = details.get("username");

        reference1 = new Firebase("https://messagingapp-48a08.firebaseio.com/messages/" + myid + "_" + partnerid + "/msg");
        reference2 = new Firebase("https://messagingapp-48a08.firebaseio.com/messages/" + partnerid + "_" + myid + "/msg");

        sharedPreferences = getSharedPreferences("Cust",MODE_PRIVATE);
        uname = sharedPreferences.getString("username","username");
        type = sharedPreferences.getString("type","typenotfound");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", uname);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("messages" );
                    myRef.child(partnerid + "_" +myid ).child("newmsg").setValue("yes");

                    if(type.equals("Clients")){
                        Log.e(TAG,"client msged");
                        final AIConfiguration config = new AIConfiguration("d991034195684cfa9a058849af17aaaf",
                                AIConfiguration.SupportedLanguages.English,
                                AIConfiguration.RecognitionEngine.System);


                        aiDataService = new AIDataService(ChatActivity.this, config);



                        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

                            private AIError aiError;

                            @Override
                            protected AIResponse doInBackground(final String... params) {
                                final AIRequest request = new AIRequest();
                                String query = params[0];
                                String event = params[1];

                                if (!TextUtils.isEmpty(query))
                                    request.setQuery(query);
                                if (!TextUtils.isEmpty(event))
                                    request.setEvent(new AIEvent(event));
                                final String contextString = params[2];
                                RequestExtras requestExtras = null;
                                if (!TextUtils.isEmpty(contextString)) {
                                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                                    requestExtras = new RequestExtras(contexts, null);
                                }

                                try {
                                    return aiDataService.request(request, requestExtras);
                                } catch (final AIServiceException e) {
                                    aiError = new AIError(e);
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(final AIResponse response) {
                                if (response != null) {
                                    HashMap<String,String> hm= new HashMap<>();
                                    Log.e(TAG,"response obtained");
                                    final Result result = response.getResult();
                                    final String speech = result.getFulfillment().getSpeech();
                                    hm.put("speech",speech);
                                    final Metadata metadata = result.getMetadata();
                                    final String intentid = metadata.getIntentId();
                                    hm.put("intentid",intentid);
                                    Log.e(TAG,hm.toString()+"from inside");

                                    Map<String, String> msgmap = new HashMap<String, String>();

                                    msgmap.put("user", details.get("username"));

                                    while(true) {
                                        try {
                                            if (hm.get("intentid").equals("74421ee9-b383-4718-b369-0ee88f54e3f4") || hm.get("intentid").equals("97fa5871-6c8c-4086-b392-d68727064623")) {
                                                msgmap.put("message", hm.get("speech"));
                                                reference1.push().setValue(msgmap);
                                                reference2.push().setValue(msgmap);
                                                Log.e(TAG, "hello/timings intent");

                                            } else if (hm.get("intentid").equals("84e47689-235a-4bd8-ba8e-d0203aeead6f")) {
                                                msgmap.put("message", details.get("username"));
                                                reference1.push().setValue(msgmap);
                                                reference2.push().setValue(msgmap);
                                                Log.e(TAG, "name intent" + details.get("username"));

                                            } else if (hm.get("intentid").equals("7e19ba44-ecf7-42bf-98c5-40c68c2109d7")) {
                                                msgmap.put("message", details.get("services"));
                                                reference1.push().setValue(msgmap);
                                                reference2.push().setValue(msgmap);
                                                Log.e(TAG, "services intent" + details.get("services"));

                                            } else {
                                                String reciever;
                                                // Notification
                                                reciever = "BusinessOwner";
                                                Firebase notificationReference = new Firebase("https://messagingapp-48a08.firebaseio.com/notifications/" + reciever + "/" + partnerid);
                                                notificationReference.child("Message").setValue(System.currentTimeMillis());
                                            }
                                            Log.e(TAG, hm.toString());
                                            break;
                                        }catch(Exception ex){
                                            Log.e(TAG,"exception");
                                        }
                                    }

                                } else {
                                    Log.e(TAG,"error found");
                                    //onError(aiError);
                                }
                            }
                        };

                        task.execute(messageText, null, null);

                    }
                    else{
                        String reciever = "Clients";
                        Firebase notificationReference = new Firebase("https://messagingapp-48a08.firebaseio.com/notifications/"+reciever+"/"+partnerid);
                        notificationReference.child("Message").setValue(System.currentTimeMillis());
                    }

                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);

                String userName = map.get("user").toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("messages" );
                myRef.child(myid + "_" + partnerid).child("newmsg").setValue("no");

                if(map.get("message")!=null){
                    String message = map.get("message").toString();
                    if(userName.equals(uname)){
                        addMessageBox("You:-\n" + message, 1);
                    }
                    else{
                        addMessageBox(partneruname + ":-\n" + message, 2);
                    }
                }
                else{
                    if(userName.equals(uname)){
                        addImageView(map.get("imageurl"),1);
                    }
                    else {
                        addImageView(map.get("imageurl"), 2);
                    }
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void addMessageBox(String message, int type) {
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lp2.weight = 1.0f;

        if (type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.shadowedbackground);
        } else {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.shadowedbackground);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(0, scrollView.getBottom());
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void addImageView(Object uri,int type) {

        ImageView imageView = new ImageView(ChatActivity.this);
        Glide.with(getBaseContext())
                .load(uri.toString())
                .into(imageView);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 2.0f;

        if (type == 1) {
            lp2.gravity = Gravity.LEFT;
            //textView.setBackgroundResource(R.drawable.bubble1);
        } else {
            lp2.gravity = Gravity.RIGHT;
            //textView.setBackgroundResource(R.drawable.bubble_out);
        }
        imageView.setLayoutParams(lp2);
        layout.addView(imageView);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(0, scrollView.getBottom());
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) { //uploading pic
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onactiv is called Main", "");
        SharedPreferences prefs = getSharedPreferences("Cust", MODE_PRIVATE);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://messagingapp-48a08.appspot.com");
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {


            //for confirmation of uploading
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Confirm Send")
                    .setMessage("Send Image??")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with sending

                            //data
                            Uri uri = data.getData();
                            final String timenow = (new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())).toString();
                            StorageReference filepath = storageRef.child(uid + "-" + timenow);

                            //uploading
                            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(ChatActivity.this, "upload done", Toast.LENGTH_LONG);
                                    storageRef.child(uid + "-" + timenow).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Got the download URL for 'users/me/profile.png'
                                            Log.e("uchat-", uri + ""); /// The string(file link) that you need
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("imageurl", uri + "");
                                            map.put("user", uname);
                                            reference1.push().setValue(map);
                                            reference2.push().setValue(map);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                            Log.e("fail in up of chat img", "");
                                        }
                                    });
                                }
                            });

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

}


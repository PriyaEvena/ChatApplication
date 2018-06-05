package project.messagingapp.amazon.amazonproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private String phno;
    private EditText phnum;
    private ProgressDialog dialog;
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "LoginActivity";

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //declaring views
        dialog = new ProgressDialog(this);
        //setting ui
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.e(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                String tokenId = FirebaseInstanceId.getInstance().getToken();
                Log.e(TAG,"Token "+ tokenId);
                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                //updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]
                dialog.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Invalid phone number",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                //updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(final String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                //updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
//
//                new AlertDialog.Builder(LoginActivity.this)
//                        .setTitle("OTP Verification")
//                        .setMessage("Enter OTP")
//                        .setView(R.layout.otpfield)
//                        .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                LayoutInflater Li = LayoutInflater.from(LoginActivity.this);
//                                View promptsview = Li.inflate(R.layout.otpfield,null);
//                                EditText ed = (EditText) promptsview.findViewById(R.id.otpNumber);
//                                String otp = ed.getText().toString();
//                                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
//                                signInWithPhoneAuthCredential(credential);
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(LoginActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String otp = input.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                        signInWithPhoneAuthCredential(credential);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        };
        // [END phone_auth_callbacks]


    }
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        updateUII(currentUser);
    }
    public void updateUII(FirebaseUser user){
        Log.e(TAG,"in start");

        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
    }

    public void GotoRegister(View v){
        Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(i);
    }
    public void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        Log.e(TAG,"entered in start");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            String url="https://messagingapp-48a08.firebaseio.com/phoneno.json";
                            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                                @Override
                                public void onResponse(String s) {
                                    Log.e(TAG,"In signin method");
                                    updateSharedPreferences(s);
                                }
                            },new com.android.volley.Response.ErrorListener(){
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    System.out.println("" + volleyError);
                                }
                            });
                            RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
                            rQueue.add(request);

                            Intent i = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(i);

                            Log.d(TAG, "signInWithCredential:success");
                            dialog.dismiss();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Snackbar.make(findViewById(android.R.id.content), "Invalid OTP",
                                        Snackbar.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    }
                });
    }
    public void updateSharedPreferences(String s){

        String path="";
        try{
            JSONObject obj = new JSONObject(s);
            path = obj.getString(phno);
            Log.e(TAG,"sub path is"+path);
        }catch(JSONException ex){
            Log.e(TAG,s+".json");
            Log.e(TAG, ex.getMessage());
        }
        Log.e(TAG,path);
        StringRequest request = new StringRequest(Request.Method.GET, "https://messagingapp-48a08.firebaseio.com/"+path+".json", new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                updateSharedpreferences2(s);
            }
        },new com.android.volley.Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
                Log.e(TAG,"Volley"+volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
        rQueue.add(request);
    }
    public void updateSharedpreferences2(String path){
        String uname="";
        String typeofuser="";
        sharedPreferences = getSharedPreferences("Cust",MODE_PRIVATE);
        try{
            JSONObject obj = new JSONObject(path);
            uname = obj.getString("username");
            typeofuser = obj.getString("type");
        }catch(Exception ex){
            Log.e(TAG,"path is "+path+".json");
            Log.e(TAG, ex.getMessage());
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",uname);
        editor.putString("type",typeofuser);
        Log.e(TAG,uname);
        Log.e(TAG,typeofuser);
        editor.commit();
        Log.e(TAG,"updated");
    }
    public void loginclick(View v){
        phnum = (EditText) findViewById(R.id.pno);
        phno = phnum.getText().toString();
        dialog.setMessage("Hang On...");
        dialog.setCancelable(false);
        if(phno==null || phno.equals("")){
            Toast.makeText(LoginActivity.this,"Please enter phone number",Toast.LENGTH_SHORT).show();

        }
        else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef2 = database.getReference("phoneno");
            Log.e("hey ","everything is fine");

            myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(phno)){
                        myRef2.child("dum").removeValue();
                        //dialog.dismiss();
                        View view = getCurrentFocus();
                        view.clearFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        Snackbar.make(findViewById(android.R.id.content), "You need to be registered",
                                Snackbar.LENGTH_SHORT).show();
                        //Intent i = new Intent(LoginActivity.this,LoginActivity.class);
                        //startActivity(i);
                    }
                    else{
                        dialog.show();
                        myRef2.child("dum").removeValue();
                        Log.e("hey","removed dum");
                        startPhoneNumberVerification(phno);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            myRef2.child("dum").setValue("dum");
        }
    }
}

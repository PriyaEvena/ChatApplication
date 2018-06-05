package project.messagingapp.amazon.amazonproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    private String userGivenName;
    private String phoneNumber;
    private String nameattr;
    private String services;
    private String typeOfCust;
    private EditText name;
    private EditText nameatt;
    private EditText phone;
    private EditText pass;
    private EditText serviceProvided;
    private Spinner spinner;
    private ImageView profileImg;
    private Cust cust;
    private Uri imageuri;
    private AlertDialog userDialog;

    SharedPreferences sharedPreferences;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private static final int GALLERY_INTENT = 2;
    private String profileuri;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private static final String TAG = "PhoneAuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button register = (Button) findViewById(R.id.registerbutton);
        name = (EditText) findViewById(R.id.Username);
        phone = (EditText) findViewById(R.id.mobilenum);
        nameatt = (EditText) findViewById(R.id.fullname);
        serviceProvided = (EditText) findViewById(R.id.servicesprovided);

        spinner = (Spinner) findViewById(R.id.typeOfCustomer);
        //EditText pwd = (EditText)findViewById(R.id.password);
        dialog = new ProgressDialog(this);
        profileImg = (ImageView) findViewById(R.id.profilepic);


        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("clicked syucc1111", "");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                Log.e("clicked syucces", "");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });



        mAuth = FirebaseAuth.getInstance();


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeOfCust = adapterView.getItemAtPosition(i).toString();
                if(typeOfCust.equals("Clients")){
                    serviceProvided.setFocusable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeOfCust = adapterView.getItemAtPosition(0).toString();
            }
        });

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
                final AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);

                final EditText edittext = new EditText(RegisterActivity.this);
                alert.setTitle("Enter OTP");
                alert.setView(edittext);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String otp = edittext.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                        signInWithPhoneAuthCredential(credential);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        dialog.dismiss();
                    }
                });
                alert.show();
//                new AlertDialog.Builder(RegisterActivity.this)
//                        .setTitle("OTP Verification")
//                        .setMessage("Enter OTP")
//                        .setView(R.layout.otpfield)
//                        .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                EditText ed = (EditText) findViewById(R.id.otpNumber);
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


            }
        };
        // [END phone_auth_callbacks]

    }

    public void methodGet(View v) {


        // Add the user attributes. Attributes are added as key-value pairs
        // Adding user's given name.
        // Note that the key is "given_name" which is the OIDC claim for given name
        userGivenName = name.getText().toString();
        phoneNumber = phone.getText().toString();
        nameattr = nameatt.getText().toString();
        services = serviceProvided.getText().toString();
        if (userGivenName == null || phoneNumber == null || nameattr == null) {
            Snackbar.make(findViewById(android.R.id.content), "Enter details",
                    Snackbar.LENGTH_SHORT).show();
        }
        else if(typeOfCust.equals("BusinessOwner") && services == null){
            Snackbar.make(findViewById(android.R.id.content), "Enter services",
                    Snackbar.LENGTH_SHORT).show();
        }
        else{
            if (typeOfCust == null || typeOfCust.equals("")) {
                Toast.makeText(this, "type of cust is problem", Toast.LENGTH_LONG).show();
            }
            dialog.setMessage("Hang On...");
            dialog.setCancelable(false);
            dialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReferenceFromUrl("gs://messagingapp-48a08.appspot.com");
            StorageReference filepath = storageRef.child(phoneNumber);

            //uploading
            filepath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(RegisterActivity.this, "upload done", Toast.LENGTH_LONG);
                    storageRef.child(phoneNumber).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profileuri = uri.toString();
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

            cust = new Cust(userGivenName, phoneNumber, profileuri , typeOfCust);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef2 = database.getReference("phoneno");
            Log.e(TAG,"everything is fine");

            myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(phoneNumber)){
                        myRef2.child("dum").removeValue();
                        Log.e(TAG,"removed dum");
                        startPhoneNumberVerification(phoneNumber);
                    }
                    else{
                        myRef2.child("dum").removeValue();
                        Log.e(TAG,"removed dum");
                        dialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "You are already registered",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            myRef2.child("dum").setValue("dum");

        }
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
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            String uid = user.getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String t = "";
                            if (typeOfCust.equalsIgnoreCase("BusinessOwner")) {
                                t = "BusinessOwner";
                            } else {
                                t = "Clients";
                            }
                            Log.e(TAG,"here t is "+t);
                            DatabaseReference myRef = database.getReference(t);
                            DatabaseReference myRef2 = database.getReference("phoneno");
                            if(typeOfCust.equals("BusinessOwner")){
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("username", userGivenName);
                                map.put("phoneno", phoneNumber);
                                map.put("photouri",profileuri);
                                map.put("type",typeOfCust);
                                map.put("servicesProvided",services);
                                myRef.child(uid).setValue(map);
                            }
                            else{
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("username", userGivenName);
                                map.put("phoneno", phoneNumber);
                                map.put("photouri",profileuri);
                                map.put("type",typeOfCust);
                                myRef.child(uid).setValue(map);
                            }
                            myRef2.child(phoneNumber).setValue(typeOfCust+"/"+uid);
                            sharedPreferences = getSharedPreferences("Cust", Context.MODE_PRIVATE);
                            final SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("type",typeOfCust);
                            editor.putString("username",userGivenName);
                            editor.commit();
                            Log.e(TAG,"completed t value");
                            Intent i = new Intent(RegisterActivity.this,MainActivity.class);
                            dialog.dismiss();
                            startActivity(i);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) { //uploading pic
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            //for confirmation of uploading
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("Confirm Send")
                    .setMessage("Set image as Profile picture??")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with sending

                            //data
                            imageuri = data.getData();
                            profileImg.setImageURI(imageuri);

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

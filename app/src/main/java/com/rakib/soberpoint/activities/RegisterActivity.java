package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.utils.OptionsUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    TextInputLayout et_email, et_password, et_name;
    MaterialButton btn_login;
    ProgressBar progress_horizontal;
    private RadioButton radioButton;
    ImageButton back;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        progress_horizontal = findViewById(R.id.progress_horizontal);
        radioGroup = findViewById(R.id.rb_group);
        btn_login.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            progress_horizontal.setVisibility(View.VISIBLE);
            String name = et_name.getEditText().getText().toString();
            if (TextUtils.isEmpty(name)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Please enter name", Toast.LENGTH_LONG).show();
                return;
            }
            String email = et_email.getEditText().getText().toString();
            if (TextUtils.isEmpty(email)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_LONG).show();
                return;
            }
            String password = et_password.getEditText().getText().toString();
            if (TextUtils.isEmpty(password)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.length() <= 8) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Please enter at least 8 character password", Toast.LENGTH_LONG).show();
                return;
            }
            radioButton = (RadioButton) findViewById(selectedId);
            String gender = radioButton.getText().toString();
            if (TextUtils.isEmpty(gender)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Please choose your gender", Toast.LENGTH_LONG).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progress_horizontal.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    Map<String, String> map = new HashMap<>();
                    map.put("name", name);
                    map.put("image", "");
                    map.put("email", email);
                    map.put("bio", "");
                    map.put("ssc", "");
                    map.put("hsc", "");
                    map.put("graduation", "");
                    map.put("from", "");
                    map.put("cover", "");
                    map.put("status", "Offline");
                    String user_id = mAuth.getCurrentUser().getUid();
                    firebaseFirestore.collection("Users").document(user_id).set(map).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Map<String, Object> map1 = new HashMap<>();
                            map1.put("lat", 12.12);
                            map1.put("lng", 12.12);
                            map1.put("id", user_id);
                            firebaseFirestore.collection("Location").document(user_id).set(map1).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    OptionsUtils.savePrefs(RegisterActivity.this, "NAME", name);
                                    OptionsUtils.savePrefs(RegisterActivity.this, "EMAIL", email);
                                    Intent intent = new Intent(RegisterActivity.this, SingleImageUploadActivity.class);
                                    intent.putExtra("title", "profile");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Error to push data", Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(RegisterActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    //Toast.makeText(getContext(), "ERROR, Please try again.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(RegisterActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    progress_horizontal.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();;
                }
            });
        });


        createRequest();


        findViewById(R.id.google_signIn).setOnClickListener(view -> signIn());
        findViewById(R.id.btn_go_sign_up).setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("447924617341-e25sqis2ojkpfotau2egpr3qu6uhmtbh.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                String personName = account.getDisplayName();
                String personEmail = account.getEmail();
                String profileURL = Objects.requireNonNull(account.getPhotoUrl()).toString();
                OptionsUtils.savePrefs(RegisterActivity.this, "NAME", personName);
                OptionsUtils.savePrefs(RegisterActivity.this, "EMAIL", personEmail);
                OptionsUtils.savePrefs(RegisterActivity.this, "IMAGE", profileURL);
                firebaseAuthWithGoogle(account.getIdToken(), personName, personEmail, profileURL);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String name, String email, String photo) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("name", name);
                        map.put("image", photo);
                        map.put("email", email);
                        map.put("bio", "");
                        map.put("ssc", "");
                        map.put("hsc", "");
                        map.put("graduation", "");
                        map.put("from", "");
                        map.put("cover", "");
                        map.put("status", "Offline");
                        String user_id = mAuth.getCurrentUser().getUid();
                        firebaseFirestore.collection("Users").document(user_id).set(map).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Map<String, Object> map1 = new HashMap<>();
                                map1.put("lat", "");
                                map1.put("lng", "");
                                map1.put("id", user_id);
                                firebaseFirestore.collection("Location").document(user_id).set(map1).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        intent.putExtra("type", "home");
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                            }
                        });


                    }else {
                        Toast.makeText(RegisterActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
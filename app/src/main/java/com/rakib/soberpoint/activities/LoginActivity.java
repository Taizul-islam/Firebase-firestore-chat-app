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
import android.widget.TextView;
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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.utils.OptionsUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    TextInputLayout et_email, et_password;
    MaterialButton btn_login;
    ProgressBar progress_horizontal;
    TextView btn_go_sign_up;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        progress_horizontal = findViewById(R.id.progress_horizontal);
        btn_login.setOnClickListener(v -> {
            progress_horizontal.setVisibility(View.VISIBLE);
            String email = et_email.getEditText().getText().toString();
            if (TextUtils.isEmpty(email)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Please enter email", Toast.LENGTH_LONG).show();
                return;
            }
            String password = et_password.getEditText().getText().toString();
            if (TextUtils.isEmpty(password)) {
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_LONG).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progress_horizontal.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("type", "home");
                    startActivity(intent);
                    finish();
                } else {

                    Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("message",task.getException().getMessage());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    progress_horizontal.setVisibility(View.GONE);
                    Log.d("message",e.getMessage());
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        });


        createRequest();


        findViewById(R.id.google_signIn).setOnClickListener(view -> signIn());
        findViewById(R.id.btn_go_sign_up).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                OptionsUtils.savePrefs(LoginActivity.this, "NAME", personName);
                OptionsUtils.savePrefs(LoginActivity.this, "EMAIL", personEmail);
                OptionsUtils.savePrefs(LoginActivity.this, "IMAGE", profileURL);
                firebaseAuthWithGoogle(account.getIdToken(), personName, personEmail, profileURL);
            } catch (ApiException e) {
                Log.d("message",e.getMessage());
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
                        map.put("status", "offline");
                        String user_id = mAuth.getCurrentUser().getUid();
                        firebaseFirestore.collection("Users").document(user_id).set(map).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Map<String, Object> map1 = new HashMap<>();
                                map1.put("lat", 12.12);
                                map1.put("lng", 12.12);
                                map1.put("id", user_id);
                                firebaseFirestore.collection("Location").document(user_id).set(map1).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("type", "home");
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            } else {
                                Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                            }
                        });


                    }else {
                        Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("message",e.getMessage());
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
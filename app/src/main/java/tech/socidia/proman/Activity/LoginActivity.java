package tech.socidia.proman.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tech.socidia.proman.R;

public class LoginActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    EditText nomorTeleponET;
    TextInputLayout nomorTeleponContainer;
    FirebaseAuth mAuth;
    String nomorTelepon;
    Button tombolLogin;
    View activityParent;
    List<EditText> otp = new LinkedList<>();
    int currentOTP = 0;
    View popUpView;
    AdView adView;

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeView();
        showAd();
        if(mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(LoginActivity.this, ProjectList.class);
            startActivity(intent);
            finish();
        }
        tombolLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nomorTeleponET.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Kosong", Toast.LENGTH_SHORT).show();
                }else {
                    if(nomorTeleponET.getText().charAt(0)=='0'){
                        nomorTeleponET.setText(nomorTeleponET.getText().toString().substring(1));
                    }
                    view.setEnabled(false);
                    view.setBackgroundColor(Color.parseColor("#D3D3D3"));
                    tombolLogin.setText("Loading");
                    nomorTelepon = nomorTeleponContainer.getPrefixText().toString() + nomorTeleponET.getText().toString();
                    buildLoginPhone(nomorTelepon);

                }
            }
        });

    }
    protected void showAd(){
        MobileAds.initialize(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    protected void initializeView(){
        nomorTeleponET = findViewById(R.id.nomorTelepon);
        nomorTeleponContainer = findViewById(R.id.nomorTeleponContainer);
        tombolLogin = findViewById(R.id.btnLogin);
        activityParent = findViewById(R.id.activityParent);
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("id");
        firestore = FirebaseFirestore.getInstance();
        adView = findViewById(R.id.loginAdBanner);
    }
    protected void loginNomorTelepon(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firestore.collection("users")
                                    .document(mAuth.getCurrentUser().getUid())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        Map<String,Object> userCreate = new HashMap<>();
                                        try{
                                            String name = task.getResult().getData().get("phoneNum").toString();
                                        }catch (Exception e){
                                            userCreate.put("phoneNum","+62"+nomorTeleponET.getText().toString());
                                        }
                                        try {
                                            String name = task.getResult().getData().get("name").toString();
                                        }catch (Exception e){
                                            userCreate.put("name","");
                                        }try {
                                            String name = task.getResult().getData().get("pictureLink").toString();
                                        }catch (Exception e){
                                            userCreate.put("pictureLink","");
                                        }
                                        if(userCreate.size()!=0){
                                            firestore.collection("users")
                                                    .document(mAuth.getCurrentUser().getUid())
                                                    .set(userCreate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Snackbar.make(popUpView,"LOGIN Sukses",Snackbar.LENGTH_LONG)
                                                                .setAction("OK", new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {

                                                                    }
                                                                })
                                                                .show();
                                                        Intent intent = new Intent(LoginActivity.this,ProjectList.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }else{
                                            Snackbar.make(popUpView,"LOGIN Sukses",Snackbar.LENGTH_LONG)
                                                    .setAction("OK", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {

                                                        }
                                                    })
                                                    .show();
                                            Intent intent = new Intent(LoginActivity.this,ProjectList.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            });

                        } else {
                            Snackbar.make(popUpView,"OTP Salah",Snackbar.LENGTH_LONG)
                                    .setAction("Tutup", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                            .show();
                        }
                        tombolLogin.setEnabled(true);
                        tombolLogin.setBackgroundColor(Color.parseColor("#D3D3D3"));
                        tombolLogin.setText("Login");
                    }
                });

    }
    protected void buildLoginPhone(String nomorTelepon){
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                loginNomorTelepon(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Snackbar.make(activityParent,"Kode Sudah Dikirim",Snackbar.LENGTH_LONG)
                        .setAction("Tutup",null)
                        .show();
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                LayoutInflater inflater = LoginActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.popup_otp_validation, null);
                builder.setView(dialogView);
                popUpView = dialogView;
                TextView phoneNumber = dialogView.findViewById(R.id.konfirmasiNomorTelepon);
                phoneNumber.setText("+62 - "+nomorTeleponET.getText().toString());
                builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tombolLogin.setEnabled(true);
                        tombolLogin.setBackgroundColor(Color.parseColor("#D3D3D3"));
                        tombolLogin.setText("Login");
                    }
                });
                builder.setTitle("Verifikasi OTP");
                otp = new LinkedList<>();
                otp.add(dialogView.findViewById(R.id.otp1));
                otp.add(dialogView.findViewById(R.id.otp2));
                otp.add(dialogView.findViewById(R.id.otp3));
                otp.add(dialogView.findViewById(R.id.otp4));
                otp.add(dialogView.findViewById(R.id.otp5));
                otp.add(dialogView.findViewById(R.id.otp6));
                currentOTP=0;
                otp.get(0).requestFocus();
                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int count) {
                        if(count==0){
                            if(currentOTP!=0){
                                currentOTP-=1;
                            }
                            otp.get(currentOTP).requestFocus();
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(editable.length() >= 1){
                            if(currentOTP!=5){
                                currentOTP+=1;
                            }else{
                                verifyOTP(verificationId);
                            }
                            otp.get(currentOTP).requestFocus();
                        }
                    }
                };
                for (EditText otpNumber:otp) {
                    otpNumber.addTextChangedListener(textWatcher);
                }

                for (EditText otpNumber:otp) {
                    otpNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if(b){
                                for (int i=0;i<otp.size();i++){
                                    if(otp.get(i)==(EditText)view){
                                        currentOTP=i;
                                    }
                                }
                            }
                        }
                    });
                }

                final AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        PhoneAuthOptions loginPhone = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(nomorTelepon)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(loginPhone);
    }
    protected void verifyOTP(String verificationId){
        String otpString = "";
        for (EditText otpNumber:
                otp) {
            otpString += otpNumber.getText().toString();
        }
        loginNomorTelepon(PhoneAuthProvider.getCredential(verificationId,otpString));
    }
}
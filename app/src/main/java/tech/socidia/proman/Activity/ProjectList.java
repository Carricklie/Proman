package tech.socidia.proman.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.socidia.proman.Models.Project;
import tech.socidia.proman.R;

public class ProjectList extends AppCompatActivity {
    public View activityParent;
    FirebaseAuth mAuth;
    View logoutCard;
    FirebaseFirestore firestore;
    TextView profileName;
    ImageView profilePicture;
    ImageView profileImageBox;
    TextView textInBox;
    Uri imageUpload;
    StorageReference storageReference;
    SwipeRefreshLayout refreshLayout;
    public BottomNavigationView bottomNavigationView;
    NavController navController;
    public MenuItem joinProjectMenuItem;
    public MenuItem myProjectMenuItem;
    public MenuItem projectDetailsMenuItem;
    public MenuItem uploadProgressMenuItem;
    public MenuItem uploadImageMenuItem;
    public MenuItem adminMediaReadMenuItem;
    public MenuItem adminMediaViewMenuItem;
    public String currentUserName;
    public Project projectChosen;
    public Map<String,String> adminViewChoose = new HashMap<>();
    AdView adView;
    public boolean refresh=false;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }
    public FirebaseFirestore getFirestore(){
        return firestore;
    }
    public FirebaseAuth getAuth(){
        return mAuth;
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        try {
            initialize();
            showAd();
            refreshLayout.setEnabled(false);
            logoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar snack = Snackbar.make(activityParent, "Konfirmasi Keluar Akun", Snackbar.LENGTH_SHORT);
                    snack.setAction("IYA", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAuth.signOut();
                            Intent intent = new Intent(ProjectList.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    View snackView = snack.getView();
                    FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)snackView.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    snackView.setLayoutParams(params);
                    snack.show();
                }
            });
            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createUploadProfilePictureDialog();
                }
            });
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    checkName();
                    startActivity(getIntent());
                }
            });
            checkName();
            checkProject();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    protected void showAd(){
        MobileAds.initialize(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(data!=null&&data.getData()!=null) {
                textInBox.setVisibility(View.GONE);
                profileImageBox.setVisibility(View.VISIBLE);
                imageUpload = data.getData();
                profileImageBox.setImageURI(data.getData());
            }
        }/*else if(requestCode==1){
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
            if(data!=null&&data.getData()!=null) {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.uploadProgress2);
                UploadProgress fragmentActivity =  (UploadProgress) fragment.getHost();
                fragmentActivity.addVideo(data.getData());
                fragmentActivity.allowUpload();
            }
        }*/
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if(bottomNavigationView.getSelectedItemId()==projectDetailsMenuItem.getItemId()){
                navigateToMyProject();
                return true;
            }
           try{
               if(bottomNavigationView.getSelectedItemId()==adminMediaViewMenuItem.getItemId()){
                   changeToAdminMenu();
                   return true;
               }
              else{
                   return false;
               }
           }catch(Exception e) {
               return false;
           }
        }
        return super.onKeyDown(keyCode, event);
    }
    protected void createUploadProfilePictureDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectList.this);
        LayoutInflater inflater = ProjectList.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_profile_image_upload, null);
        builder.setView(dialogView);
        CardView pictureBox = dialogView.findViewById(R.id.uploadProfilePictureCardView);
        profileImageBox = dialogView.findViewById(R.id.uploadProfilePictureImageView);
        textInBox = dialogView.findViewById(R.id.uploadProfilePictureTextView);
        pictureBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Ubah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final StorageReference reference = storageReference.child("profilePicture."+getExt(imageUpload));
                UploadTask uploadTask = reference.putFile(imageUpload);
                Task<Uri>  uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(task.isSuccessful()){

                        }else {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            Map<String, Object> nestedData = new HashMap<>();

                            firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                                    .update("pictureLink",downloadUri.toString());
                            dialog.dismiss();
                            Snackbar.make(activityParent,"Foto Profil Diubah",Snackbar.LENGTH_SHORT)
                                    .setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).show();
                            checkName();
                        }
                    }
                });
            }
        });
        builder.setTitle("Ubah Foto Profil");
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();

    }
    public BottomNavigationView getBottomNavigationView(){
        return bottomNavigationView;
    }

    protected void initialize(){
        storageReference = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        logoutCard = findViewById(R.id.logoutCardView);
        mAuth.setLanguageCode("id");
        activityParent = findViewById(R.id.activityParent);
        profileName = findViewById(R.id.profileNameTV);
        profilePicture = findViewById(R.id.profilePictureIV);
        refreshLayout = findViewById(R.id.refreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this,R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
        myProjectMenuItem = bottomNavigationView.getMenu().getItem(0);
        joinProjectMenuItem = bottomNavigationView.getMenu().getItem(1);
        projectDetailsMenuItem = bottomNavigationView.getMenu().getItem(2);
        uploadProgressMenuItem = bottomNavigationView.getMenu().getItem(3);
        uploadImageMenuItem = bottomNavigationView.getMenu().getItem(4);
        adView = findViewById(R.id.mainAdBanner);
    }
    protected void checkName(){
        CollectionReference users = firestore.collection("users");
        DocumentReference currentUserDataReference = users.document(mAuth.getCurrentUser().getUid());
        currentUserDataReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot currentUserData = task.getResult();
                    if (currentUserData!=null){
                        currentUserName = currentUserData.getData().get("name").toString();
                        profileName.setText(currentUserName);
                        String link = currentUserData.getData().get("pictureLink").toString();
                        if(!link.isEmpty()){
                            StorageReference httpsReference = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(link);
                            httpsReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Glide
                                                .with(ProjectList.this)
                                                .load(task.getResult())
                                                .centerCrop()
                                                .into(profilePicture);
                                    }else{
                                        Toast.makeText(ProjectList.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        if(currentUserName.isEmpty()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProjectList.this);
                            LayoutInflater inflater = ProjectList.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.pop_up_name, null);
                            builder.setView(dialogView);
                            EditText profileNameEditText = dialogView.findViewById(R.id.profileNameEditText);
                            builder.setPositiveButton("Ubah", null);
                            builder.setTitle("Isi Nama");
                            final AlertDialog dialog = builder.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(profileNameEditText.getText().toString().isEmpty()){
                                                Toast.makeText(ProjectList.this, "Nama Kosong", Toast.LENGTH_SHORT).show();
                                            }else{
                                                firestore.collection("users")
                                                        .document(mAuth.getCurrentUser().getUid())
                                                        .update("name",profileNameEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Snackbar.make(activityParent,"Nama Sudah Diubah ",Snackbar.LENGTH_SHORT)
                                                                    .setAction("Close", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {

                                                                        }
                                                                    }).show();
                                                            dialog.dismiss();
                                                            checkName();
                                                        }else{
                                                            Snackbar.make(activityParent,"Gagal Menyetel Nama",Snackbar.LENGTH_SHORT)
                                                                    .setAction("Close", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {

                                                                        }
                                                                    }).show();
                                                        }

                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
                            dialog.show();

                        }

                    }
                }else{
                    Toast.makeText(ProjectList.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
        refreshLayout.setRefreshing(false);
    }
    protected String getExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public void navigateToMyProject(){
        menuAppear();
        myProjectMenuItem.setEnabled(true);
        myProjectMenuItem.setVisible(true);
        bottomNavigationView.setSelectedItemId(myProjectMenuItem.getItemId());
    }
    public void navigateToProjectDetails(){
        menuAppear();
        projectDetailsMenuItem.setEnabled(true);;
        bottomNavigationView.setSelectedItemId(projectDetailsMenuItem.getItemId());
    }
    public void closeProjectDetails(){
        projectDetailsMenuItem.setEnabled(false);
        projectDetailsMenuItem.setVisible(false);
    }
    public void navigateToUploadProgress(){
        uploadProgressMenuItem.setVisible(true);
        menuGone();
        uploadProgressMenuItem.setEnabled(true);
        bottomNavigationView.setSelectedItemId(uploadProgressMenuItem.getItemId());
    }
    public void navigateToUploadImage(){
        uploadImageMenuItem.setVisible(true);
        menuGone();
        uploadImageMenuItem.setEnabled(true);
        bottomNavigationView.setSelectedItemId(uploadImageMenuItem.getItemId());
    }
    public void menuGone(){
        joinProjectMenuItem.setVisible(false);
        myProjectMenuItem.setVisible(false);
        projectDetailsMenuItem.setVisible(false);

    }
    public void menuAppear(){
        uploadProgressMenuItem.setVisible(false);
        uploadImageMenuItem.setVisible(false);
        joinProjectMenuItem.setVisible(true);
        myProjectMenuItem.setVisible(true);
        joinProjectMenuItem.setEnabled(true);
        myProjectMenuItem.setEnabled(true);
    }
    public void changeToAdminMenu(){
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.menu_admin);
        adminMediaReadMenuItem = bottomNavigationView.getMenu().getItem(0);
        adminMediaViewMenuItem = bottomNavigationView.getMenu().getItem(1);
        bottomNavigationView.setSelectedItemId(adminMediaReadMenuItem.getItemId());
    }
    public void changeToProjectMenu(){
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.menu_project);
        myProjectMenuItem = bottomNavigationView.getMenu().getItem(0);
        joinProjectMenuItem = bottomNavigationView.getMenu().getItem(1);
        projectDetailsMenuItem = bottomNavigationView.getMenu().getItem(2);
        uploadProgressMenuItem = bottomNavigationView.getMenu().getItem(3);
        uploadImageMenuItem = bottomNavigationView.getMenu().getItem(4);
        navigateToProjectDetails();
    }
    public void navigateToAdminDetails(){
        adminMediaViewMenuItem.setEnabled(true);
        adminMediaViewMenuItem.setVisible(true);
        bottomNavigationView.setSelectedItemId(adminMediaViewMenuItem.getItemId());
    }
    public void refreshProjectDetails(){
        navigateToMyProject();
    }
    protected void checkProject(){
        firestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("userProjects")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> listOfProjects = task.getResult().getDocuments();
                    if(!listOfProjects.isEmpty()){
                        navigateToMyProject();
                    }
                }
            }
        });
        firestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("pendingApply")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> listOfProjects = task.getResult().getDocuments();
                    if(!listOfProjects.isEmpty()){
                        navigateToMyProject();
                    }
                }else{
                    Toast.makeText(ProjectList.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
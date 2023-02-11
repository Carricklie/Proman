package tech.socidia.proman.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import tech.socidia.proman.Utils.MediaAdapter;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

public class UploadImgActivity extends AppCompatActivity {
    // Create a storage reference from our app
    private static final int PICK_VIDEO = 1;
    private List<Uri> videoList = new ArrayList<>();
    private List<Uri> pictureList = new ArrayList<>();
    private List<Uri> recyclerList = new ArrayList<>();
    ExoPlayer exoPlayer;
    PlayerView playerView;
    ProgressBar progressUpload;
    MediaController mediaController;
    Button uploadButton;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    RecyclerView mediaList;
    UploadTask uploadTask;
    MediaAdapter mediaAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img);
        initializeApp();
        allowUpload();
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(recyclerList.get(viewHolder.getAdapterPosition())!=null){
                    return makeMovementFlags(0,ItemTouchHelper.DOWN);
                }else{
                    return makeMovementFlags(0,0);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction==ItemTouchHelper.DOWN){
                    videoList.remove(videoList.indexOf(recyclerList.get(viewHolder.getAdapterPosition())));
                    recyclerList.remove(viewHolder.getAdapterPosition());
                    mediaAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                }
            }
        });
        helper.attachToRecyclerView(mediaList);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                UploadVideos(view);
            }
        });
        mediaList.addOnItemTouchListener(new RecyclerItemClickListener(mediaList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                if(recyclerList.get(inPosition)==null){
                    if(ActivityCompat.checkSelfPermission(UploadImgActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(UploadImgActivity.this, new String[]{Manifest.permission.CAMERA},2);
                    }else{
                        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(cameraIntent,3);
                    }
                }else{
                    playerView.setVisibility(View.VISIBLE);
                    exoPlayer.setMediaItem(MediaItem.fromUri(recyclerList.get(inPosition)));
                    exoPlayer.prepare();
                    exoPlayer.play();
                    //video
                }
            }
        }));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==3){
            if(data!=null&&data.getData()!=null) {
                videoList.add(data.getData());
                allowUpload();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UploadVideos(View button){
        for (Uri video:
             recyclerList) {
            if(video==null){
                recyclerList.remove(video);
            }
        }
        if(recyclerList.size()>=1){
            button.setVisibility(View.GONE);
            progressUpload.setVisibility(View.VISIBLE);
            progressUpload.setMax(100);
            progressUpload.setProgress(0,true);
            for (int i=0;i<recyclerList.size();i++){
                final  StorageReference reference = storageReference.child(System.currentTimeMillis()+"."+getExt(recyclerList.get(i)));
                uploadTask = reference.putFile(recyclerList.get(i));
                int finalI = i+1;
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
                            progressUpload.setProgress(finalI /recyclerList.size()*100,true);
                            if(finalI /recyclerList.size()*100==100){
                                videoList = new ArrayList<>();
                                pictureList = new ArrayList<>();
                                progressUpload.setVisibility(View.GONE);
                                button.setVisibility(View.VISIBLE);
                                allowUpload();
                                exoPlayer.removeMediaItem(0);
                                exoPlayer.prepare();
                                Toast.makeText(UploadImgActivity.this, "Video sudah Diupload", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

        }else{
            Toast.makeText(this, "Tidak Ada Video", Toast.LENGTH_SHORT).show();
        }

    }
    protected String getExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    protected void initializeApp(){
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.viewVideo);
        playerView.setVisibility(View.GONE);
        playerView.setPlayer(exoPlayer);
        progressUpload = findViewById(R.id.progressUpload);
        uploadButton = findViewById(R.id.UploadButton);
        mediaController = new MediaController(this);
        mediaList = findViewById(R.id.mediaList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mediaList.setLayoutManager(layoutManager);
        storageReference = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    protected void allowUpload() {
        recyclerList = new ArrayList<>();
        recyclerList.addAll(videoList);
        recyclerList.addAll(pictureList);
        if(recyclerList.isEmpty()){
            uploadButton.setEnabled(false);
        }else{
            uploadButton.setEnabled(true);
        }
        recyclerList.add(null);
        mediaAdapter = new MediaAdapter(this,recyclerList,"video");
        mediaList.setAdapter(mediaAdapter);
    }
}
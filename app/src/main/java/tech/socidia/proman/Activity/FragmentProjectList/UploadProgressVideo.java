package tech.socidia.proman.Activity.FragmentProjectList;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.MediaAdapter;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadProgressVideo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadProgressVideo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UploadProgressVideo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadProgress.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadProgressVideo newInstance(String param1, String param2) {
        UploadProgressVideo fragment = new UploadProgressVideo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_progress_video, container, false);
    }
    ProjectList parent;
    EditText titleUploadEditText;
    EditText descriptionUploadEditText;
    Button backButtonUploadProgress;
    ExoPlayer exoPlayer;
    PlayerView playerView;
    MediaController mediaController;
    StorageReference storageReference;
    RecyclerView videoListRecyclerView;
    UploadTask uploadTask;
    Button uploadProgressButton;
    MediaAdapter mediaAdapter;
    List<Uri> recyclerVideoList;
    List<Uri> videoList;
    ProgressBar progressUpload;
    Button secretTap;
    int uploaded=0;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        allowUpload();
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(recyclerVideoList.get(viewHolder.getAdapterPosition())!=null){
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
                    videoList.remove(videoList.indexOf(recyclerVideoList.get(viewHolder.getAdapterPosition())));
                    recyclerVideoList.remove(viewHolder.getAdapterPosition());
                    mediaAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    allowUpload();
                    playerView.setVisibility(View.GONE);
                }
            }
        });
        helper.attachToRecyclerView(videoListRecyclerView);
        backButtonUploadProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.navigateToProjectDetails();
            }
        });
        videoListRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(videoListRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                if(uploadProgressButton.getVisibility()==View.VISIBLE){
                    if(recyclerVideoList.get(inPosition)==null){
                        if(ActivityCompat.checkSelfPermission(parent, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(parent, new String[]{Manifest.permission.CAMERA},2);
                        }else{
                            Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            startActivityForResult(cameraIntent,1);
                        }
                    }else{
                        playerView.setVisibility(View.VISIBLE);
                        exoPlayer.setMediaItem(MediaItem.fromUri(recyclerVideoList.get(inPosition)));
                        exoPlayer.prepare();
                        exoPlayer.play();
                        //video
                    }
                }
            }
        }));
        uploadProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProgressVideo(v);
            }
        });
        secretTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploaded+=1;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(data!=null&&data.getData()!=null) {
                videoList.add(data.getData());
                allowUpload();
            }
        }
    }

    protected void initialize(View view){
        parent = ((ProjectList)getActivity());
        backButtonUploadProgress = view.findViewById(R.id.backButtonUploadProgress);
        exoPlayer = new ExoPlayer.Builder(parent).build();
        playerView = view.findViewById(R.id.viewProgressVideo);
        playerView.setVisibility(View.GONE);
        playerView.setPlayer(exoPlayer);
        mediaController = new MediaController(parent);
        videoListRecyclerView= view.findViewById(R.id.videoListUploadProgress);
        uploadProgressButton = view.findViewById(R.id.uploadProgressButton);
        LinearLayoutManager layoutManager = new LinearLayoutManager(parent, LinearLayoutManager.HORIZONTAL, false);
        videoListRecyclerView.setLayoutManager(layoutManager);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        storageReference = FirebaseStorage.getInstance().getReference(parent.projectChosen.getProjectID())
                .child(dateFormat.format(Timestamp.now().toDate()))
                .child("progressVideo");
        videoList = new ArrayList<>();
        videoListRecyclerView.getRecycledViewPool().setMaxRecycledViews(0,0);
        progressUpload = view.findViewById(R.id.progressUpload);
        titleUploadEditText = view.findViewById(R.id.titleProgressUploadEditText);
        descriptionUploadEditText = view.findViewById(R.id.progressDescriptionEditText);
        progressUpload.setVisibility(View.GONE);
        secretTap=view.findViewById(R.id.secretTap);
    }
    protected String getExt(Uri uri){
        ContentResolver contentResolver = parent.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    protected void allowUpload() {
        buttonCheckUpload();
        mediaAdapter = new MediaAdapter(parent,recyclerVideoList,"videos");
        videoListRecyclerView.setAdapter(mediaAdapter);
    }
    protected void buttonCheckUpload(){
        recyclerVideoList = new ArrayList<>();
        for (Uri video:
             videoList) {
            if(video==null){
                videoList.remove(video);
            }
        }
        recyclerVideoList.addAll(videoList);
        if(recyclerVideoList.isEmpty()){
            uploadProgressButton.setEnabled(false);
            uploadProgressButton.setBackgroundColor(Color.parseColor("#D3D3D3"));

        }else{
            uploadProgressButton.setEnabled(true);
            uploadProgressButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
        }
        recyclerVideoList.add(null);
    }
    private void uploadProgressVideo(View button){
        uploaded=0;
        playerView.setVisibility(View.GONE);
        if(videoList.size()>=1){
            button.setVisibility(View.GONE);
            progressUpload.setVisibility(View.VISIBLE);
            progressUpload.setMax(100);
            progressUpload.animate();
            progressUpload.setProgress(0,true);
            for (int i=0;i<videoList.size();i++){
                final  StorageReference reference = storageReference.child(Timestamp.now().toDate().toString()+"("+Timestamp.now().getNanoseconds()+")"+"."+getExt(videoList.get(i)));
                uploadTask = reference.putFile(videoList.get(i));

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(task.isSuccessful()){

                        }else {
                            Toast.makeText(parent, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            Map<String,Object> videoData = new HashMap<>();
                            videoData.put("link",downloadUri);
                            videoData.put("title",titleUploadEditText.getText().toString());
                            videoData.put("description",descriptionUploadEditText.getText().toString());
                            Map<String,Object> existdata = new HashMap<>();
                            existdata.put("exist",true);
                            parent.getFirestore().collection("projects")
                                    .document(parent.projectChosen.getProjectID())
                                    .collection("media")
                                    .document(dateFormat.format(Timestamp.now().toDate()))
                                    .set(existdata);
                            parent.getFirestore().collection("projects")
                                    .document(parent.projectChosen.getProjectID())
                                    .collection("media")
                                    .document(dateFormat.format(Timestamp.now().toDate()))
                                    .collection("videos")
                                    .document(parent.getAuth().getCurrentUser().getUid())
                                    .set(existdata);
                            parent.getFirestore().collection("projects")
                                    .document(parent.projectChosen.getProjectID())
                                    .collection("media")
                                    .document(dateFormat.format(Timestamp.now().toDate()))
                                    .collection("videos")
                                    .document(parent.getAuth().getCurrentUser().getUid())
                                    .collection("uploaded")
                                    .add(videoData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        secretTap.performClick();
                                        progressUpload.setProgress((int)(((float)uploaded /(float) videoList.size())*100f),true);
                                        if(uploaded==videoList.size()){
                                            videoList = new ArrayList<>();
                                            progressUpload.setVisibility(View.GONE);
                                            descriptionUploadEditText.setText("");
                                            titleUploadEditText.setText("");
                                            button.setVisibility(View.VISIBLE);
                                            allowUpload();
                                            exoPlayer.removeMediaItem(0);
                                            exoPlayer.prepare();
                                            Toast.makeText(parent, "Video sudah Diupload", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }

        }else{
            Toast.makeText(parent, "Tidak Ada Video", Toast.LENGTH_SHORT).show();
        }

    }
}
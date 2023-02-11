package tech.socidia.proman.Activity.FragmentProjectList;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.MediaAdapter;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadReceiptImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadReceiptImage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UploadReceiptImage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadReceiptImage.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadReceiptImage newInstance(String param1, String param2) {
        UploadReceiptImage fragment = new UploadReceiptImage();
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
        return inflater.inflate(R.layout.fragment_upload_receipt_image, container, false);
    }
    ProjectList parent;
    EditText titleReceiptUploadEditText;
    EditText descriptionReceiptUploadEditText;
    Button backButtonUploadReceipt;
    StorageReference storageReference;
    RecyclerView imageListRecyclerView;
    UploadTask uploadTask;
    Button uploadReceiptButton;
    MediaAdapter mediaAdapter;
    List<Uri> recyclerImageList;
    List<Uri> imageList;
    ProgressBar progressUpload;
    Button secretTap;
    int uploaded=0;
    ImageView imageViewReceipt;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        allowUploadImage();
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(recyclerImageList.get(viewHolder.getAdapterPosition())!=null){
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
                    imageList.remove(imageList.indexOf(recyclerImageList.get(viewHolder.getAdapterPosition())));
                    recyclerImageList.remove(viewHolder.getAdapterPosition());
                    mediaAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    allowUploadImage();
                    imageViewReceipt.setVisibility(View.GONE);
                }
            }
        });
        helper.attachToRecyclerView(imageListRecyclerView);
        backButtonUploadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.navigateToProjectDetails();
            }
        });
        imageListRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(imageListRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition){
                if(uploadReceiptButton.getVisibility()==View.VISIBLE){
                    if(recyclerImageList.get(inPosition)==null){
                        if(ActivityCompat.checkSelfPermission(parent, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(parent, new String[]{Manifest.permission.CAMERA},2);
                        }else{
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 1);
                        }
                    }else{
                        imageViewReceipt.setVisibility(View.VISIBLE);
                        imageViewReceipt.setImageURI(recyclerImageList.get(inPosition));
                    }
                }
            }
        }));
        uploadReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProgressImage(v);
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
            if(data!=null) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                imageList.add(getImageUri(parent,image));
                allowUploadImage();
            }
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    protected void initialize(View view){
        parent = ((ProjectList)getActivity());
        backButtonUploadReceipt = view.findViewById(R.id.backButtonUploadReceipt);
        imageListRecyclerView= view.findViewById(R.id.imageListUploadReceipt);
        uploadReceiptButton = view.findViewById(R.id.uploadReceiptImageButton);
        LinearLayoutManager layoutManager = new LinearLayoutManager(parent, LinearLayoutManager.HORIZONTAL, false);
        imageListRecyclerView.setLayoutManager(layoutManager);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        storageReference = FirebaseStorage.getInstance().getReference(parent.projectChosen.getProjectID())
                .child(dateFormat.format(Timestamp.now().toDate()))
                .child("receiptImage");
        imageList = new ArrayList<>();
        imageListRecyclerView.getRecycledViewPool().setMaxRecycledViews(0,0);
        progressUpload = view.findViewById(R.id.receiptUpload);
        titleReceiptUploadEditText = view.findViewById(R.id.titleReceiptsUploadEditText);
        descriptionReceiptUploadEditText = view.findViewById(R.id.receiptDescriptionEditText);
        progressUpload.setVisibility(View.GONE);
        secretTap=view.findViewById(R.id.secretTapImage);
        imageViewReceipt = view.findViewById(R.id.viewReceiptImageView);
    }
    protected String getExt(Uri uri){
        ContentResolver contentResolver = parent.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    protected void allowUploadImage() {
        try{
            buttonCheckUpload();
            mediaAdapter = new MediaAdapter(parent,recyclerImageList,"images");
            imageListRecyclerView.setAdapter(mediaAdapter);
        }catch (Exception e){
            Toast.makeText(parent, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    protected void buttonCheckUpload(){
        recyclerImageList = new ArrayList<>();
        for (Uri image:
                imageList) {
            if(image==null){
                imageList.remove(image);
            }
        }
        recyclerImageList.addAll(imageList);
        if(recyclerImageList.isEmpty()){
            uploadReceiptButton.setEnabled(false);
            uploadReceiptButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
        }else{
            uploadReceiptButton.setEnabled(true);
            uploadReceiptButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
        }
        if(!recyclerImageList.contains(null)){
            recyclerImageList.add(null);
        }
    }
    private void uploadProgressImage(View button){
        uploaded=0;
        imageViewReceipt.setVisibility(View.GONE);
        if(imageList.size()>=1){
            button.setVisibility(View.GONE);
            progressUpload.setVisibility(View.VISIBLE);
            progressUpload.setMax(100);
            progressUpload.animate();
            progressUpload.setProgress(0,true);
            for (int i=0;i<imageList.size();i++){
                final  StorageReference reference = storageReference.child(Timestamp.now().toDate().toString()+"("+Timestamp.now().getNanoseconds()+")"+"."+getExt(imageList.get(i)));
                uploadTask = reference.putFile(imageList.get(i));

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
                            videoData.put("title",titleReceiptUploadEditText.getText().toString());
                            videoData.put("description",descriptionReceiptUploadEditText.getText().toString());
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
                                    .collection("images")
                                    .document(parent.getAuth().getCurrentUser().getUid())
                                    .set(existdata);
                            parent.getFirestore().collection("projects")
                                    .document(parent.projectChosen.getProjectID())
                                    .collection("media")
                                    .document(dateFormat.format(Timestamp.now().toDate()))
                                    .collection("images")
                                    .document(parent.getAuth().getCurrentUser().getUid())
                                    .collection("uploaded")
                                    .add(videoData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        secretTap.performClick();
                                        progressUpload.setProgress((int)(((float)uploaded /(float) imageList.size())*100f),true);
                                        if(uploaded==imageList.size()){
                                            imageList = new ArrayList<>();
                                            progressUpload.setVisibility(View.GONE);
                                            descriptionReceiptUploadEditText.setText("");
                                            titleReceiptUploadEditText.setText("");
                                            button.setVisibility(View.VISIBLE);
                                            allowUploadImage();
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
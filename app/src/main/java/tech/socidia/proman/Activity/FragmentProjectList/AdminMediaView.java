package tech.socidia.proman.Activity.FragmentProjectList;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
 * Use the {@link AdminMediaView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminMediaView extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminMediaView() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminMediaView.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminMediaView newInstance(String param1, String param2) {
        AdminMediaView fragment = new AdminMediaView();
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
        return inflater.inflate(R.layout.fragment_admin_media_view, container, false);
    }
    ProjectList parent;
    EditText progressTitleAdmin;
    EditText progressDescriptionAdmin;
    RecyclerView mediaRecyclerView;
    ImageView imageViewAdmin;
    ExoPlayer exoPlayer;
    PlayerView playerView;
    MediaController mediaController;
    List<Map<String,String>> mediaListMap;
    List<Uri> mediaList;
    ProgressBar progressBar;
    TextInputLayout progressTitleTIL;
    TextInputLayout progressDescriptionTIL;
    Uri chosenUri;
    boolean zoom = false;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        imageViewAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (zoom == false) {
                    zoom=true;
                    Glide
                            .with(parent)
                            .load(chosenUri)
                            .centerCrop()// the uri you got from Firebase
                            .into(imageViewAdmin);
                }else if(zoom){
                    zoom=false;
                    Glide
                            .with(parent)
                            .load(chosenUri)
                            .into(imageViewAdmin);
                }
            }
        });
    }
    protected  void initialize(View view){
        parent = ((ProjectList)getActivity());
        exoPlayer = new ExoPlayer.Builder(parent).build();
        playerView = view.findViewById(R.id.viewProgressAdminVideo);
        playerView.setVisibility(View.GONE);
        playerView.setPlayer(exoPlayer);
        mediaController = new MediaController(parent);
        mediaRecyclerView = view.findViewById(R.id.adminMediaRecyclerView);
        progressTitleAdmin = view.findViewById(R.id.titleProgressAdminViewEditText);
        progressDescriptionAdmin = view.findViewById(R.id.progressDescriptionAdminViewEditText);
        imageViewAdmin = view.findViewById(R.id.viewReceiptAdminViewIV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(parent, LinearLayoutManager.HORIZONTAL, false);
        mediaRecyclerView.setLayoutManager(layoutManager);
        progressTitleAdmin.setKeyListener(null);
        progressDescriptionAdmin.setKeyListener(null);
        progressTitleTIL = view.findViewById(R.id.titleProgressTextInputlayoutMediaView);
        progressDescriptionTIL = view.findViewById(R.id.progressDescriptionTextInputLayoutMediaView);
        progressBar = view.findViewById(R.id.progressBarMediaView);
        progressBar.animate();
        mediaListMap = new ArrayList<>();
        mediaList = new ArrayList<>();
        parent.getFirestore().collection("projects")
                .document(parent.projectChosen.getProjectID().toString())
                .collection("media")
                .document(parent.adminViewChoose.get("date"))
                .collection(parent.adminViewChoose.get("type"))
                .document(parent.adminViewChoose.get("id"))
                .collection("uploaded")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot upload:
                         task.getResult().getDocuments()) {
                        Map<String,String> newMap = new HashMap<>();
                        newMap.put("description",upload.getData().get("description").toString());
                        newMap.put("link",upload.getData().get("link").toString());
                        newMap.put("title",upload.getData().get("title").toString());
                        mediaListMap.add(newMap);
                        Uri uri =  Uri.parse(upload.getData().get("link").toString());
                        mediaList.add(uri);
                    }
                    MediaAdapter mediaAdapter = new MediaAdapter(parent,mediaList,parent.adminViewChoose.get("type"));
                    mediaRecyclerView.setAdapter(mediaAdapter);
                    mediaRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mediaRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View inView, int inPosition) {
                            progressTitleAdmin.setText(mediaListMap.get(inPosition).get("title"));
                            progressDescriptionAdmin.setText(mediaListMap.get(inPosition).get("description"));
                            if(parent.adminViewChoose.get("type").equals("images")){
                                imageViewAdmin.setVisibility(View.VISIBLE);
                                playerView.setVisibility(View.GONE);
                                chosenUri = mediaList.get(inPosition);
                                Glide
                                        .with(parent)
                                        .load(mediaList.get(inPosition)) // the uri you got from Firebase
                                        .into(imageViewAdmin);
                            }else if(parent.adminViewChoose.get("type").equals("videos")){
                                imageViewAdmin.setVisibility(View.GONE);
                                playerView.setVisibility(View.VISIBLE);
                                exoPlayer.setMediaItem(MediaItem.fromUri(mediaList.get(inPosition)));
                                exoPlayer.prepare();
                                exoPlayer.play();
                            }
                        }
                    }));
                    mediaRecyclerView.setVisibility(View.VISIBLE);
                    progressDescriptionTIL.setVisibility(View.VISIBLE);
                    progressTitleTIL.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
}
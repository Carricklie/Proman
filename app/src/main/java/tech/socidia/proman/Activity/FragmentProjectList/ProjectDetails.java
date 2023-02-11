package tech.socidia.proman.Activity.FragmentProjectList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.Models.Member;
import tech.socidia.proman.Models.PendingMember;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.MemberAdapter;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectDetails extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProjectDetails() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProjectDetails.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectDetails newInstance(String param1, String param2) {
        ProjectDetails fragment = new ProjectDetails();
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
        return inflater.inflate(R.layout.fragment_project_details, container, false);
    }
    ProjectList parent;
    TextView projectTitleTV;
    TextView projectStartDateTV;
    TextView projectInitialBudgetTV;
    TextView projectDurationTV;
    Button memberButton;
    Button pendingMemberButton;
    Button uploadProgressButton;
    Button adminButton;
    Button uploadReceipt;
    RecyclerView memberRecyclerView;
    Member thisUserMember;
    RecyclerItemClickListener recyclerItemClickListenerPending;
    RecyclerItemClickListener getRecyclerItemClickListenerMember;
    LinearLayout linearLayoutMember;
    FloatingActionButton refreshButton;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        setUI(view);
        setThisUserMember();
        pendingMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingMemberButton.setEnabled(false);
                pendingMemberButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                memberButton.setEnabled(true);
                memberButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                setRecyclerViewPendingMember();
            }
        });
        memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingMemberButton.setEnabled(true);
                pendingMemberButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                memberButton.setEnabled(false);
                memberButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                setRecyclerViewMember();
            }
        });
        uploadProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.navigateToUploadProgress();
            }
        });
        uploadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.navigateToUploadImage();
            }
        });
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               parent.changeToAdminMenu();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.refresh=true;
                parent.refreshProjectDetails();
            }
        });
    }
    protected void initialize(View view){
        projectTitleTV = view.findViewById(R.id.projectTitleTVDetails);
        projectStartDateTV = view.findViewById(R.id.startDateTVDetails);
        projectInitialBudgetTV = view.findViewById(R.id.initialBudgetTVDetails);
        projectDurationTV = view.findViewById(R.id.projectDurationTVDetails);
        memberButton = view.findViewById(R.id.memberBtn);
        pendingMemberButton = view.findViewById(R.id.pendingMemberBtn);
        uploadProgressButton = view.findViewById(R.id.uploadBtn);
        adminButton = view.findViewById(R.id.adminBtn);
        memberRecyclerView = view.findViewById(R.id.recyclerViewMembers);
        parent = ((ProjectList)getActivity());
        uploadReceipt = view.findViewById(R.id.uploadReceiptBtn);
        linearLayoutMember = view.findViewById(R.id.linearLayoutMember);
        refreshButton = view.findViewById(R.id.refreshButton);
    }
    protected void setThisUserMember(){
        parent.getFirestore().collection("projects")
                .document(parent.projectChosen.getProjectID())
                .collection("members")
                .document(parent.getAuth().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    thisUserMember = new Member(task.getResult().getData().get("name").toString(),
                            task.getResult().getId().toString(),
                            task.getResult().getData().get("role").toString(),
                            task.getResult().getData().get("dateJoined").toString());

                    if(thisUserMember.getRole().equals("Pemilik")){
                        linearLayoutMember.setVisibility(View.VISIBLE);
                        adminButton.setVisibility(View.VISIBLE);
                    }else if(thisUserMember.getRole().equals("Admin")){
                        linearLayoutMember.setVisibility(View.GONE);
                        adminButton.setVisibility(View.VISIBLE);
                    }else{
                        linearLayoutMember.setVisibility(View.GONE);
                        adminButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    protected void setUI(View view){
        projectTitleTV.setText(parent.projectChosen.getProjectTitle());
        projectStartDateTV.setText("Mulai : "+parent.projectChosen.getStartDateString());
        projectInitialBudgetTV.setText(String.format("Modal Awal : Rp %.0f",parent.projectChosen.getInitialBudget()));
        projectDurationTV.setText(String.format("Durasi Proyek : %.0f bulan",parent.projectChosen.getProjectDuration()));
        memberRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerItemClickListenerPending = new RecyclerItemClickListener(memberRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                LayoutInflater inflater = parent.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.popup_pending_member, null);
                builder.setView(dialogView);
                ImageView profilePictureIV = dialogView.findViewById(R.id.profilePictureIVPopup);
                TextView profileNameTV = dialogView.findViewById(R.id.profileNameTvPopup);
                TextView profilePhoneNumTV = dialogView.findViewById(R.id.profilePhoneNumberTvPopup);
                EditText descriptionApplyEditText = dialogView.findViewById(R.id.descriptionPendingEditTextPopUp);
                Button acceptBtn = dialogView.findViewById(R.id.acceptPendingMemberBtn);
                Button denyBtn = dialogView.findViewById(R.id.deenyPendingMemberBtn);
                String theUserID = parent.projectChosen.getPendingMemberList().get(inPosition).getMemberID();
                PendingMember currentPendingMember = parent.projectChosen.getPendingMemberList().get(inPosition);
                descriptionApplyEditText.setKeyListener(null);
                profileNameTV.setText(currentPendingMember.getName());
                descriptionApplyEditText.setText(currentPendingMember.getApplyDescription());
                FirebaseFirestore.getInstance().collection("users")
                        .document(currentPendingMember.getMemberID())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            profilePhoneNumTV.setText(task.getResult().getData().get("phoneNum").toString());
                            String link = task.getResult().getData().get("pictureLink").toString();
                            if(!link.isEmpty()){
                                Uri uri = Uri.parse(link);
                                StorageReference httpsReference = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(link);
                                Glide
                                        .with(parent)
                                        .load(link)
                                        .centerCrop()
                                        .into(profilePictureIV);

                            }
                        }

                    }
                });
                builder.setTitle("Profil");
                builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acceptPending(currentPendingMember,inPosition);
                        dialog.dismiss();
                    }
                });
                denyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        denyPending(currentPendingMember,inPosition);
                        dialog.dismiss();
                    }
                });
            }
        });
        getRecyclerItemClickListenerMember = new RecyclerItemClickListener(memberRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                if(parent.projectChosen.getMemberList().get(inPosition).getRole().equals("Pekerja")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                    builder.setTitle("Promosi "+parent.projectChosen.getMemberList().get(inPosition).getName()+" ke Admin?");
                    builder.setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.getFirestore().collection("projects")
                                    .document(parent.projectChosen.getProjectID())
                                    .collection("members")
                                    .document(parent.projectChosen.getMemberList().get(inPosition).getMemberID())
                                    .update("role","Admin").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    parent.getFirestore().collection("users")
                                            .document(parent.projectChosen.getMemberList().get(inPosition).getMemberID())
                                            .collection("userProjects")
                                            .document(parent.projectChosen.getProjectID())
                                            .update("role","Admin").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            parent.projectChosen.getMemberList().get(inPosition).setRole("Admin");
                                            setRecyclerViewMember();
                                            Snackbar.make(parent.activityParent,"Sukses promosi",Snackbar.LENGTH_LONG)
                                                    .setAction("Tutup", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {

                                                        }
                                                    }).show();
                                        }
                                    });
                                }
                            });

                        }
                    });
                    builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        setRecyclerViewMember();
    }
    protected void setRecyclerViewMember(){
        MemberAdapter memberAdapter = new MemberAdapter(getContext());
        memberAdapter.assignMemberAdapter(parent.projectChosen.getMemberList());
        memberRecyclerView.setAdapter(memberAdapter);
        memberRecyclerView.removeOnItemTouchListener(recyclerItemClickListenerPending);
        memberRecyclerView.addOnItemTouchListener(getRecyclerItemClickListenerMember);
    }
    protected void setRecyclerViewPendingMember(){
        MemberAdapter pendingMemberAdapter = new MemberAdapter(getContext());
        pendingMemberAdapter.assignPendingMemberAdapter(parent.projectChosen.getPendingMemberList());
        memberRecyclerView.setAdapter(pendingMemberAdapter);
        memberRecyclerView.removeOnItemTouchListener(getRecyclerItemClickListenerMember);
        memberRecyclerView.addOnItemTouchListener(recyclerItemClickListenerPending);
    }
    protected void acceptPending(PendingMember currentPendingMember,int position){
        parent.getFirestore().collection("projects")
                .document(parent.projectChosen.getProjectID())
                .collection("pendingMembers")
                .document(currentPendingMember.getMemberID())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    parent.getFirestore().collection("users")
                            .document(currentPendingMember.getMemberID())
                            .collection("pendingApply")
                            .document(parent.projectChosen.getProjectID())
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Member newMember = new Member(currentPendingMember.getName(),
                                        currentPendingMember.getMemberID(),
                                        "Pekerja", Timestamp.now());
                                Map<String,Object> memberData = new HashMap<>();
                                memberData.put("dateJoined",newMember.getDateJoinedString());
                                memberData.put("name",newMember.getName());
                                memberData.put("role",newMember.getRole());
                                parent.getFirestore().collection("projects")
                                        .document(parent.projectChosen.getProjectID())
                                        .collection("members")
                                        .document(currentPendingMember.getMemberID())
                                        .set(memberData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            parent.getFirestore().collection("users")
                                                    .document(newMember.getMemberID())
                                                    .collection("userProjects")
                                                    .document(parent.projectChosen.getProjectID())
                                                    .set(memberData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        PendingMember pendingMember = parent.projectChosen.getPendingMemberList().remove(position);
                                                        parent.projectChosen.getMemberList().add(newMember);
                                                        setRecyclerViewPendingMember();
                                                        Snackbar.make(parent.activityParent,
                                                                "Sukses menerima permintaan",
                                                                Snackbar.LENGTH_LONG)
                                                                .setAction("Tutup", new View.OnClickListener() {
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
                        }
                    });
                }
            }
        });
    }
    protected void denyPending(PendingMember currentPendingMember,int position){
        parent.getFirestore().collection("projects")
                .document(parent.projectChosen.getProjectID())
                .collection("pendingMembers")
                .document(currentPendingMember.getMemberID())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    parent.getFirestore().collection("users")
                            .document(currentPendingMember.getMemberID())
                            .collection("pendingApply")
                            .document(parent.projectChosen.getProjectID())
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                parent.projectChosen.getPendingMemberList().remove(position);
                                setRecyclerViewPendingMember();
                                Snackbar.make(parent.activityParent,"Sukses menolak permintaan",Snackbar.LENGTH_LONG)
                                        .setAction("Tutup", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(parent, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
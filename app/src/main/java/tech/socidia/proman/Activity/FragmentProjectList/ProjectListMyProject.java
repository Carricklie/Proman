package tech.socidia.proman.Activity.FragmentProjectList;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import tech.socidia.proman.Activity.LoginActivity;
import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.Activity.SplashScreen;
import tech.socidia.proman.Models.Member;
import tech.socidia.proman.Models.PendingMember;
import tech.socidia.proman.Models.Project;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.ProjectListAdapter;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectListMyProject#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectListMyProject extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProjectListMyProject() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProjectListMyProject.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectListMyProject newInstance(String param1, String param2) {
        ProjectListMyProject fragment = new ProjectListMyProject();
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
        return inflater.inflate(R.layout.fragment_project_list_my_project, container, false);

    }
    ProjectList parent;
    RecyclerView recyclerViewProjectList;
    RadioButton checkBoxActive;
    RadioButton  checkBoxPending;
    RadioButton  checkBoxExpired;
    List<String> userProjectListID;
    List<Project> userProjectList;
    List<Project> projectList;
    List<Project> filtered;
    Button hiddenButton;
    ProgressBar progressBar;

    int counter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        parent.getFirestore().collection("users")
                .document(parent.getAuth().getCurrentUser().getUid())
                .collection("userProjects")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot document:
                            task.getResult().getDocuments()) {
                        userProjectListID.add(document.getId());
                    }
                    cacheAndLoadAllProject();
                }
            }
        });
        checkBoxActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyActive();
            }
        });
        checkBoxPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyPending();
            }
        });
        hiddenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterPlus();
            }
        });

    }
    protected void initialize(View view){
        recyclerViewProjectList = view.findViewById(R.id.recyclerViewProject);
        checkBoxActive = view.findViewById(R.id.activeCheckbox);
        checkBoxPending = view.findViewById(R.id.pendingCheckbox);
        checkBoxExpired = view.findViewById(R.id.expiredCheckbox);
        parent = ((ProjectList)getActivity());
        userProjectListID = new ArrayList<>();
        projectList = new ArrayList<>();
        userProjectList = new ArrayList<>();
        filtered = new ArrayList<>();
        hiddenButton = view.findViewById(R.id.hiddenButtonMyProject);
        progressBar = view.findViewById(R.id.loadingProjectProgressBar);
        progressBar.animate();

    }
    protected void counterPlus(){
        counter++;
    }
    protected void cacheAndLoadAllProject(){
        counter=0;
        parent.getFirestore().collection("projects")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    final int theLength = task.getResult().getDocuments().size();
                    for (DocumentSnapshot documentProject :
                            task.getResult().getDocuments()) {

                        Project newProject = new Project(documentProject.getData().get("projectTitle").toString(),
                                documentProject.getData().get("startDate").toString(),
                                (Double) documentProject.getData().get("projectDuration"),
                                (Double) documentProject.getData().get("initialBudget"));
                        newProject.setProjectID(documentProject.getId());
                        parent.getFirestore().collection("projects")
                                .document(documentProject.getId())
                                .collection("members")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    List<Member> memberList = new ArrayList<>();
                                    for (DocumentSnapshot documentMember:
                                            task.getResult().getDocuments()) {
                                        Member member = new Member(documentMember.getData().get("name").toString(),
                                                documentMember.getId(),documentMember.getData().get("role").toString(),
                                                documentMember.getData().get("dateJoined").toString());
                                        memberList.add(member);
                                    }
                                    newProject.setMemberList(memberList);
                                    parent.getFirestore().collection("projects")
                                            .document(documentProject.getId())
                                            .collection("pendingMembers")
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){
                                                List<PendingMember> pendingMemberList = new ArrayList<>();
                                                for (DocumentSnapshot documentMember:
                                                        task.getResult().getDocuments()) {
                                                    PendingMember member = new PendingMember(documentMember.getData().get("name").toString(),
                                                            documentMember.getId(),documentMember.getData().get("role").toString(),
                                                            documentMember.getData().get("description").toString());
                                                    pendingMemberList.add(member);
                                                }
                                                newProject.setPendingMemberList(pendingMemberList);
                                                projectList.add(newProject);
                                                hiddenButton.performClick();
                                                if(counter == theLength){
                                                    onlyActive();
                                                    if(parent.projectChosen!=null&&parent.refresh==true){
                                                        parent.refresh=false;
                                                        for (Project project:
                                                                projectList) {
                                                            if(parent.projectChosen.getProjectID().equals(project.getProjectID())){
                                                                parent.projectChosen = project;
                                                                parent.navigateToProjectDetails();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            }
        });
    }
    protected void onlyPending(){
        userProjectList = new ArrayList<>();
        for (Project project :
                projectList) {
            for (Member member :
                    project.getMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    break;
                }
            }
            for (PendingMember member :
                    project.getPendingMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    userProjectList.add(project);
                    break;
                }
            }
        }
        ProjectListAdapter projectListAdapter = new ProjectListAdapter(userProjectList,parent.getAuth().getCurrentUser().getUid());
        recyclerViewProjectList.setAdapter(projectListAdapter);
        recyclerViewProjectList.addOnItemTouchListener(new RecyclerItemClickListener(recyclerViewProjectList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                for (Member member:
                        userProjectList.get(inPosition).getMemberList()) {
                    if(member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())){
                        parent.projectChosen = userProjectList.get(inPosition);
                        parent.navigateToProjectDetails();
                    }
                }
            }
        }));
    }
    protected void onlyActive(){
        recyclerViewProjectList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        userProjectList = new ArrayList<>();
        for (Project project :
                projectList) {
            for (Member member :
                    project.getMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    userProjectList.add(project);
                    break;
                }
            }
            for (PendingMember member :
                    project.getPendingMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    break;
                }
            }
        }
        ProjectListAdapter projectListAdapter = new ProjectListAdapter(userProjectList,parent.getAuth().getCurrentUser().getUid());
        recyclerViewProjectList.setAdapter(projectListAdapter);
        recyclerViewProjectList.addOnItemTouchListener(new RecyclerItemClickListener(recyclerViewProjectList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                for (Member member:
                        userProjectList.get(inPosition).getMemberList()) {
                    if(member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())){
                        parent.projectChosen = userProjectList.get(inPosition);
                        parent.navigateToProjectDetails();
                    }
                }
            }
        }));

    }
    protected void onlyActivePending(){
        userProjectList = new ArrayList<>();
        for (Project project :
                projectList) {
            for (Member member :
                    project.getMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    userProjectList.add(project);
                    break;
                }
            }
            for (PendingMember member :
                    project.getPendingMemberList()) {
                if (member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())) {
                    userProjectList.add(project);
                    break;
                }
            }
        }
        ProjectListAdapter projectListAdapter = new ProjectListAdapter(userProjectList,parent.getAuth().getCurrentUser().getUid());
        recyclerViewProjectList.setAdapter(projectListAdapter);
        recyclerViewProjectList.addOnItemTouchListener(new RecyclerItemClickListener(recyclerViewProjectList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View inView, int inPosition) {
                for (Member member:
                        userProjectList.get(inPosition).getMemberList()) {
                    if(member.getMemberID().equals(parent.getAuth().getCurrentUser().getUid())){
                        parent.projectChosen = userProjectList.get(inPosition);
                        parent.navigateToProjectDetails();
                    }
                }
            }
        }));
    }
}
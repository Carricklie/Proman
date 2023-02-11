package tech.socidia.proman.Activity.FragmentProjectList;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.Models.Member;
import tech.socidia.proman.Models.PendingMember;
import tech.socidia.proman.Models.Project;
import tech.socidia.proman.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectListJoin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectListJoin extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProjectListJoin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProjectListJoin.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectListJoin newInstance(String param1, String param2) {
        ProjectListJoin fragment = new ProjectListJoin();
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
    ProjectList parent;
    TextInputEditText applyCodeEditText;
    TextInputLayout applyDescriptionTIL;
    TextInputEditText applyDescriptionEditText;
    Button applyJoin;
    Button createProjectButton;
    LinearLayout createProjectGroup;
    TextInputEditText projectNameEditText;
    TextInputLayout projectStartDateTextInputLayout;
    TextInputEditText projectStartDateEditText;
    TextInputEditText projectDurationEditText;
    TextInputEditText initialBudgetEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_project_list_join, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        applyCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==6){
                    enableApply();
                }else{
                    disableApply();
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        applyCodeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    resetCreateProject();
                }
            }
        });
        applyJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(applyCodeEditText.getText().toString().isEmpty()
                        ||applyDescriptionEditText.getText().toString().isEmpty()){
                    Snackbar.make(parent.activityParent,"Isi semua kolom",Snackbar.LENGTH_LONG)
                            .setAction("Tutup", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                }else{
                    v.setEnabled(false);
                    applyJoinProject();
                }
            }
        });
        createProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(projectNameEditText.getText().toString().isEmpty()
                        ||projectStartDateEditText.getText().toString().isEmpty()
                        ||projectDurationEditText.getText().toString().isEmpty()
                        ||initialBudgetEditText.getText().toString().isEmpty()) {
                    if(createProjectGroup.getVisibility()==View.VISIBLE){
                        Snackbar.make(parent.activityParent,"Isi semua kolom",Snackbar.LENGTH_LONG)
                                .setAction("Tutup", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                    }
                    projectNameEditText.requestFocus();
                    resetJoinProject();
                    enableCreateProject();
                }else{
                    v.setEnabled(false);
                    createProject();
                }
            }
        });
        projectStartDateTextInputLayout.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        projectStartDateEditText.setText(String.format("%02d-%02d-%02d",dayOfMonth,month+1,year));
                    }
                },year,month,day);
                datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePicker.show();
            }
        });
    }

    protected void initialize(View view){
        applyCodeEditText = view.findViewById(R.id.projectCodeEditText);
        applyDescriptionTIL = view.findViewById(R.id.applyDescriptionTextInputLayout);
        applyDescriptionEditText = view.findViewById(R.id.applyDescriptionEditText);
        applyJoin = view.findViewById(R.id.applyProjectButton);
        createProjectButton = view.findViewById(R.id.createProject);
        createProjectGroup = view.findViewById(R.id.createProjectGroup);
        projectNameEditText = view.findViewById(R.id.projectTitleEditText);
        projectStartDateTextInputLayout = view.findViewById(R.id.startDateTextInputLayout);
        projectStartDateEditText = view.findViewById(R.id.projectStartDateEditText);
        projectStartDateEditText.setKeyListener(null);
        projectDurationEditText = view.findViewById(R.id.projectDurationEditText);
        initialBudgetEditText = view.findViewById(R.id.initialBudgetEditText);
        parent = ((ProjectList)getActivity());
    }
    protected void disableApply(){
        applyDescriptionTIL.setVisibility(View.GONE);
        applyJoin.setEnabled(false);
        applyJoin.setBackgroundColor(Color.parseColor("#D3D3D3"));
    }
    protected void enableApply(){
        applyDescriptionTIL.setVisibility(View.VISIBLE);
        applyJoin.setEnabled(true);
        applyJoin.setBackgroundColor(Color.parseColor("#FFBB86FC"));
    }
    protected void resetJoinProject(){
        disableApply();
        applyCodeEditText.setText("");
        applyDescriptionEditText.setText("");
    }
    protected void enableCreateProject(){
        createProjectGroup.setVisibility(View.VISIBLE);
    }
    protected void disableCreateProject(){
        createProjectGroup.setVisibility(View.GONE);
    }
    protected void resetCreateProject(){
        disableCreateProject();
        projectNameEditText.setText("");
        projectStartDateEditText.setText("");
        projectDurationEditText.setText("");
        initialBudgetEditText.setText("");
    }
    protected void createProject(){
        Project newProject = new Project(projectNameEditText.getText().toString(),
                projectStartDateEditText.getText().toString(),
                Double.parseDouble(projectDurationEditText.getText().toString()),
                Double.parseDouble(initialBudgetEditText.getText().toString()));
        Map<String,Object> newProjectData = new HashMap<>();
        newProjectData.put("projectTitle",newProject.getProjectTitle());
        newProjectData.put("startDate",newProject.getStartDateString());
        newProjectData.put("projectDuration",newProject.getProjectDuration());
        newProjectData.put("initialBudget",newProject.getInitialBudget());
        String randomID = String.format("%06d",new Random().nextInt(1000000));
        parent.getFirestore().collection("projects")
                .document(randomID)
                .set(newProjectData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Member member = new Member(parent.currentUserName,
                                    parent.getAuth().getCurrentUser().getUid()
                            ,"Pemilik",Timestamp.now());
                            Map<String,Object> userRole = new HashMap<>();
                            userRole.put("name",member.getName());
                            userRole.put("role",member.getRole());
                            userRole.put("dateJoined",member.getDateJoinedString());
                            parent.getFirestore().collection("users")
                                    .document(parent.getAuth().getCurrentUser().getUid())
                                    .collection("userProjects")
                                    .document(randomID)
                                    .set(userRole).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        parent.getFirestore().collection("projects")
                                                .document(randomID)
                                                .collection("members")
                                                .document(parent.getAuth().getCurrentUser().getUid())
                                                .set(userRole).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    resetCreateProject();
                                                    Snackbar.make(parent.activityParent,"Proyek Sukses Dibuat",Snackbar.LENGTH_LONG)
                                                            .setAction("OK", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).show();
                                                    parent.navigateToMyProject();
                                                }else{
                                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            createProject();
                        }
                        createProjectButton.setEnabled(true);
                    }
                });
    }
    protected void applyJoinProject(){
        parent.getFirestore().collection("projects")
                .document(applyCodeEditText.getText().toString())
                .collection("members")
                .document(parent.getAuth().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    try{
                        String test = task.getResult().getData().get("role").toString();
                        Snackbar.make(parent.activityParent,"Ada sudah berada di grup ini",Snackbar.LENGTH_LONG)
                                .setAction("Tutup", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                    }catch (Exception e){
                        PendingMember pendingMember = new PendingMember(parent.currentUserName,
                                parent.getAuth().getCurrentUser().getUid(),
                                "Pekerja",applyDescriptionEditText.getText().toString());
                        Map<String,Object> applyMember = new HashMap<>();
                        applyMember.put("dateJoined","");
                        applyMember.put("description",pendingMember.getApplyDescription());
                        applyMember.put("name",pendingMember.getName());
                        applyMember.put("role",pendingMember.getRole());
                        parent.getFirestore().collection("projects")
                                .document(applyCodeEditText.getText().toString())
                                .collection("pendingMembers")
                                .document(parent.getAuth().getCurrentUser().getUid())
                                .set(applyMember).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    parent.getFirestore().collection("users")
                                            .document(parent.getAuth().getCurrentUser().getUid())
                                            .collection("pendingApply")
                                            .document(applyCodeEditText.getText().toString())
                                            .set(applyMember).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                resetJoinProject();
                                                Snackbar.make(parent.activityParent,"Sukses Meminta",Snackbar.LENGTH_LONG)
                                                        .setAction("Tutup", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                            }
                                                        }).show();
                                                parent.navigateToMyProject();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                applyJoin.setEnabled(true);
            }
        });
    }
}
package tech.socidia.proman.Activity.FragmentProjectList;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.Models.Member;
import tech.socidia.proman.R;
import tech.socidia.proman.Utils.MediaAdapter;
import tech.socidia.proman.Utils.MemberAdapter;
import tech.socidia.proman.Utils.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminMediaRead#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminMediaRead extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminMediaRead() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminMediaRead.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminMediaRead newInstance(String param1, String param2) {
        AdminMediaRead fragment = new AdminMediaRead();
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
        return inflater.inflate(R.layout.fragment_admin_media_read, container, false);
    }
    ProjectList parent;
    Button backButtonAdminMenu;
    LinearLayout adminChooseLinearLayout;
    Button videoProgressButton;
    Button imageReceiptButton;
    TextInputLayout chooseDateAdminTextInputLayout;
    EditText chooseDateAdminEditText;
    RecyclerView recyclerViewMemberMedia;
    List<Member> memberTodayList;
    ProgressBar progressBar;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        backButtonAdminMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.changeToProjectMenu();
            }
        });
        chooseDateAdminTextInputLayout.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        chooseDateAdminEditText.setText(String.format("%02d-%02d-%02d",dayOfMonth,month+1,year));
                        if(!chooseDateAdminEditText.getText().toString().isEmpty()){
                            adminChooseLinearLayout.setVisibility(View.VISIBLE);
                            imageReceiptButton.setEnabled(true);
                            videoProgressButton.setEnabled(true);
                            videoProgressButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                            imageReceiptButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                        }
                    }
                },year,month,day);
                datePicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
                datePicker.show();
            }
        });
        videoProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                v.setBackgroundColor(Color.parseColor("#D3D3D3"));
                imageReceiptButton.setEnabled(true);
                imageReceiptButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                findImagesFrom("videos");
            }
        });
        imageReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                v.setEnabled(false);
                v.setBackgroundColor(Color.parseColor("#D3D3D3"));
                videoProgressButton.setEnabled(true);
                videoProgressButton.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                findImagesFrom("images");
            }
        });
    }
    protected  void initialize(View view){
        parent = ((ProjectList)getActivity());
        backButtonAdminMenu = view.findViewById(R.id.backButtonAdminMenu);
        adminChooseLinearLayout = view.findViewById(R.id.linearLayoutAdminChoice);
        videoProgressButton = view.findViewById(R.id.videoProgressButton);
        imageReceiptButton = view.findViewById(R.id.imageReceiptButton);
        chooseDateAdminTextInputLayout = view.findViewById(R.id.chooseDateAdminTextInputLayout);
        chooseDateAdminEditText = view.findViewById(R.id.chooseDateAdminEditText);
        recyclerViewMemberMedia = view.findViewById(R.id.recyclerViewMembersAdminView);
        adminChooseLinearLayout.setVisibility(View.GONE);
        progressBar = view.findViewById(R.id.progressBarMemberSubmit);
        progressBar.animate();
        progressBar.setVisibility(View.GONE);
        memberTodayList = new ArrayList<>();
        chooseDateAdminEditText.setKeyListener(null);
        if(!chooseDateAdminEditText.getText().toString().isEmpty()){
            adminChooseLinearLayout.setVisibility(View.VISIBLE);
        }
    }
    protected void findImagesFrom(String type){
        progressBar.setVisibility(View.VISIBLE);
        memberTodayList = new ArrayList<>();
        parent.getFirestore().collection("projects")
                .document(parent.projectChosen.getProjectID().toString())
                .collection("media")
                .document(chooseDateAdminEditText.getText().toString())
                .collection(type)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    recyclerViewMemberMedia.setVisibility(View.VISIBLE);
                    for (DocumentSnapshot taskItem:
                         task.getResult()) {
                        for (Member member:
                                parent.projectChosen.getMemberList()) {
                            if(taskItem.getId().equals(member.getMemberID())){
                                memberTodayList.add(member);
                                break;
                            }
                        }
                    }
                    MemberAdapter memberAdapter = new MemberAdapter(parent);
                    memberAdapter.assignMemberAdapter(memberTodayList);
                    recyclerViewMemberMedia.setAdapter(memberAdapter);
                    recyclerViewMemberMedia.addOnItemTouchListener(new RecyclerItemClickListener(recyclerViewMemberMedia, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View inView, int inPosition) {
                            parent.adminViewChoose.put("id",memberTodayList.get(inPosition).getMemberID());
                            parent.adminViewChoose.put("type",type);
                            parent.adminViewChoose.put("date",chooseDateAdminEditText.getText().toString());
                            parent.navigateToAdminDetails();
                        }
                    }));
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}
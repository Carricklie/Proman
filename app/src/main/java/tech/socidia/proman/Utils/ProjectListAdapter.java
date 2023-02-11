package tech.socidia.proman.Utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import tech.socidia.proman.Models.Member;
import tech.socidia.proman.Models.Project;
import tech.socidia.proman.R;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ProjectListViewHolder>{
    List<Project> myProjectList;
    String userID;
    public ProjectListAdapter(List<Project> myProjectList,String userID){
        this.userID = userID;
        this.myProjectList = myProjectList;
    }

    @NonNull
    @Override
    public ProjectListAdapter.ProjectListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectListAdapter.ProjectListViewHolder holder, int position) {
        holder.bind(myProjectList.get(position),userID);
    }

    @Override
    public int getItemCount() {
        return myProjectList.size();
    }

    public static class ProjectListViewHolder extends RecyclerView.ViewHolder {

        TextView projectTitleTV;
        TextView projectIDTV;
        TextView dateStartedTV;
        RadioButton radiobuttonStatus;
        public ProjectListViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitleTV = itemView.findViewById(R.id.projectTitleItem);
            projectIDTV = itemView.findViewById(R.id.projectInvitationID);
            dateStartedTV = itemView.findViewById(R.id.projectStartDateTV);
            radiobuttonStatus = itemView.findViewById(R.id.statusRB);
        }

        public void bind(Project project,String userID) {
            projectTitleTV.setText(project.getProjectTitle());
            projectIDTV.setText(project.getProjectID());
            dateStartedTV.setText(project.getStartDateString());
            radiobuttonStatus.setChecked(true);
            for (Member member:
                 project.getMemberList()) {
                if(member.getMemberID().equals(userID)){
                    radiobuttonStatus.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#32CD32")));
                    return;
                }
            }
            for (Member member:
                 project.getPendingMemberList()) {
                if(member.getMemberID().equals(userID)){
                    radiobuttonStatus.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFF00")));
                    return;
                }
            }
        }
    }

}


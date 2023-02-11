package tech.socidia.proman.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import tech.socidia.proman.Activity.ProjectList;
import tech.socidia.proman.Models.Member;
import tech.socidia.proman.Models.PendingMember;
import tech.socidia.proman.R;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder>{
    List<Member> memberList = new ArrayList<>();
    List<PendingMember> pendingMemberList = new ArrayList<>();
    Context context;
    public MemberAdapter(Context context){
        this.context = context;
    }
    public void assignMemberAdapter(List<Member> memberList){
        this.memberList= memberList;
    }
    public void assignPendingMemberAdapter(List<PendingMember> pendingMemberList){
        this.pendingMemberList= pendingMemberList;
    }

    @NonNull
    @Override
    public MemberAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.MemberViewHolder holder, int position) {
        if(memberList.isEmpty()&&!pendingMemberList.isEmpty()){
            holder.bind(pendingMemberList.get(position),context);
        }else if(!memberList.isEmpty()&&pendingMemberList.isEmpty()){
            holder.bind(memberList.get(position),context);
        }

    }

    @Override
    public int getItemCount() {
        if(memberList.isEmpty()&&!pendingMemberList.isEmpty()){
            return pendingMemberList.size();
        }else if(!memberList.isEmpty()&&pendingMemberList.isEmpty()){
            return memberList.size();
        }
        return -1;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {

        ImageView memberProfilePictureIV;
        TextView memberNameTV;
        TextView memberRoleTV;
        TextView pendingMemberPhoneNumTV;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberProfilePictureIV = itemView.findViewById(R.id.memberProfilePictureIV);
            memberNameTV = itemView.findViewById(R.id.memberNameTV);
            memberRoleTV = itemView.findViewById(R.id.memberRole);
            pendingMemberPhoneNumTV = itemView.findViewById(R.id.memberPhoneNum);
        }

        public void bind(Object member, Context context) {
            if(member instanceof PendingMember){
                PendingMember pendingMember = (PendingMember)member;
                memberRoleTV.setVisibility(View.GONE);
                pendingMemberPhoneNumTV.setVisibility(View.VISIBLE);
                memberNameTV.setText(pendingMember.getName());
                FirebaseFirestore.getInstance().collection("users")
                        .document(pendingMember.getMemberID())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            pendingMemberPhoneNumTV.setText(task.getResult().getData().get("phoneNum").toString());
                            String link = task.getResult().getData().get("pictureLink").toString();
                            if(!link.isEmpty()){
                                StorageReference httpsReference = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(link);
                                httpsReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            Glide
                                                    .with(context)
                                                    .load(task.getResult())
                                                    .centerCrop()
                                                    .into(memberProfilePictureIV);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }else{
                Member theMember = (Member)member;
                memberNameTV.setText(theMember.getName());
                memberRoleTV.setVisibility(View.VISIBLE);
                pendingMemberPhoneNumTV.setVisibility(View.GONE);
                memberRoleTV.setText(theMember.getRole());
                FirebaseFirestore.getInstance().collection("users")
                        .document(theMember.getMemberID())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                String link = "";
                                try{
                                    link = task.getResult().getData().get("pictureLink").toString();
                                }catch (Exception e){
                                    link ="";
                                }
                                if(!link.isEmpty()){
                                    StorageReference httpsReference = FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(link);
                                    httpsReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()){
                                                Glide
                                                        .with(context)
                                                        .load(task.getResult())
                                                        .centerCrop()
                                                        .into(memberProfilePictureIV);
                                            }
                                        }
                                    });
                                }
                            }
                    }
                });
            }
        }
    }

}

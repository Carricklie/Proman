package tech.socidia.proman.Models;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project {
    String projectID;
    List<Member> memberList;
    List<PendingMember> pendingMemberList;
    String projectTitle;
    Timestamp startDate;
    Double initialBudget;
    Double projectDuration;


    public Project() {

    }
    public Project(String projectTitle, String startDate, Double projectDuration,Double initialBudget) {
        setProjectTitle(projectTitle);
        setStartDateString(startDate);
        setProjectDuration(projectDuration);
        setInitialBudget(initialBudget);
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Project(String projectID, List<Member> memberList, List<Member> pendingMemberList, String projectTitle, String startDate, Double projectDuration) {
        setProjectID(projectID);
        memberList = new ArrayList<>();
        pendingMemberList = new ArrayList<>();
        setProjectTitle(projectTitle);
        setStartDateString(startDate);
        setProjectDuration(projectDuration);
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public List<Member> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<Member> memberList) {
        this.memberList = memberList;
    }

    public List<PendingMember> getPendingMemberList() {
        return pendingMemberList;
    }

    public void setPendingMemberList(List<PendingMember> pendingMemberList) {
        this.pendingMemberList = pendingMemberList;
    }

    public String getStartDateString() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(startDate.toDate());
    }

    public boolean setStartDateString(String startDate) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try{
            Date temp = dateFormat.parse(startDate);
            this.startDate = new Timestamp(temp);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Double getProjectDuration() {
        return projectDuration;
    }

    public void setProjectDuration(Double projectDuration) {
        this.projectDuration = projectDuration;
    }
    public Double getInitialBudget() {
        return initialBudget;
    }

    public void setInitialBudget(Double initialBudget) {
        this.initialBudget = initialBudget;
    }
}
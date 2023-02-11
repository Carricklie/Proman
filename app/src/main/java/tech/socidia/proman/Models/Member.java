package tech.socidia.proman.Models;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Member {
    String name;
    String memberID;
    String role;
    Timestamp dateJoined;

    public Member(String name, String memberID, String role, String dateJoined) {
        setName(name);
        setMemberID(memberID);
        setRole(role);
        setDateJoinedString(dateJoined);
    }
    public Member(String name, String memberID, String role, Timestamp dateJoined) {
        setName(name);
        setMemberID(memberID);
        setRole(role);
        setDateJoined(dateJoined);
    }
    public String getDateJoinedString() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(dateJoined.toDate());
    }

    public boolean setDateJoinedString(String startDate) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try{
            Date temp = dateFormat.parse(startDate);
            this.dateJoined = new Timestamp(temp);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Timestamp dateJoined) {
        this.dateJoined = dateJoined;
    }
}

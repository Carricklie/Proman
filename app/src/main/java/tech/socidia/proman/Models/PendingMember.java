package tech.socidia.proman.Models;

import com.google.firebase.Timestamp;

public class PendingMember extends Member{
    String applyDescription;

    public String getApplyDescription() {
        return applyDescription;
    }

    public void setApplyDescription(String applyDescription) {
        this.applyDescription = applyDescription;
    }

    public PendingMember(String name, String memberID, String role,String applyDescription) {
        super(name, memberID, role, "");
        setApplyDescription(applyDescription);
    }
}

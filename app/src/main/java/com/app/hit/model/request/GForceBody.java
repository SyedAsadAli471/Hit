package com.app.hit.model.request;

import java.util.List;

public class GForceBody {
    String user_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGdate() {
        return Gdate;
    }

    public void setGdate(String gdate) {
        Gdate = gdate;
    }

    public List<Integer> getGforce() {
        return Gforce;
    }

    public void setGforce(List<Integer> gforce) {
        Gforce = gforce;
    }

    String Gdate;
    List<Integer> Gforce = null;
}

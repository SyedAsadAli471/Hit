
package com.app.hit.model.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendGForceResponse {

    @SerializedName("response")
    @Expose
    private AddUserData data;

    public AddUserData getData() {
        return data;
    }

    public void setData(AddUserData data) {
        this.data = data;
    }

}

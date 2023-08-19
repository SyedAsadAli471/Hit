
package com.app.hit.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetUserListResponse {

    @SerializedName("response")
    @Expose
    private GetUserListData getUserListData;

    public GetUserListData getGetUserListData() {
        return getUserListData;
    }

    public void setGetUserListData(GetUserListData getUserListData) {
        this.getUserListData = getUserListData;
    }

}

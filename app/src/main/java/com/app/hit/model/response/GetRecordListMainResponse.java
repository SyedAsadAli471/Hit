
package com.app.hit.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetRecordListMainResponse {

    @SerializedName("response")
    @Expose
    private GetRecordListResponse response;

    public GetRecordListResponse getResponse() {
        return response;
    }

    public void setResponse(GetRecordListResponse response) {
        this.response = response;
    }

}


package com.app.hit.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ImageUrls implements Serializable {

    @SerializedName("1x")
    @Expose
    private String _1x;
    @SerializedName("2x")
    @Expose
    private String _2x;
    @SerializedName("3x")
    @Expose
    private String _3x;

    public String get1x() {
        return _1x;
    }

    public void set1x(String _1x) {
        this._1x = _1x;
    }

    public String get2x() {
        return _2x;
    }

    public void set2x(String _2x) {
        this._2x = _2x;
    }

    public String get3x() {
        return _3x;
    }

    public void set3x(String _3x) {
        this._3x = _3x;
    }

}

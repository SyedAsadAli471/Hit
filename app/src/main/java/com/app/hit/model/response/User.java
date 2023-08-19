
package com.app.hit.model.response;

import com.app.hit.model.response.ImageUrls;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user_type")
    @Expose
    private String userType;
    @SerializedName("signup_via")
    @Expose
    private String signupVia;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("age")
    @Expose
    private String age;
    @SerializedName("weight")
    @Expose
    private String weight;
    @SerializedName("height")
    @Expose
    private String height;
    @SerializedName("sports")
    @Expose
    private String sports;
    @SerializedName("position")
    @Expose
    private String position;
    @SerializedName("terms")
    @Expose
    private String terms;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("verify_code")
    @Expose
    private String verifyCode;
    @SerializedName("verify_status")
    @Expose
    private Integer verifyStatus;
    @SerializedName("account_visibility")
    @Expose
    private String accountVisibility;
    @SerializedName("visibility")
    @Expose
    private Integer visibility;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("login_at")
    @Expose
    private String loginAt;

    public String getDevicedetail() {
        return devicedetail;
    }

    public void setDevicedetail(String devicedetail) {
        this.devicedetail = devicedetail;
    }

    @SerializedName("devicedetail")
    @Expose
    private String devicedetail;
    @SerializedName("image_urls")
    @Expose
    private ImageUrls imageUrls;

    public int getMaxGForce() {
        return maxGForce;
    }

    public void setMaxGForce(int maxGForce) {
        this.maxGForce = maxGForce;
    }

    int maxGForce;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return devicetype;
    }

    public void setDeviceType(String deviceType) {
        this.devicetype = deviceType;
    }

    @SerializedName("DeviceId")
    @Expose
    private String deviceId;
    @SerializedName("devicename")
    @Expose
    private String deviceName;
    @SerializedName("devicetype")
    @Expose
    private String devicetype;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getSignupVia() {
        return signupVia;
    }

    public void setSignupVia(String signupVia) {
        this.signupVia = signupVia;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getSports() {
        return sports;
    }

    public void setSports(String sports) {
        this.sports = sports;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getAccountVisibility() {
        return accountVisibility;
    }

    public void setAccountVisibility(String accountVisibility) {
        this.accountVisibility = accountVisibility;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public Object getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Object getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(String loginAt) {
        this.loginAt = loginAt;
    }

    public ImageUrls getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ImageUrls imageUrls) {
        this.imageUrls = imageUrls;
    }

}

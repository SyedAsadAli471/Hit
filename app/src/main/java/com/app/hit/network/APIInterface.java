package com.app.hit.network;

import com.app.hit.model.request.GForceBody;
import com.app.hit.model.response.AddUserResponse;
import com.app.hit.model.response.GetRecordListMainResponse;
import com.app.hit.model.response.GetUserListResponse;
import com.app.hit.model.response.SendGForceResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("user/list?")
    Call<GetUserListResponse> getUserList(@Query("pagination") boolean page);

    @Multipart
    @POST("user/add")
    Call<AddUserResponse> addUser(@Part MultipartBody.Part filePart,
                                  @Part("name") RequestBody name,
                                  @Part("age") RequestBody age,
                                  @Part("weight") RequestBody weight,
                                  @Part("height") RequestBody height,
                                  @Part("sports") RequestBody sport,
                                  @Part("position") RequestBody position,
                                  @Part("email") RequestBody email,
                                  @Part("DeviceId") RequestBody deviceId,
                                  @Part("devicetype") RequestBody devicetype,
                                  @Part("devicename") RequestBody devicename,
                                  @Part("devicedetail") RequestBody devicedetail,
                                  @Part("terms") RequestBody terms);

    @Multipart
    @POST("user/update")
    Call<AddUserResponse> editUser(@Part MultipartBody.Part filePart,
                                   @Part("name") RequestBody name,
                                   @Part("age") RequestBody age,
                                   @Part("weight") RequestBody weight,
                                   @Part("height") RequestBody height,
                                   @Part("sports") RequestBody sport,
                                   @Part("position") RequestBody position,
                                   @Part("email") RequestBody email,
                                   @Part("DeviceId") RequestBody deviceId,
                                   @Part("devicetype") RequestBody devicetype,
                                   @Part("devicename") RequestBody devicename,
                                   @Part("devicedetail") RequestBody devicedetail,
                                   @Part("terms") RequestBody terms);
//    @FormUrlEncoded
//    @POST("add/record")
//    Call<SendGForceResponse> sendGForceData(@Field("user_id") String userId,
//                                            @Field("Gdate") String date,
//                                            @Field("Gforce[]") ArrayList<Integer> data);

    @POST("add/record")
    Call<SendGForceResponse> sendGForceData(@Body GForceBody body);

    @GET("record/list?")
    Call<GetRecordListMainResponse> getRecordList(@Query("pagination") boolean page);


//    @GET(WEB_API_URL + "app/brewing/type/recipe/step/list")
//    @Headers("Accept:application/json")
//    Call<GetRecipeByIdResponse> getRecipeById(
//            @Header("Accept-Language") String language,
//            @Header("Authorization") String Authorization,
//            @Query("brewing_type_recipe_id") String id_order);
//
//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}

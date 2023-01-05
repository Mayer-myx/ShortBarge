package com.deepsoft.shortbarge.driver.retrofit;

import com.deepsoft.shortbarge.driver.gson.ResultGson;

import org.json.JSONArray;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

    /**
     * 登陆
     * @param username
     * @param password
     * @return
     */
    @GET("account/login")
    Call<ResultGson> getLogin(@Query("username") String username,
                                 @Query("password") String password);

    /**
     * 登出
     * @return
     */
    @GET("account/logout")
    Call<ResultGson> getLogout();

    /**
     * 添加用户
     * @param username
     * @param password
     * @param roleldList
     * @return
     */
    @GET("account/addUser")
    Call<ResultGson> getAddUser(@Query("username") String username,
                                @Query("password") String password,
                                @Query("roleldList") JSONArray roleldList);

    /**
     * 删除用户
     * @param ids
     * @return
     */
    @GET("account/deleteUser")
    Call<ResultGson> getDeleteUser(@Query("ids") JSONArray ids);

    /**
     * 修改用户
     * @param id
     * @param username
     * @param password
     * @param roleldList
     * @return
     */
    @GET("account/updateUser")
    Call<ResultGson> getUpdateUser(@Query("id") int id,
                                   @Query("uesrname") String username,
                                   @Query("password") String password,
                                   @Query("roleldList") JSONArray roleldList);

    /**
     * 获取用户详情
     * @param id
     * @return
     */
    @GET("account/getUserDetail")
    Call<ResultGson> getUserDetail(@Query("id") String id);

    /**
     * 获取用户名
     * @return
     */
    @GET("account/getUserName")
    Call<ResultGson> getUserName();

    /**
     * 获取司机信息
     * @return
     */
    @GET("driver/getDriverInfo")
    Call<ResultGson> getDriverInfo();

    /**
     * 获取任务列表 轮询
     * @return
     */
    @GET("transport/getDriverTask")
    Observable<ResultGson> getDriverTask();

    /**
     * 天气
     * @return
     */
    @GET("weather/getWeatherInfo")
    Call<ResultGson> getWeatherInfo(@Query("lang") String lang);

    /**
     * 修改任务状态
     * @param transportTaskId
     * @param state
     * @return
     */
    @GET("transport/changeTaskState")
    Call<ResultGson> changeTaskState(@Query("transportTaskId") String transportTaskId,
                              @Query("state") Integer state);


    /**
     * 天气
     * @return
     */
    @GET("account/getAdminUser")
    Call<ResultGson> getAdminUser();

    /**
     * 上传文件 录音
     * @param part
     * @return
     */
    @Multipart
    @POST("file/uploadFile")
    Call<ResultGson> uploadFile(@Part MultipartBody.Part part);


//    /*无参POST请求 */
//    @POST("postNoParamUser")
//    Call<DriverInfoGson>postNoParamUser();
//
//    /*有参POST请求 */
//    @FormUrlEncoded
//    @POST("postParamUser")
//    Call<DriverInfoGson> postParamUser(@Field("id") int id);
//
//    /*JSON化参数POST请求 */
//    @POST("postObjectParamUser")
//    Call<DriverInfoGson>postObjectParamUser(@Body DriverInfoGson user);
}

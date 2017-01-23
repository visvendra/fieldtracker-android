package com.allsmart.fieldtracker.parsers;

import android.widget.Toast;

import com.allsmart.fieldtracker.activity.MainActivity;
import com.crashlytics.android.Crashlytics;
import com.allsmart.fieldtracker.utils.Logger;
import com.allsmart.fieldtracker.storage.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by AllSmart-LT008 on 12/7/2016.
 */

public class UserDetailParser {

    String response ="";// fnp.jks
    String result = "";
    Preferences preferences;
    public UserDetailParser(String response,Preferences preferences)
    {
        this.response =  response;
        this.preferences =  preferences;
    }
    public String Parse()
    {
        try {
            JSONObject parentobject = new JSONObject(response);
            if(parentobject.has("user")){
                result = "success";
                JSONArray users = new JSONArray(parentobject.getString("user"));
                for(int i=0;i<users.length();i++) {
                    JSONObject obj = users.getJSONObject(i);
                    preferences.saveString(Preferences.USERNAME, obj.getString("username"));
                    preferences.saveString(Preferences.USERFULLNAME, obj.getString("userFullName"));
                    preferences.saveString(Preferences.USERFIRSTNAME, obj.getString("firstName"));
                    preferences.saveString(Preferences.USERLASTNAME, obj.getString("lastName"));
                    preferences.saveString(Preferences.USERID, obj.getString("userId"));
                    if(obj.has("userPhotoPath")) {
                        preferences.saveString(Preferences.USER_PHOTO, obj.getString("userPhotoPath"));
                    } else {
                        preferences.saveString(Preferences.USER_PHOTO,"");
                    }
                    preferences.saveString(Preferences.USEREMAIL, obj.getString("emailAddress"));
                    preferences.saveString(Preferences.ROLETYPEID, obj.getString("roleTypeId"));
 /*For testing*/ // preferences.saveString(Preferences.ROLETYPEID, "FieldExecutiveOffPremise");
                    if(obj.has("productStoreId")) {
                        preferences.saveString(Preferences.PARTYID, obj.getString("productStoreId"));
                    }
                    preferences.commit();;
                }
            }else if(parentobject.has("error")){
                result =parentobject.getString("errors");
            }
        } catch (JSONException e) {
            Logger.e("Log",e);
            Crashlytics.log(1,getClass().getName(),"Error in Parsing the response");
            Crashlytics.logException(e);
        } finally {
            return result;
        }
    }

}
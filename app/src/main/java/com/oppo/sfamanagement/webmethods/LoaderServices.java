package com.oppo.sfamanagement.webmethods;

import android.content.Context;
import android.os.Bundle;

import com.oppo.sfamanagement.MainActivity;
import com.oppo.sfamanagement.database.AppsConstant;
import com.oppo.sfamanagement.database.Preferences;
import com.oppo.sfamanagement.parsers.AddPromoterParser;
import com.oppo.sfamanagement.parsers.AddStoreParser;
import com.oppo.sfamanagement.parsers.ImageUploadParser;
import com.oppo.sfamanagement.parsers.PromoterListParser;
import com.oppo.sfamanagement.parsers.PromoterUpdateParser;
import com.oppo.sfamanagement.parsers.StoreDetailParser;
import com.oppo.sfamanagement.parsers.StoreListParser;
import com.oppo.sfamanagement.parsers.StoreUpdateParser;
import com.oppo.sfamanagement.parsers.UserDetailParser;
import com.oppo.sfamanagement.parsers.UserStoreDetailParser;

import static com.oppo.sfamanagement.webmethods.Services.ADD_PROMOTER;


/**
 * Created by AllSmart-LT008 on 8/5/2016.
 */
public class LoaderServices extends CustomAsyncTask
{
    private Context context;
    LoaderMethod strType;
    Bundle args;
    Preferences preferences;
    public LoaderServices(Context context, LoaderMethod strType, Bundle args) {
        super(context);
        this.context = context;
        this.args = args;
        this.strType = strType;
        preferences = new Preferences(context);

    }
    @Override
    public Object loadInBackground()
    {
//        Log.e("URL"+strType,args.getString(AppsConstant.URL));
        String response ="";// Base64
        if(strType==LoaderMethod.USER_LOGIN)
            response = new RestHelper().makeRestCallAndGetResponseLogin(args.getString(AppsConstant.URL),args.getString(AppsConstant.USER), args.getString(AppsConstant.PASSWORD),preferences);
//        else  if(strType==LoaderMethod.IMAGE_UPLOAD)
//            response = new RestHelper().makeRestCallAndGetResponseImageUpload(args.getString(AppsConstant.URL), args.getString(AppsConstant.FILE),preferences);
        else  if(strType==LoaderMethod.IMAGE_UPLOAD)
            response = new RestHelper().makeRestCallAndGetResponseImageUpload(args.getString(AppsConstant.URL), args.getString(AppsConstant.FILE), args.getString(AppsConstant.FILEPURPOSE),preferences);
        else
            response = new RestHelper().makeRestCallAndGetResponse(args.getString(AppsConstant.URL),args.getString(AppsConstant.METHOD), args.getString(AppsConstant.PARAMS),preferences);
       switch (strType)
        {
            case USER_LOGIN:
                return new UserDetailParser(response,preferences).Parse();
            case USER_STORE_DETAIL:
                return new UserStoreDetailParser(response,preferences).Parse();
            case STORE_DETAIL:
                return new StoreDetailParser(response,preferences).Parse();
            case STORE_LIST:
                return new StoreListParser(response,preferences).Parse();
            case STORE_UPDATE:
                return new StoreUpdateParser(response).Parse();
            case ADD_STORE:
                return new AddStoreParser(response).Parse();
            case PROMOTER_LIST:
                return new PromoterListParser(response).Parse();
            case ADD_PROMOTER:
                return new AddPromoterParser(response).Parse();
            case UPDATE_PROMOTER:
                return new PromoterUpdateParser(response).Parse();
            case IMAGE_UPLOAD:
                return new ImageUploadParser(response).Parse();
            default:
                return  null;

        }

    }
}
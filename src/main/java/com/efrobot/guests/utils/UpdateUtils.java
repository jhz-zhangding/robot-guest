package com.efrobot.guests.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.efrobot.guests.R;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.crypto.JsonUtil;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.net.BaseSendRequestListener;
import com.efrobot.library.net.NetClient;
import com.efrobot.library.net.TextMessage;
import com.efrobot.library.net.utils.NetUtil;

import org.json.JSONException;

/**
 * Created by zd on 2017/10/18.
 */
public class UpdateUtils {

    private UpdateUtils instance;

    private final String TAG = this.getClass().getSimpleName();

    /**
     * 根据包名获取详情
     */
    public static final String APPDETAIL_PACKAGE = "/v2/software/item2pkname";

    public UpdateUtils getInstance() {
        if(instance == null) {
            instance = new UpdateUtils();
        }
        return instance;
    }

    /**
     * 获取应用详情数据
     *
     * @param appPackageName 应用包名
     */
    public String getAppDetail(Context context, String appPackageName, final onAppCallBack onAppCallBack) {
        String version = "";
        if (!NetUtil.checkNet(context)) {
            RobotToastUtil.getInstance(context).showToastBusiness("无网络链接");
        }
        try {
            TextMessage message = new TextMessage();
            message.setRequestMethod(TextMessage.REQUEST_METHOD_POST);
            {      //根据包名进行详情获取
                message.setUrl(com.efrobot.library.urlconfig.UrlConstants.Store.getOldVersionHost() + APPDETAIL_PACKAGE);
                message.append("packageName", appPackageName);
                message.append("robotId",  RobotState.getInstance(context).getRobotNumber());
            }
            message.setEncryption(true);
            NetClient.getInstance(context).sendNetMessage(message, new BaseSendRequestListener<TextMessage>() {
                @Override
                public void onFail(TextMessage message, int errorCode, String errorMessage) {
                    super.onFail(message, errorCode, errorMessage);
                    if(onAppCallBack != null) {
                        onAppCallBack.onFail(message, errorCode, errorMessage);
                    }
                    L.e(TAG, message.getUrl() + "    onFail=" + errorMessage);
                }

                @Override
                public void onSuccess(TextMessage message, String result) {
                    super.onSuccess(message, result);
                    if(onAppCallBack != null) {
                        onAppCallBack.onSuccess(message, result);
                    }
                    L.e(TAG, message.getUrl() + "    onSuccess=" + result + "");

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 2  * 获取版本号
     * 3  * @return 当前应用的版本号
     * 4
     */
    public String getVersion(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface onAppCallBack {
        void onSuccess(TextMessage message, String result);
        void onFail(TextMessage message, int errorCode, String errorMessage);
    }

}

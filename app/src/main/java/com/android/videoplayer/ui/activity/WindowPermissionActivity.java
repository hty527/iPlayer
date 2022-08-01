package com.android.videoplayer.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.android.iplayer.utils.PlayerUtils;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.WindowPermission;

/**
 * Created by TinyHung@Outlook.com
 * 2020/2/17
 * 悬浮窗权限获取
 */

public class WindowPermissionActivity extends AppCompatActivity {

    //设置回执
    protected final static int SETTING_REQUST = 123;
    private static final String TAG = "WindowPermissionActivity";
    //授权状态,true:授权完成 false:授权被拒绝
    private boolean mSuccess;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),android.R.color.transparent));
        requstPermissions();
    }

    /**
     * 向用户申请的权限列表
     */
    private void onRequstPermissionResult(boolean success) {
        this.mSuccess=success;
        finish();
    }

    /**
     * 运行时权限
     */
    protected void requstPermissions() {
        boolean hasPermission = PlayerUtils.getInstance().checkWindowsPermission(this);//检查是否获取了悬浮窗权限
        if(hasPermission){
            onRequstPermissionResult(true);
            return;
        }
        //向用户申请悬浮窗权限
        try {
            Intent intent = new Intent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//开启后startActivityForResult失效,会非正常回调:onActivityResult
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse( "package:"+ PlayerUtils.getInstance().getPackageName(getApplicationContext())));
            } else {
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", PlayerUtils.getInstance().getPackageName(getApplicationContext()), null));
            }
            startActivityForResult(intent,SETTING_REQUST);
        }catch (Throwable e){
            e.printStackTrace();
            onRequstPermissionResult(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d(TAG,"onActivityResult-->requestCode:"+requestCode+",resultCode:"+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== SETTING_REQUST){
            boolean hasPermission = PlayerUtils.getInstance().checkWindowsPermission(this);//检查是否获取了悬浮窗权限
            if(hasPermission){
                onRequstPermissionResult(true);
            }else{
                onRequstPermissionResult(false);
            }
        }else{
            Toast.makeText(getApplicationContext(),"开启失败!",Toast.LENGTH_SHORT).show();
            onRequstPermissionResult(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        WindowPermission.getInstance().onRequstPermissionResult(mSuccess);
    }
}
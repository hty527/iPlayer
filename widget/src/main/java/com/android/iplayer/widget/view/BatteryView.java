package com.android.iplayer.widget.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.iplayer.widget.R;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/8/9
 * Desc:电池电量、实时时间显示控件
 */
public class BatteryView extends LinearLayout {

    private BatteryReceiver mBatteryReceiver;
    private TextView mBatteryText,mBatteryTime;
    private ImageView mBatteryStatus;

    public BatteryView(Context context) {
        this(context,null);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.player_battery_view,this);
        mBatteryText = (TextView) findViewById(R.id.battery_text);
        mBatteryTime = (TextView) findViewById(R.id.battery_time);
        mBatteryStatus = (ImageView) findViewById(R.id.battery_status);
        updateSystemTime();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(null==mBatteryReceiver){
            mBatteryReceiver = new BatteryReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//电池电量变化、充电状态
            intentFilter.addAction(Intent.ACTION_TIME_TICK);//系统时间每分钟变化
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);//手动改变系统时间变化
            getContext().registerReceiver(mBatteryReceiver,intentFilter);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(null!=mBatteryReceiver){
            getContext().unregisterReceiver(mBatteryReceiver);
            mBatteryReceiver=null;
        }
    }

    /**
     * 是否显示系统时间交互组件
     * @param showTime true:显示系统时间 false:不显示
     */
    public void showTime(boolean showTime){
        if(!showTime){
            if(null!=mBatteryTime){
                mBatteryTime.setVisibility(View.GONE);
                mBatteryTime=null;
            }
        }
    }

    /**
     * 更新系统时间
     */
    private void updateSystemTime() {
        if(null!=mBatteryTime){
            mBatteryTime.setText(PlayerUtils.getInstance().getCurrentTimeStr());
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {

        public BatteryReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            //电池电量、充电状态变化
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                Bundle extras = intent.getExtras();
                if(null!=mBatteryText&&null!=extras){
                    //获取剩余电量
                    int current = extras.getInt("level");// 获得当前电量
                    int total = extras.getInt("scale");// 获得总电量
                    int percent = current * 100 / total;
                    mBatteryText.setText(percent+"%");
                    //检查充电状态
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||status == BatteryManager.BATTERY_STATUS_FULL;
                    if(null!=mBatteryStatus) mBatteryStatus.setVisibility(isCharging?View.VISIBLE:View.INVISIBLE);
                }
            //系统时间每分钟变化\手动改变系统时间变化
            }else if(Intent.ACTION_TIME_TICK.equals(intent.getAction())||Intent.ACTION_TIME_CHANGED.equals(intent.getAction())){
                updateSystemTime();
            }
        }
    }
}
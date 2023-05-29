package com.android.player.video.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.player.R;
import com.android.player.video.listener.OnMenuActionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * created by hty
 * 2022/7/11
 * Desc:功能菜单
 */
public class PlayerMenuView extends LinearLayout implements View.OnClickListener {

    private List<View> mMenuSpeeds =new ArrayList<>();//倍速
    private List<View> mMenuZooms =new ArrayList<>();//视频缩放比例
    private List<View> mMenuScales =new ArrayList<>();//视频显示比例
    private List<View> mMenuMutes =new ArrayList<>();//静音
    private List<View> mMenuMirrors=new ArrayList<>();//镜像

    public PlayerMenuView(Context context) {
        this(context,null);
    }

    public PlayerMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayerMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_menu_view,this);
        //倍速
        View menu_speed_1 = findViewById(R.id.menu_speed_1);
        View menu_speed_2 = findViewById(R.id.menu_speed_2);
        View menu_speed_3 = findViewById(R.id.menu_speed_3);
        View menu_speed_4 = findViewById(R.id.menu_speed_4);
        View menu_speed_5 = findViewById(R.id.menu_speed_5);
        menu_speed_1.setOnClickListener(this);menu_speed_2.setOnClickListener(this);menu_speed_3.setOnClickListener(this);
        menu_speed_4.setOnClickListener(this);menu_speed_5.setOnClickListener(this);
        mMenuSpeeds.add(menu_speed_1);mMenuSpeeds.add(menu_speed_2);mMenuSpeeds.add(menu_speed_3);
        mMenuSpeeds.add(menu_speed_4);mMenuSpeeds.add(menu_speed_5);

        //视频缩放模式
        View menu_zoom_1 = findViewById(R.id.menu_zoom_1);
        View menu_zoom_2 = findViewById(R.id.menu_zoom_2);
        View menu_zoom_3 = findViewById(R.id.menu_zoom_3);
        menu_zoom_1.setOnClickListener(this);menu_zoom_2.setOnClickListener(this);menu_zoom_3.setOnClickListener(this);
        mMenuZooms.add(menu_zoom_1);
        mMenuZooms.add(menu_zoom_2);
        mMenuZooms.add(menu_zoom_3);

        //视频显示比例
        View menu_scale_1 = findViewById(R.id.menu_scale_1);
        View menu_scale_2 = findViewById(R.id.menu_scale_2);
        View menu_scale_3 = findViewById(R.id.menu_scale_3);
        menu_scale_1.setOnClickListener(this);menu_scale_2.setOnClickListener(this);menu_scale_3.setOnClickListener(this);
        mMenuScales.add(menu_scale_1);mMenuScales.add(menu_scale_2);mMenuScales.add(menu_scale_3);

        //静音
        View menu_mute_1 = findViewById(R.id.menu_mute_1);
        View menu_mute_2 = findViewById(R.id.menu_mute_2);
        menu_mute_1.setOnClickListener(this);menu_mute_2.setOnClickListener(this);
        mMenuMutes.add(menu_mute_1);
        mMenuMutes.add(menu_mute_2);

        //镜像
        View menu_degree_1 = findViewById(R.id.menu_degree_1);
        View menu_degree_2 = findViewById(R.id.menu_degree_2);
        menu_degree_1.setOnClickListener(this);menu_degree_2.setOnClickListener(this);
        mMenuMirrors.add(menu_degree_1);mMenuMirrors.add(menu_degree_2);
        selectedMuteIndex(1,false);//是否静音
        selectedZoomIndex(1);//播放器默认原始大小，单Demo演示用到的这个自定义组件默认是缩放裁剪铺满全屏
        selectedMirrorsIndex(1);//是否镜像
        onReset();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.menu_speed_1) {
            selectedSpeedIndex(0);
        }else if (id == R.id.menu_speed_2) {
            selectedSpeedIndex(1);
        }else if (id == R.id.menu_speed_3) {
            selectedSpeedIndex(2);
        }else if (id == R.id.menu_speed_4) {
            selectedSpeedIndex(3);
        }else if (id == R.id.menu_speed_5) {
            selectedSpeedIndex(4);
        }else if (id == R.id.menu_zoom_1) {
            selectedZoomIndex(0);
        }else if (id == R.id.menu_zoom_2) {
            selectedZoomIndex(1);
        }else if (id == R.id.menu_zoom_3) {
            selectedZoomIndex(2);
        }else if (id == R.id.menu_scale_1) {
            selectedScaleIndex(0);
        }else if (id == R.id.menu_scale_2) {
            selectedScaleIndex(1);
        }else if (id == R.id.menu_scale_3) {
            selectedScaleIndex(2);
        }else if (id == R.id.menu_mute_1) {
            selectedMuteIndex(0,true);
        }else if (id == R.id.menu_mute_2) {
            selectedMuteIndex(1,true);
        }else if (id == R.id.menu_degree_1) {
            selectedMirrorsIndex(0);
        }else if (id == R.id.menu_degree_2) {
            selectedMirrorsIndex(1);
        }
    }

    /**
     * 倍速选中
     * @param index
     */
    private void selectedSpeedIndex(int index) {
        if(null!= mMenuSpeeds && mMenuSpeeds.size()>0){
            for (View view : mMenuSpeeds) {
                view.setSelected(false);
            }
            if(mMenuSpeeds.size()>index) {
                mMenuSpeeds.get(index).setSelected(true);
                if(null!=mOnMenuActionListener){
                    float speed=1.0f;
                    switch (index) {
                        case 0:
                            speed=0.5f;
                            break;
                        case 1:
                            speed=0.75f;
                            break;
                        case 2:
                            speed=1.0f;
                            break;
                        case 3:
                            speed=2.5f;
                            break;
                        case 4:
                            speed=2.0f;
                            break;
                    }
                    mOnMenuActionListener.onSpeed(speed);
                }
            }
        }
    }

    /**
     * 视频缩放比例
     * @param index
     */
    private void selectedZoomIndex(int index) {
        if(null!= mMenuZooms && mMenuZooms.size()>0){
            for (View view : mMenuZooms) {
                view.setSelected(false);
            }
            if(mMenuZooms.size()>index){
                mMenuZooms.get(index).setSelected(true);
                if(null!=mOnMenuActionListener) mOnMenuActionListener.onZoom(index);
            }
        }
    }

    /**
     * 视频显示比例
     * @param index
     */
    private void selectedScaleIndex(int index) {
        if(null!= mMenuScales && mMenuScales.size()>0){
            for (View view : mMenuScales) {
                view.setSelected(false);
            }
            if(mMenuScales.size()>index) mMenuScales.get(index).setSelected(true);
        }
    }

    /**
     * 静音
     * @param index
     * @param isSet 是否设置
     */
    private void selectedMuteIndex(int index,boolean isSet) {
        if(null!= mMenuMutes && mMenuMutes.size()>0){
            for (View view : mMenuMutes) {
                view.setSelected(false);
            }
            if(mMenuMutes.size()>index){
                mMenuMutes.get(index).setSelected(true);
                if(isSet&&null!=mOnMenuActionListener) {
                    mOnMenuActionListener.onMute(0==index?true:false);
                }
            }
        }
    }

    /**
     * 更新静音状态
     * @param mute true:已静音 false:未静音
     * @param isSet 是否设置
     */
    public void updateMute(boolean mute,boolean isSet) {
        selectedMuteIndex(mute?0:1,isSet);
    }

    /**
     * 镜像
     * @param index
     */
    private void selectedMirrorsIndex(int index) {
        if(null!= mMenuMirrors && mMenuMirrors.size()>0){
            for (View view : mMenuMirrors) {
                view.setSelected(false);
            }
            if(mMenuMirrors.size()>index){
                mMenuMirrors.get(index).setSelected(true);
                if(null!=mOnMenuActionListener) {
                    mOnMenuActionListener.onMirror(0==index?true:false);
                }
            }
        }
    }

    private OnMenuActionListener mOnMenuActionListener;

    public void setOnMenuActionListener(OnMenuActionListener onMenuActionListener) {
        mOnMenuActionListener = onMenuActionListener;
    }

    /**
     * 重置
     */
    public void onReset(){
        selectedSpeedIndex(2);//播放倍速
        selectedScaleIndex(0);//画面显示比例
    }
}
package com.android.videoplayer.video.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.video.ui.fragment.ListAutoPlayerChangeFragment;
import com.android.videoplayer.video.ui.fragment.ListPlayerChangedFragment;

/**
 * created by hty
 * 2022/7/2
 * Desc:列表播放和自动播放示例
 */
public class PagerListActivity extends BaseActivity {

    private ListPlayerChangedFragment mPlayerAutoFragment;//列表自动播放
    private ListAutoPlayerChangeFragment mPlayerChangeFragment;//列表自动播放+点击跳转无缝播放

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                finish();
            }
        });
        String autoPlay = getIntent().getStringExtra("auto_play");
        if("1".equals(autoPlay)){
            mPlayerChangeFragment =new ListAutoPlayerChangeFragment();//列表滚动停止后 自动播放+点击item转场无缝播放
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mPlayerChangeFragment).commitAllowingStateLoss();
        }else{
            mPlayerAutoFragment = new ListPlayerChangedFragment();//列表滚动停止后 点击播放+点击item转场无缝播放
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mPlayerAutoFragment).commitAllowingStateLoss();
        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if(null!=mPlayerAutoFragment){
            if(mPlayerAutoFragment.isBackPressed()){
                super.onBackPressed();
            }
            return;
        }
        if(null!=mPlayerChangeFragment){
            if(mPlayerChangeFragment.isBackPressed()){
                super.onBackPressed();
            }
            return;
        }
        super.onBackPressed();
    }
}
package com.android.videoplayer.video.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.video.ui.fragment.ListAutoPlayerFragment;
import com.android.videoplayer.video.ui.fragment.ListPlayerChangeFragment;
import com.android.videoplayer.video.ui.fragment.ListPlayerFragment;
import com.android.videoplayer.ui.widget.TitleView;

/**
 * created by hty
 * 2022/7/2
 * Desc:列表播放和自动播放示例
 */
public class PagerListActivity extends BaseActivity {

    private ListPlayerFragment mPlayerFragment;//列表播放
    private ListAutoPlayerFragment mPlayerAutoFragment;//列表自动播放
    private ListPlayerChangeFragment mPlayerChangeFragment;//列表自动播放+点击跳转无缝播放

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        String type = getIntent().getStringExtra("type");
        if("1".equals(type)){
            mPlayerFragment =new ListPlayerFragment();//列表滚动停止后点击播放
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mPlayerFragment).commitAllowingStateLoss();
        }else if("2".equals(type)){
            mPlayerAutoFragment = new ListAutoPlayerFragment();//列表滚动停止后自动播放
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mPlayerAutoFragment).commitAllowingStateLoss();
        }else if("3".equals(type)){
            mPlayerChangeFragment =new ListPlayerChangeFragment();//列表滚动停止后自动播放+点击item转场无缝播放
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mPlayerChangeFragment).commitAllowingStateLoss();
        }else{
            Toast.makeText(getApplicationContext(),"位置的事件",Toast.LENGTH_SHORT).show();
            finish();
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
        if(null!=mPlayerFragment){
            if(mPlayerFragment.isBackPressed()){
                super.onBackPressed();
            }
            return;
        }
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
package com.android.player.pager.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.android.player.R;
import com.android.player.base.BaseActivity;
import com.android.player.base.BasePresenter;
import com.android.player.pager.bean.VideoBean;
import com.android.player.pager.fragment.PagerPlayerFragment;
import com.android.player.pager.fragment.VideoListFragment;
import com.android.player.utils.ScreenUtils;
import com.android.player.utils.StatusUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

/**
 * created by hty
 * 2022/6/29
 * Desc:视频列表和切片列表播放示例
 */
public class PagerPlayerActivity extends BaseActivity {

    private static String PERMISSION_WRITE_EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";//抖音视频缓存
    private static String PERMISSION_READ_EXTERNAL = "android.permission.READ_EXTERNAL_STORAGE";//抖音视频缓存

    private List<String> mTabs;
    private ViewPager mViewPager;
    private SparseArrayCompat<Fragment> mFragments = new SparseArrayCompat<>();
    private TabLayout mTabLayout;
    private ImageView mBtnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_simple);

        View statusBar = findViewById(R.id.status_bar);
        findViewById(R.id.status_bar).getLayoutParams().height= ScreenUtils.getInstance().getStatusBarHeight(getApplicationContext());
        StatusUtils.setStatusTextColor1(true, PagerPlayerActivity.this);//黑色字体

        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mBtnBack.setColorFilter(Color.parseColor("#333333"));
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTabs = new ArrayList<>();
        mTabs.add("推荐");
        mTabs.add("播放");

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(mTabs.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTabs.get(1)));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(null!=mViewPager){
                    mViewPager.setCurrentItem(position,true);
                }
                if(null!=mTabLayout){
                    if(0==position){ //黑色字体
                        if(null!=mBtnBack) mBtnBack.setColorFilter(Color.parseColor("#333333"));
                        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#333333"));
                        mTabLayout.setTabTextColors(Color.parseColor("#999999"),Color.parseColor("#333333"));
                        StatusUtils.setStatusTextColor1(true, PagerPlayerActivity.this);//黑色字体
                    }else{//白色字体
                        if(null!=mBtnBack) mBtnBack.setColorFilter(Color.parseColor("#FFFFFF"));
                        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
                        mTabLayout.setTabTextColors(Color.parseColor("#80FFFFFF"),Color.parseColor("#FFFFFFFF"));
                        StatusUtils.setStatusTextColor1(false, PagerPlayerActivity.this);//黑色字体
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager = (ViewPager) findViewById(R.id.video_pager);
        FragmentPagerAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);

        //抖音缓存
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(PERMISSION_WRITE_EXTERNAL) == PackageManager.PERMISSION_GRANTED) {
//
//            } else {
//                requestPermissions(new String[]{PERMISSION_READ_EXTERNAL,PERMISSION_WRITE_EXTERNAL}, 102);
//            }
//        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 导航播放
     * @param videoJson
     * @param position
     */
    public void navigationPlayer(String videoJson, int position) {
        if(TextUtils.isEmpty(videoJson)) return;
        List<VideoBean> data = new Gson().fromJson(videoJson, new TypeToken<List<VideoBean>>() {}.getType());
        navigationPlayer(data,position);
    }

    /**
     * 导航播放
     * @param data
     * @param position
     */
    public void navigationPlayer(List<VideoBean> data, int position) {
        if(null!=mFragments&&mFragments.size()>1&&null!=mViewPager){
            PagerPlayerFragment fragment = (PagerPlayerFragment) mFragments.get(1);
            mViewPager.setCurrentItem(1,false);
            fragment.navigationPlayer(data,position);
        }
    }

    private class FragmentAdapter extends FragmentPagerAdapter{

        public FragmentAdapter(FragmentManager fm) {
            //setUserVisibleHint和setMaxLifecycle二选一,
            //当传入的behavior为BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT时，会切换到新版本的setMaxLifecycle(@NonNull Fragment fragment,@NonNull Lifecycle.State state)中
            super(fm,BEHAVIOR_SET_USER_VISIBLE_HINT);//这里兼容旧版本的setUserVisibleHint方法
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mFragments.get(position);
            if (fragment == null) {
                switch (position) {
                    default:
                    case 0:
                        fragment = new VideoListFragment();
                        break;
                    case 1:
                        fragment = new PagerPlayerFragment();
                        break;
                }
                mFragments.put(position, fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return null!=mTabs&&mTabs.size()>0?mTabs.size():0;
        }
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
        if(null!=mViewPager&&mViewPager.getCurrentItem()!=0){
            mViewPager.setCurrentItem(0,true);
            return;
        }
        super.onBackPressed();
    }
}
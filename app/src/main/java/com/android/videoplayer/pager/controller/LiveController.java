package com.android.videoplayer.pager.controller;
import android.content.Context;
import com.android.iplayer.base.BaseController;

/**
 * created by hty
 * 2022/8/25
 * Desc:这是一个直播拉流示例的简单控制器
 */
public class LiveController extends BaseController {

    public LiveController(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void initViews() {}
}
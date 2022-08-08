package com.android.videoplayer.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * created by hty
 * 2022/6/29
 * Desc:Fragment基类
 */
public abstract class BaseFragment <P extends BasePresenter> extends Fragment implements BaseContract.BaseView  {

    protected static final String TAG = BaseFragment.class.getSimpleName();
    protected P mPresenter;
    protected abstract int getLayoutID();
    protected abstract void initViews();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutID(),null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter=createPresenter();
        if(null!=mPresenter){
            mPresenter.attachView(this);
        }
        initViews();
    }

    /**
     * 交由子类实现自己指定的Presenter,可以为空
     * @return 子类持有的继承自BasePresenter的Presenter
     */
    protected abstract P createPresenter();

    protected View findViewById(int id){
        return getView(id);
    }

    protected <T extends View> T getView(int id) {
        if (null == getView()) return null;
        return (T) getView().findViewById(id);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mPresenter){
            mPresenter.detachView();
            mPresenter=null;
        }
    }

    protected void onInvisible() {}

    protected void onVisible() {}

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int code, String errorMsg) {

    }
}

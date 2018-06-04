package com.example.jordoncheng.weiboclient_mvp.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.LoadUserContract;
import com.example.jordoncheng.weiboclient_mvp.MainActivity;
import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.Test;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.example.jordoncheng.weiboclient_mvp.presenter.LoadImagePresenter;
import com.example.jordoncheng.weiboclient_mvp.presenter.LoadUserPresenter;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;
import com.example.jordoncheng.weiboclient_mvp.view.widget.StatusAvatarImageView;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.openapi.models.User;

public class HomeActivity extends BaseActivity implements LoadUserContract.View, ImageContract.View {

    private TabLayout mTabLayout;
    private LoadUserContract.Presenter mLoadUserPresenter;
    private ImageContract.Presenter mLoadImagePresenter;

    private TextView statusesCount;
    private TextView friendsCount;
    private TextView followersCount;
    private StatusAvatarImageView avatar;

    @Override
    protected int onSetDimMode() {
        return DIMMODE_1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTransparentBar();

        setContentView(R.layout.activity_home);
        statusesCount = findViewById(R.id.home_user_statuses_count);
        friendsCount = findViewById(R.id.home_user_friends_count);
        followersCount = findViewById(R.id.home_user_followers_count);
        avatar = findViewById(R.id.home_user_avatar);

        ViewPager mViewPager = findViewById(R.id.viewpager);
        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.addOnTabSelectedListener(new PageChangeListener(mViewPager));

        FragmentPagerAdapter mAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        findViewById(R.id.fab).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, CreateStatusActivity.class);
            startActivity(intent);
        });

        setIcon();

        String stringUid = getSharedPreferences("com_weibo_sdk_android", MODE_PRIVATE).getString("uid", null);
        if(stringUid != null) {
            createUserPresenter(new LoadUserPresenter(this, Long.parseLong(stringUid)));
            mLoadUserPresenter.getUser();
        }
        createLoadImagePresenter(Injection.provideLoadImagePresenter());
    }

    @Override
    public void createLoadImagePresenter(ImageContract.Presenter presenter) {
        this.mLoadImagePresenter = presenter;
    }

    @Override
    public void createUserPresenter(LoadUserContract.Presenter presenter) {
        this.mLoadUserPresenter = presenter;
    }

    @Override
    public void onUserStart() {

    }

    @Override
    public void onLoadUserComplete(User result) {
        statusesCount.setText(String.valueOf(result.statuses_count));
        friendsCount.setText(String.valueOf(result.friends_count));
        followersCount.setText(String.valueOf(result.followers_count));
        mLoadImagePresenter.setImage(this, avatar, result.avatar_hd);
    }

    @Override
    public void onLoadUserError() {

    }

    public class PageChangeListener extends TabLayout.ViewPagerOnTabSelectedListener{

        PageChangeListener(ViewPager viewPager) {
            super(viewPager);
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Animation enter = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.tab_enter);
            View tabView = tab.getCustomView();
            if (tabView != null) {
                tabView.startAnimation(enter);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            Animation exit = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.tab_exit);
            View tabView = tab.getCustomView();
            if (tabView != null) {
                tabView.startAnimation(exit);
            }
        }
    }

    public void setIcon() {

        Animation init = AnimationUtils.loadAnimation(this, R.anim.tab_init);
        ImageView tabImageView = new ImageView(this);
        tabImageView.setImageResource(R.mipmap.ic_home_white_24dp);
        mTabLayout.getTabAt(0).setCustomView(tabImageView);
        tabImageView = new ImageView(this);
        tabImageView.setImageResource(R.mipmap.ic_favorite_white_24dp);
        mTabLayout.getTabAt(1).setCustomView(tabImageView);
        tabImageView.startAnimation(init);
        tabImageView = new ImageView(this);
        tabImageView.setImageResource(R.mipmap.ic_person_white_24dp);
        mTabLayout.getTabAt(2).setCustomView(tabImageView);
        tabImageView.startAnimation(init);
        tabImageView = new ImageView(this);
        tabImageView.setImageResource(R.mipmap.ic_earth_white_24dp);
        mTabLayout.getTabAt(3).setCustomView(tabImageView);
        tabImageView.startAnimation(init);
        tabImageView = new ImageView(this);
        tabImageView.setImageResource(R.mipmap.ic_settings_white_24dp);
        mTabLayout.getTabAt(4).setCustomView(tabImageView);
        tabImageView.startAnimation(init);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            TimelineType timelineType;
            switch (position) {
                case 0 : timelineType = TimelineType.HOME_TIMELINE;
                    break;
                case 1 : timelineType = TimelineType.BILATERAL_TIMELINE;
                    break;
                case 3 : timelineType = TimelineType.PUBLIC_TIME;
                    break;
                default: return new EmptyFragment();
            }
            return StatusesFragment.newInstance(timelineType);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}

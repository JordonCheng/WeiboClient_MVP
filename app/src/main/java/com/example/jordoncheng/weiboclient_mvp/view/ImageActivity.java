package com.example.jordoncheng.weiboclient_mvp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;

import java.util.ArrayList;

public class ImageActivity extends BaseActivity implements ImageContract.View {

    private ImageContract.Presenter mLoadImagePresenter;

    @Override
    protected int onSetDimMode() {
        return DIMMODE_1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createLoadImagePresenter(Injection.provideLoadImagePresenter());

        Intent intent = getIntent();
        ArrayList<String> mImageURLSet = intent.getStringArrayListExtra("image urls");

        setContentView(R.layout.image_page);
        ViewPager imagePager = findViewById(R.id.image_pager_view);
        imagePager.setAdapter(new ImageAdapter(mImageURLSet));
        imagePager.setCurrentItem(intent.getIntExtra("position", 0));
    }

    @Override
    public void createLoadImagePresenter(ImageContract.Presenter presenter) {
        this.mLoadImagePresenter = presenter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoadImagePresenter.onViewDestroy(this);
    }

    class ImageAdapter extends PagerAdapter {

        private ArrayList<String> mImageURLSet;

        private ImageAdapter(ArrayList<String> imageURLs) {
            super();
            mImageURLSet = imageURLs;
        }

        @Override
        public int getCount() {
            return mImageURLSet.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageTagView image = new ImageTagView(container.getContext());
            mLoadImagePresenter.setImage(ImageActivity.this, image, mImageURLSet.get(position));
            image.setOnClickListener(v -> ((Activity)v.getContext()).finish());
            container.addView(image);
            return image;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}

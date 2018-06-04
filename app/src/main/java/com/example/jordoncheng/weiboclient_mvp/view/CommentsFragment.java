package com.example.jordoncheng.weiboclient_mvp.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.LoadCommentsContract;
import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.presenter.LoadCommentsPresenter;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;

public class CommentsFragment extends Fragment implements LoadCommentsContract.View, ImageContract.View {

    private static final String CURRENT_MID = "mid";

    private LoadCommentsContract.Presenter mLoadCommentsPresenter;
    private ImageContract.Presenter mLoadImagePresenter;
    private SwipeRefreshLayout rootView;
    private RecyclerView mCommentsRecyclerView;
    private CommentsAdapter mAdapter;

    public static CommentsFragment newInstance(long mid) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putLong(CURRENT_MID, mid);
        fragment.setArguments(args);
        fragment.createLoadImagePresenter(Injection.provideLoadImagePresenter());
        fragment.createLoadCommentsPresenter(new LoadCommentsPresenter(fragment, mid));
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            createLoadImagePresenter(Injection.provideLoadImagePresenter());
            createLoadCommentsPresenter(new LoadCommentsPresenter(this, savedInstanceState.getLong(CURRENT_MID)));
        }
    }

    @Override
    public void createLoadCommentsPresenter(LoadCommentsContract.Presenter presenter) {
        this.mLoadCommentsPresenter = presenter;
    }

    @Override
    public ArrayList<Comment> getCurrentCache() {
        return mAdapter.getComments();
    }

    @Override
    public void onLoadCommentsStart() {
        if(rootView != null) rootView.setRefreshing(true);
    }

    @Override
    public void onLoadCommentsComplete(ArrayList<Comment> result, Boolean isRefresh) {
        if(isRefresh) {
            mAdapter = new CommentsAdapter(result);
            mCommentsRecyclerView.setAdapter(mAdapter);
            rootView.setRefreshing(false);
        } else {
            mAdapter.replaceComments(result);
        }
    }

    @Override
    public void onLoadCommentsError() {
        rootView.setRefreshing(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (SwipeRefreshLayout)inflater.inflate(R.layout.cmt_repost_list, container, false);
        mCommentsRecyclerView = rootView.findViewById(R.id.status_recyclerview);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        rootView.setOnRefreshListener(() -> mLoadCommentsPresenter.getCommentsRefresh());

        rootView.findViewById(R.id.click_view).setOnClickListener(v -> getActivity().finish());

        ((ViewPager)getActivity().getWindow().getDecorView().findViewById(R.id.viewpager)).addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1 && mAdapter == null) {
                    mLoadCommentsPresenter.getCommentsRefresh();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoadCommentsPresenter.onViewDestroy();
        mLoadImagePresenter.onViewDestroy(this);
    }

    @Override
    public void createLoadImagePresenter(ImageContract.Presenter presenter) {
        this.mLoadImagePresenter = presenter;
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.VH> {

        private ArrayList<Comment> mComments;

        CommentsAdapter(ArrayList<Comment> initialData) {
            this.mComments = initialData;
        }

        ArrayList<Comment> getComments() {
            return mComments;
        }

        private void replaceComments(ArrayList<Comment> comments) {
            this.mComments = comments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cmt_repost_list_item, parent, false);
            return new VH(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {

            holder.comment = mComments.get(position);
            User user = holder.comment.user;
            /*holder.userView.setOnClickListener(new UserPopupOnClickListner(user));*/
            String userImageURL = user.avatar_large;
            mLoadImagePresenter.setImage(CommentsFragment.this, (ImageTagView)holder.avatarView, userImageURL);
            holder.screenName.setText("@" + holder.comment.user.screen_name);
            holder.contentTv.setText(holder.comment.text);

            if (position == mComments.size() - 2) {
                mLoadCommentsPresenter.getMoreComments();
            }
        }


        @Override
        public int getItemCount() {
            return mComments.size();
        }

        @Override
        public void onViewRecycled(@NonNull VH holder) {
            super.onViewRecycled(holder);
            holder.avatarView.setImageBitmap(null);
            ((ImageTagView) holder.avatarView).setImageTag(0);
        }

        class VH extends RecyclerView.ViewHolder {

            Comment comment;
            ImageView avatarView;
            TextView screenName;
            TextView contentTv;
            View userView;

            VH(View itemView) {
                super(itemView);

                avatarView = itemView.findViewById(R.id.list_item_cmt_repost_avatar);
                screenName = itemView.findViewById(R.id.list_item_cmt_repost_screen_name);
                contentTv = itemView.findViewById(R.id.item_cmt_repost_tv);
                userView = itemView.findViewById(R.id.cmt_repost_user);
            }
        }
    }
}

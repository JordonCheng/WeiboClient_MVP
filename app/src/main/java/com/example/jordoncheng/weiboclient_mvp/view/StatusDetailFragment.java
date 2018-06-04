package com.example.jordoncheng.weiboclient_mvp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.PublishContract;
import com.example.jordoncheng.weiboclient_mvp.PublishType;
import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.presenter.PublishPresenter;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;
import com.example.jordoncheng.weiboclient_mvp.view.widget.InputScrollView;
import com.example.jordoncheng.weiboclient_mvp.view.widget.StatusImageLayout;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JrDnCh on 3/5/2018.
 */

public class StatusDetailFragment extends Fragment implements ImageContract.View, PublishContract.View {

    private ImageContract.Presenter mLoadImagePresenter;
    private PublishContract.Presenter mPublishPresenter;

    private boolean isLegal;
    private AppCompatEditText inputBox;
    private TextView charCount;

    public static StatusDetailFragment newInstance() {
        StatusDetailFragment fragment = new StatusDetailFragment();
        fragment.createLoadImagePresenter(Injection.provideLoadImagePresenter());
        fragment.createPublishPresenter(new PublishPresenter(fragment, PublishType.COMMENT));
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            createLoadImagePresenter(Injection.provideLoadImagePresenter());
            createPublishPresenter(new PublishPresenter(this, PublishType.COMMENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_status_page, container, false);
        charCount = rootView.findViewById(R.id.line_edit_char_count);
        inputBox = rootView.findViewById(R.id.input_box);

        //设置发送按钮的监听事件
        rootView.findViewById(R.id.line_edit_send).setOnClickListener(v -> {
            if(isLegal) mPublishPresenter.sendStatus();
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.status_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        ArrayList<Status> status = new ArrayList<>();
        status.add(getActivity().getIntent().getParcelableExtra("status"));
        StatusAdapter adapter = new StatusAdapter(status);
        recyclerView.setAdapter(adapter);

        rootView.findViewById(R.id.click_view).setOnClickListener(v -> getActivity().finish());

        final InputScrollView scrollView = rootView.findViewById(R.id.scroll_view);
        scrollView.setSizeChangedListener((w, h, oldw, oldh) -> {
            if(oldw != 0) {
                Log.d("OnSizeChangedListener", "w = " + String.valueOf(w) + ", "
                        + "h = " + String.valueOf(h) + ", "
                        + "oldw = " + String.valueOf(oldw) + ", "
                        + "oldh = " + String.valueOf(oldh));
                Looper.myQueue().addIdleHandler(() -> {
                    scrollView.scrollTo(0, 5000);
                    return false;
                });
            }
        });

        mPublishPresenter.start();
        return rootView;
    }

    @Override
    public void createLoadImagePresenter(ImageContract.Presenter presenter) {
        this.mLoadImagePresenter = presenter;
    }

    @Override
    public void createPublishPresenter(PublishContract.Presenter presenter) {
        this.mPublishPresenter = presenter;
    }

    @Override
    public EditText getEditText() {
        return inputBox;
    }

    @Override
    public void onReplacePictureComplete(Uri uri) {

    }

    @Override
    public void onAddPictureComplete(Uri uri) {

    }

    @Override
    public void onTextCountChanged(int count, boolean isLegal) {
        charCount.setText(String.valueOf(count));
        this.isLegal = isLegal;
    }

    @Override
    public void onSendStatusStart() {

    }

    @Override
    public void onSendStatusComplete() {
        inputBox.setText(null);
    }

    @Override
    public void onSendStatusFailed() {

    }

    private class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.VH> {

        private static final int HAS_NO_IMAGE_NO_RETWEET = 100001;
        private static final int HAS_IMAGE_NO_RETWEET = 100002;
        private static final int HAS_RETWEET_NO_IMAGE = 100003;
        private static final int HAS_RETWEET_AND_IMAGE = 100004;

        private ArrayList<Status> mStatuses;

        private StatusAdapter(ArrayList<Status> initialData) {
            this.mStatuses = initialData;
        }

        /*public ArrayList<Status> getStatuses() {
            return mStatuses;
        }

        private void replaceStatuses(ArrayList<Status> statuses) {
            this.mStatuses = statuses;
            notifyDataSetChanged();
        }*/

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = null;
            switch (viewType) {
                case HAS_IMAGE_NO_RETWEET: itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.status_list_item_contain_image_no_repost, parent, false);
                    break;
                case HAS_NO_IMAGE_NO_RETWEET: itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.status_list_item_only_text, parent, false);
                    break;
                case HAS_RETWEET_NO_IMAGE: itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.status_list_item_repost_no_image, parent, false);
                    break;
                case HAS_RETWEET_AND_IMAGE: itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.status_list_item_repost_has_image, parent, false);
                    break;
            }
            return new VH(itemView, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {

            holder.imageViewHolder.clear();

            Status status = mStatuses.get(position);
        /*holder.statusTextContent.setOnClickListener(new TVOnClickListener(status));
        holder.statusUser.setOnClickListener(new UserPopupOnClickListner(status.user));*/

            mLoadImagePresenter.setImage(StatusDetailFragment.this, (ImageTagView)holder.statusAvatar, status.user.avatar_large);
            holder.imageViewHolder.add(holder.statusAvatar);

            holder.statusScreenName.setText("@" + status.user.screen_name);
            holder.statusName.setText(status.user.name);
            holder.statusTextContent.setText(status.text);
            holder.statusRepostCount.setText(String.valueOf(status.reposts_count));
            holder.statusAttitudesCount.setText(String.valueOf(status.attitudes_count));

            if(holder.viewType == HAS_RETWEET_NO_IMAGE || holder.viewType == HAS_RETWEET_AND_IMAGE)
            {
                Status repost = status.retweeted_status;
                holder.repostTextContent.setOnClickListener(new DetailOnClickListener(repost));
                /*holder.repostUser.setOnClickListener(new UserPopupOnClickListner(repost.user));*/

                mLoadImagePresenter.setImage(StatusDetailFragment.this, (ImageTagView)holder.repostAvatar, repost.user.avatar_large);
                holder.imageViewHolder.add(holder.repostAvatar);

                holder.repostScreenName.setText("@" + repost.user.screen_name);
                holder.repostTextContent.setText(repost.text);
                holder.repostRepostCount.setText(String.valueOf(repost.reposts_count));
                holder.repostCommentsCount.setText(String.valueOf(repost.comments_count));
                holder.repostAttitudesCount.setText(String.valueOf(repost.attitudes_count));
            }
            else if(holder.viewType == HAS_IMAGE_NO_RETWEET)
            {
                ArrayList<String> picURLs = status.pic_urls;
                int picCount = picURLs.size();
                ((StatusImageLayout)holder.imageViews).setImageCount(picCount);

                for(int i = 0; i < picCount; i++) {
                    String url = picURLs.get(i);
                    Matcher m1 = Pattern.compile("thumbnail").matcher(url);
                    String urlImage = m1.replaceFirst("bmiddle");
                    ImageView child = (ImageView)holder.imageViews.getChildAt(i);
                    child.setOnClickListener(new ImageOnClickListener(picURLs, i));
                    mLoadImagePresenter.setImage(StatusDetailFragment.this, (ImageTagView)child, urlImage);
                    holder.imageViewHolder.add(child);
                }
            }

            if(holder.viewType == HAS_RETWEET_AND_IMAGE) {
                ArrayList<String> picURLs = status.retweeted_status.pic_urls;
                int picCount = picURLs.size();
                ((StatusImageLayout)holder.imageViews).setImageCount(picCount);

                for(int i = 0; i < picCount; i++) {
                    String url = picURLs.get(i);
                    Matcher m1 = Pattern.compile("thumbnail").matcher(url);
                    String imageURL = m1.replaceFirst("bmiddle");
                    ImageView child = (ImageView)holder.imageViews.getChildAt(i);
                    child.setOnClickListener(new ImageOnClickListener(picURLs, i));
                    mLoadImagePresenter.setImage(StatusDetailFragment.this, (ImageTagView)child, imageURL);
                    holder.imageViewHolder.add(child);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mStatuses.size();
        }

        @Override
        public int getItemViewType(int position) {

            Status status = mStatuses.get(position);
            int viewType = 0;

            if(status.pic_urls == null && status.retweeted_status == null) viewType = HAS_NO_IMAGE_NO_RETWEET;
            if(status.pic_urls != null) viewType = HAS_IMAGE_NO_RETWEET;
            if(status.retweeted_status != null)
            {
                if(status.retweeted_status.pic_urls == null) viewType = HAS_RETWEET_NO_IMAGE;
                else viewType = HAS_RETWEET_AND_IMAGE;
            }
            return viewType;
        }

        class VH extends RecyclerView.ViewHolder {

            int viewType;
            ArrayList<ImageView> imageViewHolder;
            ViewGroup imageViews;

            View statusUser; ImageView statusAvatar; TextView statusScreenName; TextView statusName;
            TextView statusTextContent;
            TextView statusRepostCount; TextView statusAttitudesCount;

            View repostUser; ImageView repostAvatar; TextView repostScreenName;
            TextView repostTextContent;
            TextView repostRepostCount; TextView repostCommentsCount; TextView repostAttitudesCount;

            VH(View itemView, int viewType) {
                super(itemView);

                this.viewType = viewType;
                imageViewHolder = new ArrayList<>();
                statusUser = itemView.findViewById(R.id.line_list_item_user_on_large);
                statusAvatar = itemView.findViewById(R.id.line_list_item_avatar_on_large);
                statusScreenName = itemView.findViewById(R.id.line_list_item_screen_name_on_large);
                statusName = itemView.findViewById(R.id.line_list_item_name_on_large);
                statusTextContent = itemView.findViewById(R.id.line_list_item_text_content_on_status);
                statusRepostCount = itemView.findViewById(R.id.line_list_item_repost_count_on_status);
                statusAttitudesCount = itemView.findViewById(R.id.line_list_item_attitudes_count_on_status);

                switch (viewType) {
                    case HAS_IMAGE_NO_RETWEET :
                        imageViews = itemView.findViewById(R.id.line_list_item_status_image);
                        break;
                    case HAS_RETWEET_NO_IMAGE :
                        repostUser = itemView.findViewById(R.id.line_list_item_user_on_small);
                        repostAvatar = itemView.findViewById(R.id.line_list_item_avatar_on_small);
                        repostScreenName = itemView.findViewById(R.id.line_list_item_screen_name_on_small);
                        repostTextContent = itemView.findViewById(R.id.line_list_item_text_content_for_repost);
                        repostRepostCount = itemView.findViewById(R.id.line_list_item_repost_count_on_repost);
                        repostCommentsCount = itemView.findViewById(R.id.line_list_item_comments_count_on_repost);
                        repostAttitudesCount = itemView.findViewById(R.id.line_list_item_attitudes_count_on_repost);
                        break;
                    case HAS_RETWEET_AND_IMAGE :
                        imageViews = itemView.findViewById(R.id.line_list_item_status_image);
                        repostUser = itemView.findViewById(R.id.line_list_item_user_on_small);
                        repostAvatar = itemView.findViewById(R.id.line_list_item_avatar_on_small);
                        repostScreenName = itemView.findViewById(R.id.line_list_item_screen_name_on_small);
                        repostTextContent = itemView.findViewById(R.id.line_list_item_text_content_for_repost);
                        repostRepostCount = itemView.findViewById(R.id.line_list_item_repost_count_on_repost);
                        repostCommentsCount = itemView.findViewById(R.id.line_list_item_comments_count_on_repost);
                        repostAttitudesCount = itemView.findViewById(R.id.line_list_item_attitudes_count_on_repost);
                        break;
                }
            }
        }
    }

    private class ImageOnClickListener implements View.OnClickListener {

        ArrayList<String> picURLs;
        int position;

        ImageOnClickListener(ArrayList<String> picURLs, int position) {
            this.picURLs = picURLs;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), ImageActivity.class);
            intent.putStringArrayListExtra("image urls", picURLs);
            intent.putExtra("position", position);
            v.getContext().startActivity(intent);
        }
    }

    private class DetailOnClickListener implements View.OnClickListener {

        Status status;

        DetailOnClickListener(Status status) {
            this.status = status;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("status", status);
            v.getContext().startActivity(intent);
        }
    }
}


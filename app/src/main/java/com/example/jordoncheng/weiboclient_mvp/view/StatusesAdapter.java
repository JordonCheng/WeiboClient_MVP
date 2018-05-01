package com.example.jordoncheng.weiboclient_mvp.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.StatusesContract;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;
import com.example.jordoncheng.weiboclient_mvp.view.widget.StatusImageLayout;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.VH> implements StatusesContract.View {

    private static final int HAS_NO_IMAGE_NO_RETWEET = 100001;
    private static final int HAS_IMAGE_NO_RETWEET = 100002;
    private static final int HAS_RETWEET_NO_IMAGE = 100003;
    private static final int HAS_RETWEET_AND_IMAGE = 100004;

    private StatusesContract.Presenter mStatusesPresenter;
    private ArrayList<Status> mStatuses;

    public StatusesAdapter(StatusesContract.Presenter presenter, StatusList initialData) {
        mStatusesPresenter = presenter;
        mStatuses = initialData.statusList;
    }

    @Override
    public void setStatuses(StatusList result) {
        mStatuses = result.statusList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusesAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case HAS_IMAGE_NO_RETWEET : itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_list_item_contain_image_no_repost, parent, false);
                break;
            case HAS_NO_IMAGE_NO_RETWEET : itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_list_item_only_text, parent, false);
                break;
            case HAS_RETWEET_NO_IMAGE : itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_list_item_repost_no_image, parent, false);
                break;
            case HAS_RETWEET_AND_IMAGE : itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_list_item_repost_has_image, parent, false);
                break;
        }
        return new VH(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusesAdapter.VH holder, int position) {

        if(holder == null) return;

        holder.imageViewHolder.clear();

        Status status = mStatuses.get(position);
        holder.statusTextContent.setOnClickListener(new TVOnClickListener(status));
        holder.statusUser.setOnClickListener(new UserPopupOnClickListner(status.user));

        loadImage((ImageTagView)holder.statusAvatar, status.user.avatar_large);
        holder.imageViewHolder.add(holder.statusAvatar);

        holder.statusScreenName.setText("@" + status.user.screen_name);
        holder.statusName.setText(status.user.name);
        holder.statusTextContent.setText(status.text);
        holder.statusRepostCount.setText(status.reposts_count + "");
        holder.statusAttitudesCount.setText(status.attitudes_count + "");

        if(holder.viewType == HAS_RETWEET_NO_IMAGE || holder.viewType == HAS_RETWEET_AND_IMAGE)
        {
            Status repost = status.retweeted_status;
            holder.repostTextContent.setOnClickListener(new TVOnClickListener(repost));
            holder.repostUser.setOnClickListener(new UserPopupOnClickListner(repost.user));

            loadImage((ImageTagView)holder.repostAvatar, repost.user.avatar_large);
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
                loadImage((ImageTagView)child, urlImage);
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
                loadImage((ImageTagView)child, imageURL);
                holder.imageViewHolder.add(child);
            }
        }

        if(position == mStatuses.size() - 2) {
            mStatusesPresenter.getMoreStatuses();
        }
    }

    @Override
    public int getItemCount() {
        return mStatuses.size();
    }

    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
        int count = holder.imageViewHolder.size();
        ImageTagView view;
        for (int i = 0; i < count; i++) {
            view = (ImageTagView)holder.imageViewHolder.get(i);
            view.setImageTag(0);
            view.setImageBitmap(null);
            view.setClickable(false);
        }
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

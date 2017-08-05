package com.codepath.tayousyl.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.tayousyl.R;
import com.codepath.tayousyl.activities.TweetDetailsActivity;
import com.codepath.tayousyl.constants.Extras;
import com.codepath.tayousyl.models.Entities;
import com.codepath.tayousyl.models.Media;
import com.codepath.tayousyl.models.Tweet;
import com.codepath.tayousyl.models.TwitterUser;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class TweetsAdapter extends ArrayAdapter<Tweet> {
    private static final PrettyTime PRETTY_TIME = new PrettyTime();
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tweet tweet = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
        }
        setupMedia(convertView, tweet);
        TextView tvTweetId = (TextView) convertView.findViewById(R.id.tvTweetId);
        tvTweetId.setText(String.valueOf(tweet.getId()));
        convertView.setTag(String.valueOf(position));
        TextView tvTweetText = (TextView) convertView.findViewById(R.id.tvTweetText);
        tvTweetText.setText(Html.fromHtml(tweet.getText()), TextView.BufferType.SPANNABLE);
        ImageView ivUserPhoto = (ImageView) convertView.findViewById(R.id.ivUserPhoto);
        TwitterUser user = tweet.getUser();
        Picasso.with(getContext()).load(user.getProfileImageUrl()).into(ivUserPhoto);
        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        tvUserName.setText(user.getName());
        TextView tvUserScreenName = (TextView) convertView.findViewById(R.id.tvUserScreenName);
        tvUserScreenName.setText("@" + user.getScreenName());
        TextView tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
        try {
            tvCreatedAt.setText(PRETTY_TIME.format(FORMATTER.parse(tweet.getCreatedAt())));
        } catch (ParseException e) {
            Log.d("TWEET", "uh oh", e);
        }

        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    TextView tvTweetId = (TextView) v.findViewById(R.id.tvTweetId);
                    Long id = Long.parseLong(tvTweetId.getText().toString());
                    Intent intent = new Intent(getContext(), TweetDetailsActivity.class);
                    intent.putExtra(Extras.TWEET_ID, id);
                    getContext().startActivity(intent);
                }
                return true;
            }
        });
        return convertView;
    }

    private void setupMedia(View view, Tweet tweet) {
        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        ivPhoto.setImageResource(0);
        ivPhoto.setVisibility(View.GONE);
        Entities entities = tweet.getEntities();
        if (entities != null) {
            List<Media> mediaList = entities.getMedia();
            for (Media media : mediaList) {
                if (media.isPhoto()) {
                    Picasso.with(getContext()).load(media.getMediaUrl()).into(ivPhoto);
                    ivPhoto.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}

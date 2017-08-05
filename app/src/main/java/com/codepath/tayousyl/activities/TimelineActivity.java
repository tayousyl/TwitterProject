package com.codepath.tayousyl.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.codepath.tayousyl.R;
import com.codepath.tayousyl.TwitterApplication;
import com.codepath.tayousyl.TwitterClient;
import com.codepath.tayousyl.adapters.TweetsAdapter;
import com.codepath.tayousyl.fragments.ComposeTweetFragment;
import com.codepath.tayousyl.listeners.EndlessScrollListener;
import com.codepath.tayousyl.models.Tweet;


import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetFragment.StatusUpdateListener {
    private TwitterClient client;
    private TweetsAdapter aTweets;
    private List<Tweet> tweets;
    private ListView lvTweets;
    //private RecyclerView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    private ComposeTweetFragment composeTweetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.twitter_logo);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                composeTweetFragment = new ComposeTweetFragment();
                composeTweetFragment.show(fragmentManager, "COMPOSE_TWEET");
                composeTweetFragment.setListener(TimelineActivity.this);
            }
        });

        setupSwitchRefreshLayout();
       lvTweets = (ListView) findViewById(R.id.lvTweets);


        tweets = new ArrayList<>();
        aTweets = new TweetsAdapter(this, tweets);
        lvTweets.setAdapter(aTweets);

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Long sinceId = getOldestTweetId();
                client.getOlderHomeTimeline(new TwitterClient.TimelineResponseHandler() {
                    @Override
                    public void onSuccess(List<Tweet> tweets) {
                        aTweets.addAll(tweets.isEmpty() ? tweets : tweets.subList(1, tweets.size()));
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        logError(error);
                    }
                }, sinceId);
                return true;
            }
        });
        client = TwitterApplication.getRestClient();
        populateTimeline();
    }

    private Long getOldestTweetId() {
        if (tweets.size() == 0) {
            return 1L;
        } else {
            Tweet tweet = tweets.get(tweets.size() - 1);
            return tweet.getId();
        }
    }

    private void populateTimeline() {
        client.getHomeTimeline(new TwitterClient.TimelineResponseHandler() {
            @Override
            public void onSuccess(List<Tweet> tweets) {
                aTweets.clear();
                TimelineActivity.this.tweets.addAll(tweets);
                aTweets.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable error) {
                swipeContainer.setRefreshing(false);
                logError(error);
            }
        });
    }

    private void logError(Throwable error) {
        Log.d("TIMELINE", "Failed to retrieve tweets", error);
    }

    private void setupSwitchRefreshLayout() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    @Override
    public void onStatusUpdated() {
        if (composeTweetFragment != null) {
            composeTweetFragment.dismiss();
        }
        lvTweets.smoothScrollToPosition(0);
        populateTimeline();
    }
}

package ru.xrystalll.goload.fullviewer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.squareup.picasso.Picasso;

import ru.xrystalll.goload.R;

public class FullscreenActivity extends AppCompatActivity {

    private float xCoOrdinate, yCoOrdinate;
    private double screenCenterX, screenCenterY;
    private int alpha;
    private ImageView imageView;
    private View view;
    private SimpleExoPlayer exoPlayer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        imageView = findViewById(R.id.imagePreview);
        SimpleExoPlayerView exoPlayerView = findViewById(R.id.videoPreview);
        view = findViewById(R.id.layout);
        view.getBackground().setAlpha(255);

        if (getIntent().hasExtra("passingImage")) {
            String file = getIntent().getStringExtra("passingImage");

            Picasso.get()
                    .load(file)
                    .into(imageView);
        } else if (getIntent().hasExtra("passingVideo")) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            imageView.setVisibility(View.GONE);
            exoPlayerView.setVisibility(View.VISIBLE);
            String file = getIntent().getStringExtra("passingVideo");
            long position = getIntent().getLongExtra("passingPosition", 0);
            ImageView fullscreenButton = exoPlayerView.findViewById(R.id.exo_fullscreen_icon);

            try {
                fullscreenButton.setImageDrawable(getResources().getDrawable(R.drawable.exo_controls_fullscreen_exit));
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
                Uri uri = Uri.parse(file);
                DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("goload_video");
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null,
                        null);
                exoPlayerView.setPlayer(exoPlayer);
                exoPlayer.prepare(mediaSource);
                exoPlayer.seekTo(position);
                exoPlayer.setPlayWhenReady(true);

                fullscreenButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final DisplayMetrics display = getResources().getDisplayMetrics();
        screenCenterX = display.widthPixels / 2;
        screenCenterY = (display.heightPixels - getStatusBarHeight()) / 2;
        final double maxHypo = Math.hypot(screenCenterX, screenCenterY);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                double centerYPos = imageView.getY() + (imageView.getHeight() / 2);
                double centerXPos = imageView.getX() + (imageView.getWidth() / 2);
                double a = screenCenterX - centerXPos;
                double b = screenCenterY - centerYPos;
                double hypo = Math.hypot(a, b);

                alpha = (int) (hypo * 255) / (int) maxHypo;
                if (alpha < 255)
                    view.getBackground().setAlpha(255 - alpha);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        xCoOrdinate = imageView.getX() - event.getRawX();
                        yCoOrdinate = imageView.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        imageView.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (alpha > 70) {
                            supportFinishAfterTransition();
                            return false;
                        } else {
                            imageView.animate().x(0).y((float) screenCenterY - imageView.getHeight() / 2).setDuration(100).start();
                            view.getBackground().setAlpha(255);
                        }
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void pausePlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    private void playPlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }

}

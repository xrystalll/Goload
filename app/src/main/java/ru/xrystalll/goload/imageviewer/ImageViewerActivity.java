package ru.xrystalll.goload.imageviewer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import ru.xrystalll.goload.R;

public class ImageViewerActivity extends AppCompatActivity {

    private float xCoOrdinate, yCoOrdinate;
    private double screenCenterX, screenCenterY;
    private int alpha;
    ImageView imageView;
    View view;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

        imageView = findViewById(R.id.imagePreview);
        view = findViewById(R.id.layout);
        view.getBackground().setAlpha(255);

        if (getIntent().hasExtra("passingImage")) {
            String file = getIntent().getStringExtra("passingImage");

            Picasso.get()
                    .load(file)
                    .into(imageView);
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}

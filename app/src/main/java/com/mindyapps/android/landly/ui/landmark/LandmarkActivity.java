package com.mindyapps.android.landly.ui.landmark;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mindyapps.android.landly.R;
import com.mindyapps.android.landly.db.LandmarkEntity;
import com.mindyapps.android.landly.models.Landmark;
import com.mindyapps.android.landly.repositories.LandmarkRepository;
import com.mindyapps.android.landly.viewmodels.ViewModelProviderFactory;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

import static com.mindyapps.android.landly.util.Constants.EXTRA_LANDMARK_IMAGE_TRANSITION_NAME;
import static com.mindyapps.android.landly.util.Constants.IMAGE_URL_EXTRA;
import static com.mindyapps.android.landly.util.Constants.LANDMARK_ENTITY_EXTRA;
import static com.mindyapps.android.landly.util.Constants.LANDMARK_EXTRA;
import static com.mindyapps.android.landly.util.Constants.POSITION_EXTRA;

public class LandmarkActivity extends DaggerAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LandmarkActivity";
    private TextView tvUserName, tvLikes, tvViews;
    private ImageView landmarkImage, userImage;
    private Landmark landmark;
    private LandmarkEntity landmarkEntity;
    private boolean isFavourite;
    private int position;
    private String pageUrl, imageUrl;

    @Inject
    RequestManager requestManager;

    @Inject
    ViewModelProviderFactory providerFactory;

    private LandmarkViewModel landmarkViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);
        landmarkViewModel = ViewModelProviders.of(this, providerFactory).get(LandmarkViewModel.class);

        tvUserName = findViewById(R.id.user_name);
        tvLikes = findViewById(R.id.likes_count);
        tvViews = findViewById(R.id.views_count);
        landmarkImage = findViewById(R.id.landmark_image);
        userImage = findViewById(R.id.user_image);

        setExtras();
        subscribeObservers();
        supportPostponeEnterTransition();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = getIntent().getExtras()
                    .getString(EXTRA_LANDMARK_IMAGE_TRANSITION_NAME);
            landmarkImage.setTransitionName(imageTransitionName);
        }

        bindView();
    }

    private void subscribeObservers() {
        landmarkViewModel.getAllLandmarkEntities().observe(this, new Observer<List<LandmarkEntity>>() {
            @Override
            public void onChanged(List<LandmarkEntity> landmarkEntities) {
                int count;
                if (landmarkEntity != null){
                    count = landmarkViewModel.getLandmarkByUrl(landmarkEntity.getPageUrl());
                } else {
                    count = landmarkViewModel.getLandmarkByUrl(landmark.getPageUrl(position));
                }
                if (count == 1){
                    isFavourite = true;
                    invalidateOptionsMenu();
                }
            }
        });
    }

    private void setExtras() {
        landmark = getIntent().getParcelableExtra(LANDMARK_EXTRA);
        landmarkEntity = getIntent().getParcelableExtra(LANDMARK_ENTITY_EXTRA);
        position = getIntent().getIntExtra(POSITION_EXTRA, 0);
    }

    private void bindView() {
        if (landmark != null) {
            setTitle(landmark.getName());
            tvUserName.setText(landmark.getUserName(position));
            tvLikes.setText(String.valueOf(landmark.getLikes(position)));
            tvViews.setText(String.valueOf(landmark.getViews(position)));
            setImage(landmark.getUserImage(position), userImage);
            setImage(landmark.getImageUrl(position), landmarkImage);
            pageUrl = landmark.getPageUrl(position);
            imageUrl = landmark.getImageUrl(position);
        } else {
            isFavourite = true;
            setTitle(landmarkEntity.getLandmarkName());
            tvUserName.setText(landmarkEntity.getUserName());
            tvLikes.setText(String.valueOf(landmarkEntity.getLikes()));
            tvViews.setText(String.valueOf(landmarkEntity.getViews()));
            setImage(landmarkEntity.getUserImageUrl(), userImage);
            setImage(landmarkEntity.getImageUrl(), landmarkImage);
            pageUrl = landmarkEntity.getPageUrl();
            imageUrl = landmarkEntity.getImageUrl();
        }
        landmarkImage.setOnClickListener(this);
    }

    private void setImage(String imageUrl, final ImageView imageView) {
        requestManager
                .load(imageUrl)
                .fitCenter()
                .error(R.color.colorPrimaryDark)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isFavourite) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.landmark_menu, menu);
        } else {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.landmark_menu_delete, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_in_browser:
                Intent pageIntent = new Intent(Intent.ACTION_VIEW);
                pageIntent.setData(Uri.parse(pageUrl));
                startActivity(pageIntent);
                return true;
            case R.id.add_to_favourites:
                addToFavourites();
                return true;
            case R.id.delete_landmark:
                deleteLandmark();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteLandmark() {
        if (landmarkEntity != null){
            landmarkViewModel.delete(landmarkEntity.getPageUrl());
        } else {
            landmarkViewModel.delete(landmark.getPageUrl(position));
        }
    }

    private void addToFavourites() {
        landmarkEntity = new LandmarkEntity(
                landmark.getUserName(position), landmark.getName(), landmark.getUserImage(position),
                landmark.getImageUrl(position), landmark.getPageUrl(position),
                landmark.getLikes(position), landmark.getViews(position));
        landmarkViewModel.insert(landmarkEntity);
        isFavourite = true;
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.landmark_image:
                Intent intent = new Intent(this, FullScreenActivity.class);
                intent.putExtra(IMAGE_URL_EXTRA, imageUrl);
                startActivity(intent);
                break;
        }
    }
}

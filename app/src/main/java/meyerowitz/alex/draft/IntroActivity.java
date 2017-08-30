package meyerowitz.alex.draft;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class IntroActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 0;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 1;

    private static VideoView intro_videoView;
    private ImageView intro_imageView;
    private ImageView draft_imageView;
    private Button continue_button;
    private Button teacher_button;
    private Button student_button;
    private Toolbar intro_toolbar;
    private Button oceanside_button;

    private FirebaseAuth auth;
    private DatabaseReference database;

    private int longAnimationDuration;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initTransitions();

        longAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        path = "android.resource://" + getPackageName() + "/" + R.raw.blurred;
        intro_videoView = (VideoView) findViewById(R.id.videoView_intro);
        intro_videoView.setVideoURI(Uri.parse(path));
        intro_videoView.setOnCompletionListener(e -> intro_videoView.start());
        intro_videoView.setOnTouchListener((a, b) -> {
            ArrayList<View> in = new ArrayList<>();
            in.add(teacher_button);
            in.add(student_button);
            in.add(intro_toolbar);
            in.add(intro_imageView);
            in.add(oceanside_button);

            ArrayList<View> out = new ArrayList<>();
            out.add(continue_button);

            crossfade(in, out);

            intro_videoView.setOnTouchListener(null);
            intro_videoView.setVideoURI(null);
            return false;
        });
        intro_videoView.start();

        draft_imageView = (ImageView) findViewById(R.id.imageView_draft);
        draft_imageView.setImageResource(R.drawable.draft);

        continue_button = (Button) findViewById(R.id.button_continue);
        continue_button.setTransformationMethod(null);
        continue_button.setOnClickListener(e -> {
            ArrayList<View> in = new ArrayList<>();
            in.add(teacher_button);
            in.add(student_button);
            in.add(intro_toolbar);
            in.add(intro_imageView);
            in.add(oceanside_button);

            ArrayList<View> out = new ArrayList<>();
            out.add(continue_button);

            intro_videoView.setOnTouchListener(null);
            intro_videoView.setVideoURI(null);

            crossfade(in, out);
        });

        intro_imageView = (ImageView) findViewById(R.id.imageView_intro);
        intro_imageView.setImageResource(R.drawable.blurred_background);
        intro_imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        intro_toolbar = (Toolbar) findViewById(R.id.toolbar);
        intro_toolbar.setTitle("Login");

        teacher_button = (Button) findViewById(R.id.button_teacher);
        teacher_button.setOnClickListener(e -> {
            Intent i = new Intent(IntroActivity.this, TeacherLoginActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(IntroActivity.this,
                    Pair.create(teacher_button, teacher_button.getTransitionName()));
            startActivity(i, options.toBundle());
        });

        student_button = (Button) findViewById(R.id.button_student);
        student_button.setOnClickListener(e -> {
            Intent i = new Intent(IntroActivity.this, StudentLoginActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(IntroActivity.this,
                    Pair.create(student_button, student_button.getTransitionName()));
            startActivity(i, options.toBundle());
        });

        oceanside_button = (Button) findViewById(R.id.button_oceanside);
        oceanside_button.setTransformationMethod(null);
        oceanside_button.setOnClickListener(e -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://oceansideschools.org/"));
            startActivity(i);
        });

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("draft").exists()) {
                    database.child("draft").setValue(-1);
                }
            }
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE},
                MY_PERMISSIONS_REQUEST_INTERNET);
    }

    private void crossfade(ArrayList<View> in, ArrayList<View> out) {
        for(ListIterator<View> l = in.listIterator(); l.hasNext();) {
            final View view = l.next();

            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .alpha(1f)
                    .setDuration(longAnimationDuration)
                    .setListener(null);
        }

        for(ListIterator<View> l = out.listIterator(); l.hasNext();) {
            final View view = l.next();

            view.animate()
                    .alpha(0f)
                    .setDuration(longAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void initTransitions() {
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);
    }
}

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
    private ImageView intro_imageView;
    private ImageView draft_imageView;
    private Button teacher_button;
    private Button student_button;
    private Toolbar intro_toolbar;
    private Button oceanside_button;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initTransitions();
        database = FirebaseDatabase.getInstance().getReference();

        draft_imageView = findViewById(R.id.imageView_draft);
        draft_imageView.setImageResource(R.drawable.draft);

        intro_imageView = findViewById(R.id.imageView_intro);
        intro_imageView.setImageResource(R.drawable.blurred_background);
        intro_imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        intro_toolbar = findViewById(R.id.toolbar);
        intro_toolbar.setTitle("Login");

        teacher_button = findViewById(R.id.button_teacher);
        teacher_button.setOnClickListener(e -> {
            Intent i = new Intent(IntroActivity.this, TeacherLoginActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(IntroActivity.this,
                    Pair.create(teacher_button, teacher_button.getTransitionName()));
            startActivity(i, options.toBundle());
        });

        student_button = findViewById(R.id.button_student);
        student_button.setOnClickListener(e -> {
            Intent i = new Intent(IntroActivity.this, StudentLoginActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(IntroActivity.this,
                    Pair.create(student_button, student_button.getTransitionName()));
            startActivity(i, options.toBundle());
        });

        oceanside_button = findViewById(R.id.button_oceanside);
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

    private void initTransitions() {
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);
    }
}

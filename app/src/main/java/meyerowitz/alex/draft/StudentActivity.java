package meyerowitz.alex.draft;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentActivity extends Activity {
    private Button oceanside_button;
    private Toolbar student_toolbar;
    private com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar draft_progressBar;
    private TextView draft_textView;
    private ImageView first_imageView;
    private TextView first_textView;
    private ImageView second_imageView;
    private TextView second_textView;
    private ImageView third_imageView;
    private TextView third_textView;

    private boolean goBack;

    private DatabaseReference profile;
    private DatabaseReference database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        setupWindowAnimations();

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        goBack = false;

        database = FirebaseDatabase.getInstance().getReference();
        profile = database.child("students").child(user.getUid());

        student_toolbar = findViewById(R.id.toolbar);
        student_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        student_toolbar.setNavigationOnClickListener(e -> {
            goBack = false;
            onBackPressed();
        });

        draft_progressBar = findViewById(R.id.progressBar_draft);

        draft_textView = findViewById(R.id.textView_draft);

        first_imageView = findViewById(R.id.imageView_first);
        first_imageView.setImageResource(R.drawable.contact_circle);
        first_textView = findViewById(R.id.textView_first);

        second_imageView = findViewById(R.id.imageView_second);
        second_imageView.setImageResource(R.drawable.contact_circle);
        second_textView = findViewById(R.id.textView_second);

        third_imageView = findViewById(R.id.imageView_third);
        third_imageView.setImageResource(R.drawable.contact_circle);
        third_textView = findViewById(R.id.textView_third);

        oceanside_button = findViewById(R.id.button_oceanside);
        oceanside_button.setTransformationMethod(null);
        oceanside_button.setOnClickListener(e -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://oceansideschools.org/"));
            startActivity(i);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        profile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                first_textView.setText((String) dataSnapshot.child("firstChoice").getValue());
                second_textView.setText((String) dataSnapshot.child("secondChoice").getValue());
                third_textView.setText((String) dataSnapshot.child("thirdChoice").getValue());
                student_toolbar.setTitle(dataSnapshot.child("firstName").getValue() + " "
                        + dataSnapshot.child("lastName").getValue());
            }
        });

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long draft = -1;
                if(dataSnapshot.child("draft").exists()) {
                    draft = (long) dataSnapshot.child("draft").getValue();
                } else {
                    database.child("draft").setValue(-1);
                }
                if(draft < 1) draft = 0;

                float currentProgress = draft_progressBar.getProgress();
                ValueAnimator animator = ValueAnimator.ofFloat(currentProgress, (float)((draft*33) + 1));
                animator.addUpdateListener(e -> {
                    float updatedValue = (float) e.getAnimatedValue();
                    draft_progressBar.setSecondaryProgress(updatedValue);
                    draft_progressBar.setProgress(updatedValue - 5);
                });
                animator.setDuration(160);
                animator.start();

                if(draft == 0) {
                    draft_textView.setText(R.string.being);
                } else if(draft == 4){
                    draft_textView.setText(R.string.completed);
                } else {
                    draft_textView.setText("The draft is currently in round " + draft + " of 3.");
                }
            }
        });
    }

    private void setupWindowAnimations() {
        Transition fade_transition = TransitionInflater.from(this)
                .inflateTransition(R.transition.transition_fade);

        getWindow().setEnterTransition(fade_transition);
        getWindow().setExitTransition(fade_transition);
    }



    @Override
    public void onBackPressed() {
        if(!goBack) {
            new AlertDialog.Builder(StudentActivity.this)
                    .setTitle("Continue?")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.yes, (a, b) -> {
                        new Thread(() -> {
                            goBack = true;
                            runOnUiThread(() -> onBackPressed());
                        }).start();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            goBack = false;
            auth.signOut();
            super.onBackPressed();
        }
    }
}

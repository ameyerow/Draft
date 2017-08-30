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
import android.widget.RelativeLayout;
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
    private ImageView profile_imageView;
    private TextView profile_textView;
    private TextView draft_textView;
    private TextView first_textView;
    private TextView second_textView;
    private TextView third_textView;
    private TextView teacher_textView;
    private TextView email_textView;
    private RelativeLayout choices_layout;
    private RelativeLayout drafted_layout;

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

        choices_layout = findViewById(R.id.layout_choices);
        drafted_layout = findViewById(R.id.layout_drafted);

        student_toolbar = findViewById(R.id.toolbar);
        student_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        student_toolbar.setNavigationOnClickListener(e -> {
            goBack = false;
            onBackPressed();
        });

        draft_progressBar = findViewById(R.id.progressBar_draft);
        draft_textView = findViewById(R.id.textView_draft);

        profile_imageView = findViewById(R.id.imageView_profile);
        profile_textView = findViewById(R.id.textView_profile);

        first_textView = findViewById(R.id.textView_first);
        second_textView = findViewById(R.id.textView_second);
        third_textView = findViewById(R.id.textView_third);

        teacher_textView = findViewById(R.id.textView_teacher);
        email_textView = findViewById(R.id.textView_email);

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

        profile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                first_textView.setText((String) dataSnapshot.child("firstChoice").getValue());
                second_textView.setText((String) dataSnapshot.child("secondChoice").getValue());
                third_textView.setText((String) dataSnapshot.child("thirdChoice").getValue());
                student_toolbar.setTitle(dataSnapshot.child("firstName").getValue() + " "
                        + dataSnapshot.child("lastName").getValue());
                boolean partnered = (boolean) dataSnapshot.child("partnered").getValue();
                if(partnered) {
                    profile_imageView.setImageResource(R.drawable.contact_partners);
                    String name = dataSnapshot.child("firstName").getValue() + " "
                            + dataSnapshot.child("lastName").getValue();
                    String partner_name = dataSnapshot.child("partnerFirst").getValue() + " "
                            + dataSnapshot.child("partnerLast").getValue();
                    profile_textView.setText(name + " and " + partner_name);
                } else {
                    profile_imageView.setImageResource(R.drawable.contact_second);
                    String name = dataSnapshot.child("firstName").getValue() + " "
                            + dataSnapshot.child("lastName").getValue();
                    profile_textView.setText(name);
                }
            }
        });

        database.child("draft").addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long draft = -1;
                if(dataSnapshot.exists()) {
                    draft = (long) dataSnapshot.getValue();
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
                    draft_textView.setText(R.string.handling);
                } else if(draft == 5) {
                    draft_textView.setText(R.string.completed);
                } else {
                    draft_textView.setText("The draft is currently in round " + draft + " of 3.");
                }
            }
        });

        database.child("students").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean drafted = (boolean) dataSnapshot.child("drafted").getValue();

                if(drafted) {
                    String teacher = (String) dataSnapshot.child("teacher").getValue();
                    String email = (String) dataSnapshot.child("teacherEmail").getValue();
                    teacher_textView.setText(teacher);
                    email_textView.setText(email);

                    drafted_layout.setVisibility(View.VISIBLE);
                    choices_layout.setVisibility(View.GONE);
                } else {
                    choices_layout.setVisibility(View.VISIBLE);
                    drafted_layout.setVisibility(View.GONE);
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

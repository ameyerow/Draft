package meyerowitz.alex.draft;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Toolbar student_toolbar;

    private TextView draft_textView;
    private com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar draft_progressBar;

    private RelativeLayout profile_relativeLayout;
    private TextView studentName_textView;
    private TextView studentEmail_textView;
    private TextView studentProject_textView;
    private ImageView partnerSeparator;
    private LinearLayout partner_layout;
    private TextView partnerName_textView;

    private RelativeLayout mentorPicks_relativeLayout;
    private TextView first_textView;
    private TextView second_textView;
    private TextView third_textView;

    private RelativeLayout mentorDrafted_relativeLayout;
    private TextView mentorName_textView;
    private TextView mentorEmail_textView;

    private DatabaseReference profile;
    private DatabaseReference database;
    private FirebaseAuth auth;

    private boolean goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        setupWindowAnimations();

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance().getReference();
        profile = database.child("students").child(user.getUid());

        student_toolbar = findViewById(R.id.toolbar);
        student_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        student_toolbar.setNavigationOnClickListener(e -> {
            goBack = false;
            onBackPressed();
        });

        draft_textView = findViewById(R.id.textView_draft);
        draft_progressBar = findViewById(R.id.progressBar_draft);

        profile_relativeLayout = findViewById(R.id.relativeLayout_profile);
        studentName_textView = findViewById(R.id.textView_name);
        studentEmail_textView = findViewById(R.id.textView_studentEmail);
        studentProject_textView = findViewById(R.id.textView_studentProject);
        partnerSeparator = findViewById(R.id.separator3);
        partner_layout = findViewById(R.id.layout_partner);
        partnerName_textView = findViewById(R.id.textView_partnerName);

        mentorPicks_relativeLayout = findViewById(R.id.relativeLayout_mentorPicks);
        first_textView = findViewById(R.id.textView_first);
        second_textView = findViewById(R.id.textView_second);
        third_textView = findViewById(R.id.textView_third);

        mentorDrafted_relativeLayout = findViewById(R.id.relativeLayout_mentorDrafted);
        mentorName_textView = findViewById(R.id.textView_mentorName);
        mentorEmail_textView = findViewById(R.id.textView_mentorEmail);
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
                studentEmail_textView.setText((String) dataSnapshot.child("email").getValue());
                studentProject_textView.setText((String) dataSnapshot.child("projectIdea").getValue());
                String name = dataSnapshot.child("firstName").getValue() + " "
                        + dataSnapshot.child("lastName").getValue();
                student_toolbar.setTitle(name);
                studentName_textView.setText(name);

                boolean partnered = (boolean) dataSnapshot.child("partnered").getValue();
                if(partnered) {
                    String partner_name = dataSnapshot.child("partnerFirst").getValue() + " "
                            + dataSnapshot.child("partnerLast").getValue();
                    partnerName_textView.setText(partner_name);
                    partnerSeparator.setVisibility(View.VISIBLE);
                    partner_layout.setVisibility(View.VISIBLE);
                } else {
                    partnerSeparator.setVisibility(View.GONE);
                    partner_layout.setVisibility(View.GONE);
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
                    draft_progressBar.setProgress(updatedValue);
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
                    draft_textView.setText("Round " + draft + " of 3");
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
                    mentorName_textView.setText(teacher);
                    mentorEmail_textView.setText(email);

                    mentorDrafted_relativeLayout.setVisibility(View.VISIBLE);
                    mentorPicks_relativeLayout.setVisibility(View.GONE);
                } else {
                    mentorPicks_relativeLayout.setVisibility(View.VISIBLE);
                    mentorDrafted_relativeLayout.setVisibility(View.GONE);
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

package meyerowitz.alex.draft;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.util.ArrayList;


public class TeacherActivity extends Activity {
    private Toolbar teacher_toolbar;

    private com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar draft_progressBar;
    private TextView draft_textView;

    private TextView rosterInfo_textView;
    private Button roster_button;
    private ImageView separatorDraft;
    private RelativeLayout draft_relativeLayout;
    private TextView draftInfo_textView;
    private Button draft_button;
    private ImageView separatorLeftover;
    private RelativeLayout leftover_relativeLayout;
    private TextView leftoverInfo_textView;
    private Button leftover_button;

    private TextView name_textView;
    private TextView email_textView;
    private TextView classes_textView;

    private DatabaseReference profile;
    private DatabaseReference firstDraft;
    private DatabaseReference secondDraft;
    private DatabaseReference thirdDraft;
    private DatabaseReference drafted;
    private DatabaseReference database;
    private FirebaseAuth auth;

    private boolean goBack;
    private long draft;
    private boolean alreadyDrafted;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        goBack = false;
        finished = false;
        draft = -1;

        database = FirebaseDatabase.getInstance().getReference();
        profile = database.child("teachers").child(user.getUid());
        firstDraft = database.child("first-draft").child(user.getUid());
        secondDraft = database.child("second-draft").child(user.getUid());
        thirdDraft = database.child("third-draft").child(user.getUid());
        drafted = database.child("drafted");

        teacher_toolbar = findViewById(R.id.toolbar);
        teacher_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        teacher_toolbar.setNavigationOnClickListener(e -> {
            goBack = false;
            onBackPressed();
        });

        draft_progressBar = findViewById(R.id.progressBar_draft);
        draft_textView = findViewById(R.id.textView_draft);

        rosterInfo_textView = findViewById(R.id.textView_rosterInfo);
        roster_button = findViewById(R.id.button_roster);
        roster_button.setOnClickListener(e -> {
            drafted.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onCancelled(DatabaseError databaseError) {}
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> emails = new ArrayList<>();

                    if(dataSnapshot.child(user.getUid()).exists()) {
                        for(DataSnapshot snapshot : dataSnapshot.child(user.getUid()).getChildren()) {
                            boolean partnered = (boolean) snapshot.child("partnered").getValue();

                            if(!partnered) {
                                String name = (String) snapshot.child("name1").getValue();
                                String email = (String) snapshot.child("email1").getValue();

                                names.add("1 "+ name);
                                emails.add(email);
                            } else {
                                String name1 = (String) snapshot.child("name1").getValue();
                                String name2 = (String) snapshot.child("name2").getValue();
                                String email1 = (String) snapshot.child("email1").getValue();
                                String email2 = (String) snapshot.child("email2").getValue();

                                names.add("2 " + name1 + " & " + name2);
                                emails.add(email1 + " " + email2);
                            }
                        }
                    }

                    Intent i = new Intent(TeacherActivity.this, TeacherRosterActivity.class);
                    i.putExtra(TeacherRosterActivity.EXTRA_NAMES, names);
                    i.putExtra(TeacherRosterActivity.EXTRA_EMAILS, emails);
                    startActivity(i);
                }
            });
        });

        separatorDraft = findViewById(R.id.separatorDraft);
        draft_relativeLayout = findViewById(R.id.relativeLayout_draft);
        draftInfo_textView = findViewById(R.id.textView_draftInfo);
        draft_button = findViewById(R.id.button_draft);
        draft_button.setOnClickListener(e -> {
            if(draft != 0) {
                ArrayList<DatabaseReference> drafts = new ArrayList<>();
                drafts.add(firstDraft);
                drafts.add(secondDraft);
                drafts.add(thirdDraft);

                drafts.get(((int) draft) - 1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> names = new ArrayList<>();
                        ArrayList<String> projects = new ArrayList<>();
                        ArrayList<String> uids = new ArrayList<>();
                        ArrayList<String> emails = new ArrayList<>();

                        database.child("removed-from-draft").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                            @Override
                            public void onDataChange(DataSnapshot drafted) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String uid = snapshot.getKey();

                                    alreadyDrafted = false;
                                    for (DataSnapshot removed : drafted.getChildren()) {
                                        String removed_uid = removed.getKey();
                                        if (removed_uid.equals(uid)) {
                                            alreadyDrafted = true;
                                            break;
                                        }
                                    }

                                    if (alreadyDrafted) {
                                        continue;
                                    }

                                    boolean partnered = (boolean) snapshot.child("partnered").getValue();
                                    String project = (String) snapshot.child("project").getValue();
                                    String name1 = (String) snapshot.child("name1").getValue();
                                    String name2 = null;
                                    String email1 = (String) snapshot.child("email1").getValue();
                                    String email2 = null;
                                    if (partnered) {
                                        name2 = (String) snapshot.child("name2").getValue();
                                        names.add("2 " + name1 + " & " + name2);

                                        email2 = (String) snapshot.child("email2").getValue();
                                        emails.add(email1 + " " + email2);
                                    } else {
                                        names.add("1 " + name1);
                                        emails.add(email1);
                                    }

                                    uids.add(uid);
                                    projects.add(project);
                                }
                                runOnUiThread(() -> {
                                    Intent i = new Intent(TeacherActivity.this, TeacherDraftActivity.class);
                                    i.putExtra(TeacherDraftActivity.EXTRA_NAMES, names);
                                    i.putExtra(TeacherDraftActivity.EXTRA_PROJECTS, projects);
                                    i.putExtra(TeacherDraftActivity.EXTRA_UIDS, uids);
                                    i.putExtra(TeacherDraftActivity.EXTRA_EMAILS, emails);
                                    startActivityForResult(i, 0);
                                });
                            }
                        });
                    }
                });
            }
        });

        separatorLeftover = findViewById(R.id.separatorLeftover);
        leftover_relativeLayout = findViewById(R.id.relativeLayout_leftover);
        leftoverInfo_textView = findViewById(R.id.textView_leftoverInfo);
        leftover_button = findViewById(R.id.button_leftover);
        leftover_button.setOnClickListener(e -> {
            Intent i = new Intent(TeacherActivity.this, TeacherLeftoverActivity.class);
            startActivity(i);
        });

        name_textView = findViewById(R.id.textView_name);
        email_textView = findViewById(R.id.textView_teacherEmail);
        classes_textView = findViewById(R.id.textView_classes);
    }

    @Override
    public void onStart() {
        super.onStart();

        profile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (dataSnapshot.child("firstName").getValue()) + " "
                        + (dataSnapshot.child("lastName").getValue());
                teacher_toolbar.setTitle(name);
                name_textView.setText(name);
                email_textView.setText((String)dataSnapshot.child("email").getValue());
                classes_textView.setText(Long.toString((long)dataSnapshot.child("numberClasses").getValue()));
            }
        });

        profile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numberClasses = (long) dataSnapshot.child("numberClasses").getValue();
                long currentNumDrafted = (long) dataSnapshot.child("currentNumDrafted").getValue();
                long availableSlots = 10*numberClasses - currentNumDrafted;

                rosterInfo_textView.setText(currentNumDrafted + " students");
                draftInfo_textView.setText(Long.toString(availableSlots) + " slots available");
            }
        });

        database.child("draft").addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    draft = (long) dataSnapshot.getValue();
                } else {
                    database.child("draft").setValue(-1);
                    draft = -1;
                }
                if(draft < 1) draft = 0;

                float currentProgress = draft_progressBar.getProgress();
                ValueAnimator animator = ValueAnimator.ofFloat(currentProgress, (float)(((draft > 3 ? 3 : draft)*33) + 1));
                animator.addUpdateListener(e -> {
                    float updatedValue = (float) e.getAnimatedValue();
                    draft_progressBar.setProgress(updatedValue);
                });
                animator.setDuration(160);
                animator.start();

                if(draft == 0) {
                    draft_textView.setText(R.string.being);
                    separatorDraft.setVisibility(View.GONE);
                    draft_relativeLayout.setVisibility(View.GONE);
                } else if(draft == 4){
                    draft_textView.setText(R.string.handling);
                    separatorDraft.setVisibility(View.GONE);
                    draft_relativeLayout.setVisibility(View.GONE);
                } else if(draft == 5) {
                    draft_textView.setText(R.string.completed);
                    separatorDraft.setVisibility(View.GONE);
                    draft_relativeLayout.setVisibility(View.GONE);
                } else {
                    draft_textView.setText("Round " + draft + " of 3");
                    separatorDraft.setVisibility(View.VISIBLE);
                    draft_relativeLayout.setVisibility(View.VISIBLE);
                }

                if(finished) {
                    separatorDraft.setVisibility(View.GONE);
                    draft_relativeLayout.setVisibility(View.GONE);
                }
            }
        });

        database.child("teachers").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                finished = (boolean) dataSnapshot.child("finished").getValue();

                if(finished || draft < 1) {
                    separatorDraft.setVisibility(View.GONE);
                    draft_relativeLayout.setVisibility(View.GONE);
                } else {
                    separatorDraft.setVisibility(View.VISIBLE);
                    draft_relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        database.child("leftover-students").child("complete").addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    boolean complete = (boolean) dataSnapshot.getValue();

                    if(!complete) {
                        separatorLeftover.setVisibility(View.VISIBLE);
                        leftover_relativeLayout.setVisibility(View.VISIBLE);
                    } else {
                        separatorLeftover.setVisibility(View.GONE);
                        leftover_relativeLayout.setVisibility(View.GONE);
                    }
                } else {
                    separatorLeftover.setVisibility(View.GONE);
                    leftover_relativeLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0) {
            if(resultCode == Activity.RESULT_OK) {
                database.child("teachers").child(auth.getCurrentUser().getUid()).child("finished").setValue(true);

                // If every teacher is finished, move onto the next draft
                database.child("teachers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean moveOn = true;
                        for(DataSnapshot teacher : dataSnapshot.getChildren()) {
                            boolean finished = (boolean) teacher.child("finished").getValue();
                            if(!finished) {
                                moveOn = false;
                                break;
                            }
                        }
                        if(moveOn) {
                            for(DataSnapshot teacher : dataSnapshot.getChildren()) {
                               String uid = teacher.getKey();
                               if(draft + 1  < 4)
                                   database.child("teachers").child(uid).child("finished").setValue(false);
                            }

                            database.child("leftover-students").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(draft + 1 == 4 && dataSnapshot.exists()) {
                                        database.child("leftover-students").child("complete").setValue(false);
                                        database.child("draft").setValue(draft + 1);
                                    } else if(draft + 1 == 4) {
                                        database.child("draft").setValue(5);
                                    } else {
                                        database.child("draft").setValue(draft + 1);
                                    }
                                }
                            });
                        }
                    }
                });
            } else if(resultCode == Activity.RESULT_CANCELED) {
                // Do nothing
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!goBack) {
            new AlertDialog.Builder(TeacherActivity.this)
                    .setTitle("Continue?")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.yes, (a, b) -> {
                        goBack = true;
                        runOnUiThread(() -> onBackPressed());
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            goBack = false;
            auth.signOut();
            finishAfterTransition();
        }
    }
}

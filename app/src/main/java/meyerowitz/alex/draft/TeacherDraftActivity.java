package meyerowitz.alex.draft;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TeacherDraftActivity extends ListActivity {
    public static final String EXTRA_NAMES = "EXTRA_NAMES";
    public static final String EXTRA_PROJECTS = "EXTRA_PROJECTS";
    public static final String EXTRA_UIDS = "EXTRA_UIDS";
    public static final String EXTRA_EMAILS = "EXTRA_EMAILS";

    private RelativeLayout list_layout;
    private RelativeLayout information_layout;
    private ImageView project_imageView;
    private TextView project_textView;
    private Toolbar draft_toolbar;
    private ImageButton sendChoices_button;
    private Button accept_button;
    private Button deny_button;
    private TextView empty_textView;
    private ImageView empty_imageView;
    //private TextView currentNum_textView;
    private ImageButton help_button;

    private DatabaseReference database;
    private DatabaseReference teacher;
    private FirebaseAuth auth;

    private ArrayList<String> names;
    private ArrayList<String> projects;
    private ArrayList<String> uids;
    private ArrayList<String> emails;
    private ArrayList<Boolean> drafted;
    private ImageView lastImageView;
    private int shortAnimationDuration;
    private int lastPositionPressed;
    private String teacherName;
    private String teacherEmail;
    private long numCanDraft;
    private long maxNum;
    private long draft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_draft);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        lastPositionPressed = -1;

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        teacher = database.child("teachers").child(auth.getCurrentUser().getUid());

        list_layout = findViewById(R.id.layout_list);
        information_layout = findViewById(R.id.layout_information);
        project_imageView = findViewById(R.id.imageView_project);
        project_imageView.setImageResource(R.drawable.project);
        project_textView = findViewById(R.id.textView_project);

        draft_toolbar = findViewById(R.id.toolbar);
        draft_toolbar.setTitle("Draft");
        draft_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        draft_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        sendChoices_button = findViewById(R.id.button_sendChoices);
        sendChoices_button.setOnClickListener(e -> {
            for(Boolean status : drafted) {
                if(status == null) {
                    Toast.makeText(this, "You must accept or deny every student",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            teacher.child("currentNumDrafted").setValue(maxNum - numCanDraft);

            for(int i = 0; i < names.size(); i++) {
                Boolean status = drafted.get(i);

                if(status) {
                    String name = names.get(i);
                    String uid = uids.get(i);
                    String email = emails.get(i);
                    boolean partnered = name.substring(0, 1).equals("2");

                    if(partnered) {
                        String name1 = name.substring(2, name.indexOf("&") - 1);
                        String name2 = name.substring(name.indexOf("&") + 2, name.length());
                        String email1 = email.substring(0, email.indexOf(" "));
                        String email2 = email.substring(email.indexOf(" ") + 1, email.length());

                        Map<String, Object> draftedValues = new HashMap<>();
                        draftedValues.put("partnered", partnered);
                        draftedValues.put("name1", name1);
                        draftedValues.put("name2", name2);
                        draftedValues.put("email1", email1);
                        draftedValues.put("email2", email2);

                        Map<String, Object> removedValues = new HashMap<>();
                        removedValues.put("placeholder", 0);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/drafted/" + auth.getCurrentUser().getUid()+"/"+uid, draftedValues);
                        childUpdates.put("/removed-from-draft/"+uid, removedValues);
                        database.updateChildren(childUpdates);

                        database.child("students").child(uid).child("drafted").setValue(true);
                        database.child("students").child(uid).child("teacher").setValue(teacherName);
                        database.child("students").child(uid).child("teacherEmail").setValue(teacherEmail);

                        database.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String first = (String) snapshot.child("firstName").getValue();
                                    String last = (String) snapshot.child("lastName").getValue();

                                    String partnerFirst = name2.substring(0, name2.indexOf(" "));
                                    String partnerLast = name2.substring(name2.indexOf(" ") + 1, name2.length());

                                    if(first.equals(partnerFirst) && last.equals(partnerLast)) {
                                        String partner_uid = snapshot.getKey();
                                        database.child("students").child(partner_uid).child("drafted").setValue(true);
                                        database.child("students").child(partner_uid).child("teacher").setValue(teacherName);
                                        database.child("students").child(partner_uid).child("teacherEmail").setValue(teacherEmail);
                                        break;
                                    }
                                }
                            }
                        });
                    } else {
                        name = name.substring(2, name.length());
                        Map<String, Object> draftedValues = new HashMap<>();
                        draftedValues.put("partnered", partnered);
                        draftedValues.put("name1", name);
                        draftedValues.put("email1", email);

                        Map<String, Object> removedValues = new HashMap<>();
                        removedValues.put("placeholder", 0);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/drafted/" + auth.getCurrentUser().getUid()+"/"+uid, draftedValues);
                        childUpdates.put("/removed-from-draft/"+uid, removedValues);
                        database.updateChildren(childUpdates);

                        database.child("students").child(uid).child("drafted").setValue(true);
                        database.child("students").child(uid).child("teacher").setValue(teacherName);
                        database.child("students").child(uid).child("teacherEmail").setValue(teacherEmail);
                    }
                }
            }

            // If this is the last draft, move the left over students to a separate category
            if(draft + 1 == 4) {
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(int i = 0; i < names.size(); i++) {
                            Boolean status = drafted.get(i);
                            if(!status) {
                                String name = names.get(i);
                                String uid = uids.get(i);
                                String email = emails.get(i);
                                String project = projects.get(i);
                                boolean partnered = name.substring(0, 1).equals("2");

                                if(partnered) {
                                    String name1 = name.substring(2, name.indexOf("&") - 1);
                                    String name2 = name.substring(name.indexOf("&") + 2, name.length());
                                    String email1 = email.substring(0, email.indexOf(" "));
                                    String email2 = email.substring(email.indexOf(" ") + 1, email.length());

                                    Map<String, Object> leftoverValues = new HashMap<>();
                                    leftoverValues.put("partnered", partnered);
                                    leftoverValues.put("name1", name1);
                                    leftoverValues.put("name2", name2);
                                    leftoverValues.put("email1", email1);
                                    leftoverValues.put("email2", email2);
                                    leftoverValues.put("project", project);

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/leftover-students/" + uid, leftoverValues);
                                    database.updateChildren(childUpdates);
                                } else {
                                    name = name.substring(2, name.length());
                                    Map<String, Object> leftoverValues = new HashMap<>();
                                    leftoverValues.put("partnered", partnered);
                                    leftoverValues.put("name1", name);
                                    leftoverValues.put("email1", email);
                                    leftoverValues.put("project", project);

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/leftover-students/" + uid, leftoverValues);
                                    database.updateChildren(childUpdates);
                                }
                            }
                        }
                    }
                });
            }

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finishAfterTransition();
        });

        accept_button = findViewById(R.id.button_accept);
        accept_button.setOnClickListener(e -> {
            String name = names.get(lastPositionPressed);
            if(name.substring(0, 1).equals("1")) {
                if(numCanDraft - 1 >= 0) {
                    if((drafted.get(lastPositionPressed) != null && !drafted.get(lastPositionPressed))
                            || drafted.get(lastPositionPressed) == null)
                        numCanDraft = numCanDraft - 1;
                    drafted.set(lastPositionPressed, true);
                    lastImageView.setImageResource(R.drawable.ic_accept);
                    //currentNum_textView.setText("You can draft " + numCanDraft + " more students.");
                } else {
                    Toast.makeText(this, "Not enough space left.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if(numCanDraft - 2 >= 0) {
                    if(drafted.get(lastPositionPressed) != null && !drafted.get(lastPositionPressed)
                            || drafted.get(lastPositionPressed) == null)
                        numCanDraft = numCanDraft - 2;
                    drafted.set(lastPositionPressed, true);
                    lastImageView.setImageResource(R.drawable.ic_accept);
                    //currentNum_textView.setText("You can draft " + numCanDraft + " more students.");
                } else {
                    Toast.makeText(this, "Not enough space left.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deny_button = findViewById(R.id.button_deny);
        deny_button.setOnClickListener(e -> {
            String name = names.get(lastPositionPressed);
            if (name.substring(0, 1).equals("1")) {
                if(drafted.get(lastPositionPressed) != null && drafted.get(lastPositionPressed)) {
                    numCanDraft = numCanDraft + 1;
                    //currentNum_textView.setText("You can draft " + numCanDraft + " more students.");
                }
                lastImageView.setImageResource(R.drawable.ic_deny);
            } else {
                if(drafted.get(lastPositionPressed) != null && drafted.get(lastPositionPressed)) {
                    numCanDraft = numCanDraft + 2;
                    //currentNum_textView.setText("You can draft " + numCanDraft + " more students.");
                }
                lastImageView.setImageResource(R.drawable.ic_deny);
            }
            drafted.set(lastPositionPressed, false);
        });

        empty_textView = findViewById(R.id.textView_empty);
        empty_imageView = findViewById(R.id.imageView_empty);
        //currentNum_textView = findViewById(R.id.textView_currentNum);

        help_button = findViewById(R.id.button_help);
        help_button.setOnClickListener(e -> {
            new AlertDialog.Builder(TeacherDraftActivity.this)
                    .setTitle("Help")
                    .setMessage("1. Press a student on your list and read their project\n" +
                                "2. Choose to either \"accept\" or \"deny\" them\n" +
                                "3. Repeat this for every student on your list\n" +
                                "4. Once you're done, hit send in the top right")
                    .setPositiveButton(android.R.string.yes, null).show();
        });

        names = getIntent().getStringArrayListExtra(EXTRA_NAMES);
        projects = getIntent().getStringArrayListExtra(EXTRA_PROJECTS);
        uids = getIntent().getStringArrayListExtra(EXTRA_UIDS);
        emails = getIntent().getStringArrayListExtra(EXTRA_EMAILS);
        drafted = new ArrayList<>();
        for(String name : names) {
            drafted.add(null);
        }

        if(names.size() == 0) {
            empty_textView.setVisibility(View.VISIBLE);
            empty_imageView.setVisibility(View.VISIBLE);
        }

        NameAdapter adapter = new NameAdapter(names);
        setListAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        teacher.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long currentNumDrafted = (long) dataSnapshot.child("currentNumDrafted").getValue();
                maxNum = (long) dataSnapshot.child("numberClasses").getValue() * 10;
                numCanDraft = maxNum - currentNumDrafted;
                //currentNum_textView.setText("You can draft " + numCanDraft + " more students.");
                teacherName = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                teacherEmail = (String) dataSnapshot.child("email").getValue();
            }
        });

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                draft = (long) dataSnapshot.child("draft").getValue();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((NameAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        int initialHeight =  (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 56,
                getResources().getDisplayMetrics()
        );
        int translatedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 306,
                getResources().getDisplayMetrics()
        );

        if(lastPositionPressed == -1) {
            project_textView.setText(projects.get(position));
            list_layout.animate()
                    .y(translatedHeight)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ValueAnimator anim = ValueAnimator.ofInt(list_layout.getMeasuredHeight(),
                                    list_layout.getMeasuredHeight() - (translatedHeight - initialHeight));
                            anim.addUpdateListener(valueAnimator ->  {
                                int val = (Integer) valueAnimator.getAnimatedValue();
                                ViewGroup.LayoutParams layoutParams = list_layout.getLayoutParams();
                                layoutParams.height = val;
                                list_layout.setLayoutParams(layoutParams);
                            });
                            anim.setDuration(1);
                            anim.start();
                        }
                    });
            lastPositionPressed = position;
            lastImageView = (ImageView)((RelativeLayout) l.getChildAt(position)).getChildAt(0);
        } else if(lastPositionPressed == position) {
            ValueAnimator anim = ValueAnimator.ofInt(list_layout.getMeasuredHeight(),
                    list_layout.getMeasuredHeight() + (translatedHeight-initialHeight));
            anim.setDuration(1);
            anim.addUpdateListener(valueAnimator ->  {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = list_layout.getLayoutParams();
                layoutParams.height = val;
                list_layout.setLayoutParams(layoutParams);
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    list_layout.animate()
                            .y(initialHeight)
                            .setDuration(shortAnimationDuration)
                            .setListener(null);
                }
            });
            anim.start();
            lastPositionPressed = -1;
        } else {
            information_layout.animate()
                    .alpha(0f)
                    .setDuration(160)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            project_textView.setText(projects.get(position));
                            information_layout.animate()
                                    .alpha(1f)
                                    .setDuration(160)
                                    .setListener(null);
                        }
                    });

            lastPositionPressed = position;
            lastImageView = (ImageView)((RelativeLayout) l.getChildAt(position)).getChildAt(0);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finishAfterTransition();
    }

    private class NameAdapter extends ArrayAdapter<String> {
        public NameAdapter(ArrayList<String> names) {
            super(TeacherDraftActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = TeacherDraftActivity.this.getLayoutInflater()
                        .inflate(R.layout.list_item, null);
            }
            String name = getItem(position);
            boolean partnered = name.substring(0, 1).equals("2");
            name = name.substring(2, name.length());

            ImageView listItem_imageView = convertView.findViewById(R.id.imageView_listItem);

            if(drafted.get(position) == null) {
                if (partnered) {
                    listItem_imageView.setImageResource(R.drawable.ic_group);
                } else {
                    listItem_imageView.setImageResource(R.drawable.ic_account);
                }
            } else {
                if(partnered && drafted.get(position)) {
                    listItem_imageView.setImageResource(R.drawable.ic_accept);
                } else if(partnered && !drafted.get(position)) {
                    listItem_imageView.setImageResource(R.drawable.ic_deny);
                } else if(!partnered && drafted.get(position)) {
                    listItem_imageView.setImageResource(R.drawable.ic_accept);
                } else if(!partnered && !drafted.get(position)) {
                    listItem_imageView.setImageResource(R.drawable.ic_deny);
                }
            }

            TextView name_textView = convertView.findViewById(R.id.textView_name);
            name_textView.setText(name);

            return convertView;
        }
    }
}

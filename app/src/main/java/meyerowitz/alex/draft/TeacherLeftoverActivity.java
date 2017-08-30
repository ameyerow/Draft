package meyerowitz.alex.draft;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

public class TeacherLeftoverActivity extends ListActivity {
    private RelativeLayout list_layout;
    private RelativeLayout information_layout;
    private ImageView project_imageView;
    private TextView project_textView;
    private Toolbar leftover_toolbar;
    private Button accept_button;

    private DatabaseReference database;
    private FirebaseAuth auth;

    private ArrayList<String> names;
    private ArrayList<String> projects;
    private ArrayList<String> uids;
    private ArrayList<String> emails;
    private int shortAnimationDuration;
    private int lastPositionPressed;
    private String teacherName;
    private String teacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_leftover);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        lastPositionPressed = -1;

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        list_layout = findViewById(R.id.layout_list);
        information_layout = findViewById(R.id.layout_information);
        project_imageView = findViewById(R.id.imageView_project);
        project_imageView.setImageResource(R.drawable.project);
        project_textView = findViewById(R.id.textView_project);

        leftover_toolbar = findViewById(R.id.toolbar);
        leftover_toolbar.setTitle("Leftover students");
        leftover_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        leftover_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        accept_button = findViewById(R.id.button_accept);
        accept_button.setOnClickListener(e -> {
            new AlertDialog.Builder(TeacherLeftoverActivity.this)
                    .setTitle("Continue?")
                    .setMessage("Are you sure you want to accept this student?")
                    .setPositiveButton(android.R.string.yes, (a, b) -> {
                        String name = names.get(lastPositionPressed);
                        String uid = uids.get(lastPositionPressed);
                        String email = emails.get(lastPositionPressed);
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

                        if(names.size() == 1) {
                            database.child("draft").setValue(5);
                        }

                        database.child("leftover-students").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dataSnapshot.getRef().setValue(null);
                            }
                        });
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        });

        names = new ArrayList<>();
        NameAdapter adapter = new NameAdapter(names);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((NameAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        database.child("leftover-students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<>();
                projects = new ArrayList<>();
                uids = new ArrayList<>();
                emails = new ArrayList<>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();

                    if(!uid.equals("complete")) {
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
                }

                if(names.size() == 0) {
                    database.child("leftover-students").child("complete").setValue(true);
                    runOnUiThread(() -> finishAfterTransition());
                }

                TeacherLeftoverActivity.this.setListAdapter(new NameAdapter(names));
                ((NameAdapter) getListAdapter()).notifyDataSetChanged();

                if(lastPositionPressed != -1) {
                    onListItemClick(null, null, lastPositionPressed,  0);
                }
            }
        });

        database.child("teachers").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teacherName = dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue();
                teacherEmail = (String) dataSnapshot.child("email").getValue();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        int initialHeight =  (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 56,
                getResources().getDisplayMetrics()
        );
        int translatedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 290,
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
        }
    }

    private class NameAdapter extends ArrayAdapter<String> {
        public NameAdapter(ArrayList<String> names) {
            super(TeacherLeftoverActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = TeacherLeftoverActivity.this.getLayoutInflater()
                        .inflate(R.layout.list_item, null);
            }
            String name = getItem(position);
            boolean partnered = name.substring(0, 1).equals("2");
            name = name.substring(2, name.length());

            ImageView listItem_imageView = convertView.findViewById(R.id.imageView_listItem);

            if (partnered) {
                listItem_imageView.setImageResource(R.drawable.contact_partners);
            } else {
                listItem_imageView.setImageResource(R.drawable.contact_second);
            }

            TextView name_textView = convertView.findViewById(R.id.textView_name);
            name_textView.setText(name);

            return convertView;
        }
    }
}

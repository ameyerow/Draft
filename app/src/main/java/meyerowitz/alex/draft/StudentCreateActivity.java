package meyerowitz.alex.draft;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentCreateActivity extends Activity {
    private ProgressDialog progressDialog;
    private Toolbar create_toolbar;
    private EditText last_name_editText;
    private EditText first_name_editText;
    private ImageView contact_imageView;
    private TextView add_partner_textView;
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private Spinner first_spinner;
    private Spinner second_spinner;
    private Spinner third_spinner;
    private EditText project_editText;
    private Button create_account_button;
    private ScrollView scrollView;

    private FirebaseAuth auth;
    private DatabaseReference database;
    private DatabaseReference teachers;
    private DatabaseReference students;

    private Map<String, String> teacherUidMap;
    private String partner_last;
    private String partner_first;
    private boolean partnered;
    private int shortAnimationDuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_create);

        setupWindowAnimations();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        teachers = database.child("teachers");
        students = database.child("students");

        teacherUidMap = new HashMap<>();
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        partner_last = "null";
        partner_first = "null";

        scrollView = findViewById(R.id.scrollView_studentCreate);

        create_toolbar = findViewById(R.id.toolbar);
        create_toolbar.setTitle("Create a student account");
        create_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        create_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        last_name_editText = findViewById(R.id.editText_last_name);
        first_name_editText = findViewById(R.id.editText_first_name);

        email_editText = findViewById(R.id.editText_email);
        pass_editText = findViewById(R.id.editText_pass);
        pass_confirm_editText = findViewById(R.id.editText_pass_confirm);

        contact_imageView = findViewById(R.id.imageView_contact);
        contact_imageView.setImageResource(R.drawable.contact);
        contact_imageView.setOnClickListener(e -> {
            if(!partnered) {
                Intent i = new Intent(StudentCreateActivity.this, StudentPartnerActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(StudentCreateActivity.this,
                        Pair.create(contact_imageView, contact_imageView.getTransitionName()));
                //change to start activity for result
                startActivityForResult(i, 0, options.toBundle());
            } else {
                new AlertDialog.Builder(StudentCreateActivity.this)
                        .setTitle("Continue?")
                        .setMessage("Do you want to remove " + partner_first + " " + partner_last +
                                " as your partner?")
                        .setPositiveButton(android.R.string.yes, (a, b) -> {
                            partner_last = "null";
                            partner_first = "null";
                            add_partner_textView.setText(R.string.add_partner);
                            contact_imageView.setImageResource(R.drawable.contact);
                            partnered = false;
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        add_partner_textView = findViewById(R.id.textView_add_partner);

        first_spinner = findViewById(R.id.spinner_first);
        second_spinner = findViewById(R.id.spinner_second);
        third_spinner = findViewById(R.id.spinner_third);

        project_editText = findViewById(R.id.editText_project);

        create_account_button = findViewById(R.id.button_create_account);
        create_account_button.setOnClickListener(e -> {
            if(!validateForm()) {
                scrollView.fullScroll(View.FOCUS_UP);
                return;
            }

            showProgressDialog();

            String email = email_editText.getText().toString().trim();
            String password = pass_editText.getText().toString().trim();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if(task.isSuccessful()) {
                            String lastName = last_name_editText.getText().toString().trim();
                            String firstName = first_name_editText.getText().toString().trim();
                            String firstChoice = first_spinner.getSelectedItem().toString().trim();
                            String secondChoice = second_spinner.getSelectedItem().toString().trim();
                            String thirdChoice = third_spinner.getSelectedItem().toString().trim();
                            String projectIdea = project_editText.getText().toString().trim();
                            String fcmToken = FirebaseInstanceId.getInstance().getToken();
                            partnered = !partner_last.equals("null") && !partner_first.equals("null");

                            FirebaseUser user = task.getResult().getUser();
                            Student student = new Student(lastName, firstName, partner_last, partner_first,
                                    firstChoice, secondChoice, thirdChoice, projectIdea, email, fcmToken);
                            Map<String, Object> studentValues = student.toMap();

                            // If the user chose a partner, check if that partner has picked them also
                            if(partnered) {
                                students.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        partnered = false;
                                        String partner_email = null;

                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String snap_lastName = (String) snapshot.child("lastName").getValue();
                                            String snap_firstName = (String) snapshot.child("firstName").getValue();
                                            String snap_partnerLast = (String) snapshot.child("partnerLast").getValue();
                                            String snap_partnerFirst = (String) snapshot.child("partnerFirst").getValue();
                                            partner_email = (String) snapshot.child("email").getValue();

                                            if(lastName.equals(snap_partnerLast) && firstName.equals(snap_partnerFirst) &&
                                                    partner_last.equals(snap_lastName) && partner_first.equals(snap_firstName)) {
                                                partnered = true;

                                                String snap_Uid = snapshot.getKey();
                                                String snap_firstChoice = (String) snapshot.child("firstChoice").getValue();
                                                String snap_secondChoice = (String) snapshot.child("secondChoice").getValue();
                                                String snap_thirdChoice = (String) snapshot.child("thirdChoice").getValue();

                                                Query firstQuery = database.child("first-draft").child(teacherUidMap.get(snap_firstChoice))
                                                        .orderByKey().equalTo(snap_Uid);
                                                firstQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot firstSnapshot: dataSnapshot.getChildren()) {
                                                            firstSnapshot.getRef().removeValue();
                                                        }
                                                    }
                                                });
                                                Query secondQuery = database.child("second-draft").child(teacherUidMap.get(snap_secondChoice))
                                                        .orderByKey().equalTo(snap_Uid);
                                                secondQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot secondSnapshot: dataSnapshot.getChildren()) {
                                                            secondSnapshot.getRef().removeValue();
                                                        }
                                                    }
                                                });

                                                Query thirdQuery = database.child("third-draft").child(teacherUidMap.get(snap_thirdChoice))
                                                        .orderByKey().equalTo(snap_Uid);
                                                thirdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot thirdSnapshot: dataSnapshot.getChildren()) {
                                                            thirdSnapshot.getRef().removeValue();
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        if(partnered) {
                                            Map<String, Object> choiceValues = new HashMap<>();
                                            choiceValues.put("partnered", partnered);
                                            choiceValues.put("name1", firstName + " " + lastName);
                                            choiceValues.put("name2", partner_first + " " + partner_last);
                                            choiceValues.put("email1", email);
                                            choiceValues.put("email2", partner_email);
                                            choiceValues.put("project", projectIdea);

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("/students/" + user.getUid(), studentValues);
                                            childUpdates.put("/first-draft/" + teacherUidMap.get(firstChoice) + "/" + user.getUid(), choiceValues);
                                            childUpdates.put("/second-draft/" + teacherUidMap.get(secondChoice) + "/" + user.getUid(), choiceValues);
                                            childUpdates.put("/third-draft/" + teacherUidMap.get(thirdChoice) + "/" + user.getUid(), choiceValues);

                                            database.updateChildren(childUpdates);
                                        } else {
                                            Map<String, Object> choiceValues = new HashMap<>();
                                            choiceValues.put("partnered", partnered);
                                            choiceValues.put("name1", firstName + " " + lastName);
                                            choiceValues.put("email1", email);
                                            choiceValues.put("project", projectIdea);

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("/students/" + user.getUid(), studentValues);
                                            childUpdates.put("/first-draft/" + teacherUidMap.get(firstChoice) + "/" + user.getUid(), choiceValues);
                                            childUpdates.put("/second-draft/" + teacherUidMap.get(secondChoice) + "/" + user.getUid(), choiceValues);
                                            childUpdates.put("/third-draft/" + teacherUidMap.get(thirdChoice) + "/" + user.getUid(), choiceValues);

                                            database.updateChildren(childUpdates);
                                        }
                                    }
                                });
                            } else {
                                Map<String, Object> choiceValues = new HashMap<>();
                                choiceValues.put("partnered", partnered);
                                choiceValues.put("name1", firstName + " " + lastName);
                                choiceValues.put("email1", email);
                                choiceValues.put("project", projectIdea);

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/students/" + user.getUid(), studentValues);
                                childUpdates.put("/first-draft/" + teacherUidMap.get(firstChoice) + "/" + user.getUid(), choiceValues);
                                childUpdates.put("/second-draft/" + teacherUidMap.get(secondChoice) + "/" + user.getUid(), choiceValues);
                                childUpdates.put("/third-draft/" + teacherUidMap.get(thirdChoice) + "/" + user.getUid(), choiceValues);

                                database.updateChildren(childUpdates);
                            }
                            finishAfterTransition();
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            email_editText.setError("Email already in use.");
                        } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            pass_editText.setError("Weak password.");
                        }
                    });
            hideProgressDialog();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        teachers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> list = new ArrayList<>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String lastName = (String) snapshot.child("lastName").getValue();
                    String firstName = (String) snapshot.child("firstName").getValue();
                    list.add(firstName + " " + lastName);

                    String UID = snapshot.getKey();
                    teacherUidMap.put(firstName + " " + lastName, UID);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentCreateActivity.this,
                        android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                first_spinner.setAdapter(adapter);
                second_spinner.setAdapter(adapter);
                third_spinner.setAdapter(adapter);
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String lastName = last_name_editText.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            last_name_editText.setError("Required.");
            valid = false;
        } else {
            last_name_editText.setError(null);
        }

        String firstName = first_name_editText.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            first_name_editText.setError("Required.");
            valid = false;
        } else {
            first_name_editText.setError(null);
        }

        String email = email_editText.getText().toString();
        if(TextUtils.isEmpty(email)) {
            email_editText.setError("Required.");
            valid = false;
        } else if(!email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            email_editText.setError("Invalid email address.");
            valid = false;
        } else {
            email_editText.setError(null);
        }

        String password = pass_editText.getText().toString();
        if(TextUtils.isEmpty(password)) {
            pass_editText.setError("Required.");
            valid = false;
        } else {
            pass_editText.setError(null);
        }

        String confirmPassword = pass_confirm_editText.getText().toString();
        if(TextUtils.isEmpty(confirmPassword)) {
            pass_confirm_editText.setError("Required.");
            valid = false;
        } else {
            if(confirmPassword.equals(password)) {
                pass_confirm_editText.setError(null);
            } else {
                pass_confirm_editText.setError("Passwords don't match.");
                valid = false;
            }
        }

        String project = project_editText.getText().toString();
        if(TextUtils.isEmpty(project)) {
            project_editText.setError("Required.");
            valid = false;
        } else {
            project_editText.setError(null);
        }

        String firstChoice = first_spinner.getSelectedItem().toString();
        String secondChoice = second_spinner.getSelectedItem().toString();
        String thirdChoice = third_spinner.getSelectedItem().toString();
        if(firstChoice.equals(secondChoice) || firstChoice.equals(thirdChoice) || secondChoice.equals(thirdChoice)) {
            Toast.makeText(this, "You must pick three different teachers.",
                    Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setupWindowAnimations() {
        Transition fade_transition = TransitionInflater.from(this)
                .inflateTransition(R.transition.transition_fade);

        getWindow().setEnterTransition(fade_transition);
        getWindow().setExitTransition(fade_transition);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0) {
            if(resultCode == Activity.RESULT_OK) {
                partner_last = data.getStringExtra("last");
                partner_first = data.getStringExtra("first");
                contact_imageView.animate()
                        .alpha(0f)
                        .setDuration(shortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                contact_imageView.setImageResource(R.drawable.contact_second);
                                contact_imageView.animate()
                                        .alpha(1f)
                                        .setDuration(shortAnimationDuration)
                                        .setListener(null);
                            }
                        });
                add_partner_textView.animate()
                        .alpha(0f)
                        .setDuration(shortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                add_partner_textView.setText(partner_first + " " + partner_last);
                                add_partner_textView.animate()
                                        .alpha(1f)
                                        .setDuration(shortAnimationDuration)
                                        .setListener(null);
                            }
                        });
                partnered = true;
            } else if(resultCode == Activity.RESULT_CANCELED) {
                // Do nothing
            }
        }
    }
}
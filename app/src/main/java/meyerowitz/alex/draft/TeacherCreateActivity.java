package meyerowitz.alex.draft;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class TeacherCreateActivity extends Activity {
    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";
    public static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";

    private Toolbar create_toolbar;
    private Button create_account_button;
    private EditText last_name_editText;
    private EditText first_name_editText;
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private EditText numberClasses_editText;
    private ScrollView scrollView;

    private FirebaseAuth auth;
    private DatabaseReference authorizedEmails;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_create);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        authorizedEmails = database.child("authorized-emails");

        create_toolbar = findViewById(R.id.toolbar);
        create_toolbar.setTitle("Create a teacher account");
        create_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        create_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        scrollView = findViewById(R.id.scrollView_teacherCreate);

        last_name_editText = findViewById(R.id.editText_last_name);
        first_name_editText = findViewById(R.id.editText_first_name);

        email_editText = findViewById(R.id.editText_email);
        String login_email = getIntent().getStringExtra(EXTRA_EMAIL);
        if(!login_email.equals("")) email_editText.setText(login_email);

        pass_editText = findViewById(R.id.editText_pass);
        String login_pass = getIntent().getStringExtra(EXTRA_PASSWORD);
        if(!login_pass.equals("")) pass_editText.setText(login_pass);

        pass_confirm_editText = findViewById(R.id.editText_pass_confirm);

        numberClasses_editText = findViewById(R.id.editText_numberClasses);

        create_account_button = findViewById(R.id.button_create_account);
        create_account_button.setOnClickListener(e -> {
            if(!validateForm()) {
                scrollView.fullScroll(View.FOCUS_UP);
                return;
            }

            String email = email_editText.getText().toString().trim();
            String password = pass_editText.getText().toString().trim();

            auth.signInAnonymously();
            authorizedEmails.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onCancelled(DatabaseError databaseError) {}
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean authorized = false;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String authorizedEmail = snapshot.getKey()
                                .replaceFirst(" ", "@")
                                .replaceFirst(" ", ".");
                        if(email.equals(authorizedEmail)) {
                            authorized = true;
                        }
                    }
                    if(authorized) {
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(TeacherCreateActivity.this, task -> {
                                    if(task.isSuccessful()) {
                                        String lastName = last_name_editText.getText().toString().trim();
                                        String firstName = first_name_editText.getText().toString().trim();
                                        String pushToken = FirebaseInstanceId.getInstance().getToken();
                                        int numberClasses = Integer.parseInt(numberClasses_editText.getText().toString().trim());

                                        FirebaseUser user = task.getResult().getUser();
                                        Teacher teacher = new Teacher(lastName, firstName, numberClasses, email);
                                        Map<String, Object> teacherValues = teacher.toMap();

                                        database.child("pushTokens").child(user.getUid()).child("pushToken").setValue(pushToken);

                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put("/teachers/" + user.getUid(), teacherValues);

                                        database.updateChildren(childUpdates);

                                        user.sendEmailVerification();
                                        Toast.makeText(TeacherCreateActivity.this,
                                                "Verification email sent to " + user.getEmail(),
                                                Toast.LENGTH_SHORT).show();

                                        finishAfterTransition();
                                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                       email_editText.setError("Email already in use.");
                                    } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                        pass_editText.setError("Weak password.");
                                    }
                                });
                    } else {
                        email_editText.setError("Unauthorized email.");
                    }
                }
            });
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
            email_editText.setError("Invalid email.");
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

        String numberClasses = numberClasses_editText.getText().toString();
        if(TextUtils.isEmpty(numberClasses)) {
            numberClasses_editText.setError("Required.");
            valid = false;
        } else {
            if (isInteger(numberClasses)) {
                numberClasses_editText.setError(null);
            } else {
                numberClasses_editText.setError("Must be an integer.");
                valid = false;
            }
        }

        return valid;
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}

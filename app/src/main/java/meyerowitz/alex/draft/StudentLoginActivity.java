package meyerowitz.alex.draft;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends Activity {
    private ImageView login_imageView;
    private Toolbar login_toolbar;
    private EditText username_editText;
    private EditText password_editText;
    private ToggleButton show_button;
    private Button login_button;
    private Button create_button;
    private Button oceanside_button;

    private FirebaseAuth auth;
    private DatabaseReference students;
    private DatabaseReference database;

    private long draft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupWindowAnimations();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        students = database.child("students");

        login_imageView = findViewById(R.id.imageView_login);
        login_imageView.setImageResource(R.drawable.blurred_background);
        login_imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        login_toolbar = findViewById(R.id.toolbar);
        login_toolbar.setTitle("Student Login");
        login_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        login_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        username_editText = findViewById(R.id.editText_username);
        password_editText = findViewById(R.id.editText_password);

        show_button = findViewById(R.id.button_show);
        show_button.setOnCheckedChangeListener((e, b) -> {
            if(password_editText.getInputType() ==
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                password_editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                password_editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });

        login_button = findViewById(R.id.button_login);
        login_button.setOnClickListener(e -> {
            login_button.setEnabled(false);
            String email = username_editText.getText().toString().trim();
            String password = password_editText.getText().toString().trim();

            if(!email.equals("") && !password.equals("")) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();

                                // Checks that the user created a student account
                                students.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.child(user.getUid()).exists()) {
                                            // If the user was a teacher, sign them out
                                            auth.signOut();
                                            Toast.makeText(StudentLoginActivity.this, "Incorrect account type.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            password_editText.getText().clear();

                                            Intent i = new Intent(StudentLoginActivity.this, StudentActivity.class);
                                            startActivity(i);
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Login failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                if(email.equals("")) {
                    username_editText.setError("Required.");
                } else {
                    username_editText.setError(null);
                }

                if(password.equals("")) {
                    password_editText.setError("Required.");
                } else {
                    password_editText.setError(null);
                }
            }
            login_button.setEnabled(true);
        });

        create_button = findViewById(R.id.button_create);
        create_button.setOnClickListener(e -> {
            if(draft == 0) {
                Intent i = new Intent(StudentLoginActivity.this, StudentCreateActivity.class);

                String username = TextUtils.isEmpty(username_editText.getText()) ? "" : username_editText.getText().toString();
                String password = TextUtils.isEmpty(password_editText.getText()) ? "" : password_editText.getText().toString();

                i.putExtra(StudentCreateActivity.EXTRA_EMAIL, username);
                i.putExtra(StudentCreateActivity.EXTRA_PASSWORD, password);

                password_editText.getText().clear();

                startActivity(i);
            }
        });

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

                switch((int) draft) {
                    case -1:
                        login_button.setLayoutParams(
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT, .70f));
                        create_button.setLayoutParams(
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT, .30f));
                        create_button.setVisibility(View.VISIBLE);
                        create_button.setEnabled(false);
                        break;
                    case 0:
                        login_button.setLayoutParams(
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, .70f));
                        create_button.setLayoutParams(
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, .30f));
                        create_button.setVisibility(View.VISIBLE);
                        create_button.setEnabled(true);
                        break;
                    default:
                        create_button.setVisibility(View.GONE);
                        break;
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
}

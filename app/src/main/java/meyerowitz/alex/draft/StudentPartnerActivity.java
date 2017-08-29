package meyerowitz.alex.draft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

public class StudentPartnerActivity extends Activity {
    private Toolbar partner_toolbar;
    private ImageView contact_imageView;
    private Button oceanside_button;
    private RelativeLayout layout;
    private Button done_button;
    private EditText last_name_editText;
    private EditText first_name_editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_partner);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        layout = findViewById(R.id.layout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = size.y - ((size.y/6) + (size.y/20));
        layout.setLayoutParams(params);

        partner_toolbar = findViewById(R.id.toolbar);
        partner_toolbar.setTitle("Add partner");
        partner_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        partner_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        contact_imageView = findViewById(R.id.imageView_contact);
        contact_imageView.setImageResource(R.drawable.contact);
        ViewGroup.LayoutParams contact_params = contact_imageView.getLayoutParams();
        contact_params.height = size.y/6;
        contact_params.width = size.y/6;
        contact_imageView.setLayoutParams(contact_params);

        last_name_editText = findViewById(R.id.editText_last_name);
        first_name_editText = findViewById(R.id.editText_first_name);

        done_button = findViewById(R.id.button_done);
        done_button.setOnClickListener(e -> {
            if(last_name_editText.getText().toString().trim().equals("") ||
                    first_name_editText.getText().toString().trim().equals("")) {
                if(last_name_editText.getText().toString().trim().equals("")) {
                    last_name_editText.setError("Required.");
                } else {
                    last_name_editText.setError(null);
                }

                if(first_name_editText.getText().toString().trim().equals("")) {
                    first_name_editText.setError("Required.");
                } else {
                    first_name_editText.setError(null);
                }
            } else {
                new AlertDialog.Builder(StudentPartnerActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("The first and last name of your partner " +
                                "has to be identical to what they entered or will enter; " +
                                "are you sure you want to continue?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("last", last_name_editText.getText()
                                        .toString().trim());
                                returnIntent.putExtra("first", first_name_editText.getText()
                                        .toString().trim());
                                setResult(Activity.RESULT_OK, returnIntent);
                                finishAfterTransition();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
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
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finishAfterTransition();
    }
}

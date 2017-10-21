package meyerowitz.alex.draft;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;

public class TeacherRosterActivity extends ListActivity {
    public static final String EXTRA_NAMES = "EXTRA_NAMES";
    public static final String EXTRA_EMAILS = "EXTRA_EMAILS";

    private RelativeLayout list_layout;
    private RelativeLayout singlePerson_layout;
    private RelativeLayout partnered_layout;
    private ImageView email_imageView;
    private TextView singlePerson_textView;
    private ImageView emailFirst_imageView;
    private TextView partneredFirst_textView;
    private ImageView emailSecond_imageView;
    private TextView partneredSecond_textView;
    private Toolbar roster_toolbar;

    private ArrayList<String> names;
    private ArrayList<String> emails;
    private int shortAnimationDuration;
    private int lastPositionPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_roster);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        lastPositionPressed = -1;

        email_imageView = findViewById(R.id.imageView_email);
        email_imageView.setImageResource(R.drawable.ic_email);
        singlePerson_textView = findViewById(R.id.textView_singlePerson);

        emailFirst_imageView = findViewById(R.id.imageView_email_first);
        emailFirst_imageView.setImageResource(R.drawable.ic_email);
        partneredFirst_textView = findViewById(R.id.textView_partnered_first);

        emailSecond_imageView = findViewById(R.id.imageView_email_second);
        emailSecond_imageView.setImageResource(R.drawable.ic_email);
        partneredSecond_textView = findViewById(R.id.textView_partnered_second);

        roster_toolbar = findViewById(R.id.toolbar);
        roster_toolbar.setTitle("Current roster");
        roster_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        roster_toolbar.setNavigationOnClickListener(e -> onBackPressed());

        list_layout = findViewById(R.id.layout_list);
        partnered_layout = findViewById(R.id.layout_partnered);
        singlePerson_layout = findViewById(R.id.layout_singlePerson);

        names = getIntent().getStringArrayListExtra(EXTRA_NAMES);
        emails = getIntent().getStringArrayListExtra(EXTRA_EMAILS);

        NameAdapter adapter = new NameAdapter(names);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((NameAdapter) getListAdapter()).notifyDataSetChanged();
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(names.get(position).substring(0, 1).equals("1")) {
            if(lastPositionPressed != -1) {
                partnered_layout.animate()
                        .alpha(0f)
                        .setDuration(160)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                partnered_layout.setVisibility(View.GONE);

                                singlePerson_textView.setText(emails.get(position));
                                singlePerson_layout.setVisibility(View.VISIBLE);
                                singlePerson_layout.animate()
                                        .alpha(1f)
                                        .setDuration(160)
                                        .setListener(null);
                            }
                        });
            } else {
                partnered_layout.setVisibility(View.GONE);

                singlePerson_textView.setText(emails.get(position));
                singlePerson_layout.setVisibility(View.VISIBLE);
                singlePerson_layout.setAlpha(1f);
            }


        } else {
            if(lastPositionPressed != -1) {
                singlePerson_layout.animate()
                        .alpha(0f)
                        .setDuration(160)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                singlePerson_layout.setVisibility(View.GONE);

                                String str = emails.get(position);
                                String first = str.substring(0, str.indexOf(" "));
                                String second = str.substring(str.indexOf(" ") + 1, str.length());

                                partneredFirst_textView.setText(first);
                                partneredSecond_textView.setText(second);
                                partnered_layout.setVisibility(View.VISIBLE);
                                partnered_layout.animate()
                                        .alpha(1f)
                                        .setDuration(160)
                                        .setListener(null);
                            }
                        });
            } else {
                singlePerson_layout.setVisibility(View.GONE);

                String str = emails.get(position);
                String first = str.substring(0, str.indexOf(" "));
                String second = str.substring(str.indexOf(" ") + 1, str.length());

                partneredFirst_textView.setText(first);
                partneredSecond_textView.setText(second);
                partnered_layout.setVisibility(View.VISIBLE);
                partnered_layout.setAlpha(1f);
            }
        }

        int initialHeight =  (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 56,
                getResources().getDisplayMetrics()
        );
        int translatedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 200,
                getResources().getDisplayMetrics()
        );

        if(position == lastPositionPressed) {
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
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    partnered_layout.setVisibility(View.GONE);
                                    singlePerson_layout.setVisibility(View.GONE);
                                }
                            });
                }
            });
            anim.start();
            lastPositionPressed = -1;
        } else if(lastPositionPressed == -1) {
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
        } else {
            lastPositionPressed = position;
        }
    }

    private class NameAdapter extends ArrayAdapter<String> {
        public NameAdapter(ArrayList<String> names) {
            super(TeacherRosterActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = TeacherRosterActivity.this.getLayoutInflater()
                        .inflate(R.layout.list_item, null);
            }
            String name = getItem(position);
            boolean partnered = name.substring(0, 1).equals("2");
            name = name.substring(2, name.length());

            ImageView listItem_imageView = convertView.findViewById(R.id.imageView_listItem);
            if(partnered) {
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

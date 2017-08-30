package meyerowitz.alex.draft;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Teacher {
    private String lastName;
    private String firstName;
    private String email;
    private int numberClasses;

    public Teacher() {
        // Default constructor required for calls to DataSnapshot.getValue(Student.class)
    }

    public Teacher(String lastName, String firstName, int numberClasses, String email) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.numberClasses = numberClasses;
        this.email = email;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> teacherValues = new HashMap<>();
        teacherValues.put("lastName", lastName);
        teacherValues.put("firstName", firstName);
        teacherValues.put("numberClasses", numberClasses);
        teacherValues.put("finished", false);
        teacherValues.put("currentNumDrafted", 0);
        teacherValues.put("email", email);

        return teacherValues;
    }
}

package meyerowitz.alex.draft;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Student {
    private String lastName;
    private String firstName;
    private String partnerLast;
    private String partnerFirst;
    private String firstChoice;
    private String secondChoice;
    private String thirdChoice;
    private String projectIdea;
    private String email;
    private String fcmToken;

    public Student() {
        // Default constructor required for calls to DataSnapshot.getValue(Student.class)
    }

    public Student(String lastName, String firstName, String partnerLast, String partnerFirst, String firstChoice,
                   String secondChoice, String thirdChoice, String projectIdea, String email, String fcmToken) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.partnerLast = partnerLast;
        this.partnerFirst = partnerFirst;
        this.firstChoice = firstChoice;
        this.secondChoice = secondChoice;
        this.thirdChoice = thirdChoice;
        this.projectIdea = projectIdea;
        this.email = email;
        this.fcmToken = fcmToken;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> studentValues = new HashMap<>();
        studentValues.put("lastName", lastName);
        studentValues.put("firstName", firstName);
        studentValues.put("partnerLast", partnerLast);
        studentValues.put("partnerFirst", partnerFirst);
        studentValues.put("firstChoice", firstChoice);
        studentValues.put("secondChoice", secondChoice);
        studentValues.put("thirdChoice", thirdChoice);
        studentValues.put("projectIdea", projectIdea);
        studentValues.put("email", email);
        studentValues.put("fcm-token", fcmToken);

        return studentValues;
    }
}

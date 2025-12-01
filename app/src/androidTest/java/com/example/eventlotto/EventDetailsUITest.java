package com.example.eventlotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlotto.ui.entrant.EntEventDetailsFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * UI tests for Ent_EventDetailsFragment (entrant home screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeUITest {

    private static final String MOCK_EVENT_ID = "testEvent123";

    @Before
    public void setUp() {
        // Insert mock event into Firestore for testing
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> mockEvent = new HashMap<>();
        mockEvent.put("eventTitle", "Mock Charity Run");
        mockEvent.put("description", "A fun run for a good cause.");
        mockEvent.put("registrationOpensAt", Timestamp.now());
        mockEvent.put("registrationClosesAt", Timestamp.now());
        mockEvent.put("eventStartAt", Timestamp.now());
        mockEvent.put("eventEndAt", Timestamp.now());
        mockEvent.put("geoConsent", false);
        mockEvent.put("entrantsApplied", 0L);
        mockEvent.put("maxEntrants", 10L);

        db.collection("events").document(MOCK_EVENT_ID).set(mockEvent);
    }

    @Test
    public void testEventDetailsFragmentDisplaysUIElements() {
        FragmentScenario<EntEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        EntEventDetailsFragment.class,
                        EntEventDetailsFragment.newInstance(MOCK_EVENT_ID).getArguments(),
                        R.style.Theme_EventLotto
                );

        onView(withId(R.id.eventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.signupDates)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDates)).check(matches(isDisplayed()));
        onView(withId(R.id.joinWaitlistButton)).check(matches(isDisplayed()));
        onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testJoinWaitlistButtonClick() {
        FragmentScenario<EntEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        EntEventDetailsFragment.class,
                        EntEventDetailsFragment.newInstance(MOCK_EVENT_ID).getArguments(),
                        R.style.Theme_EventLotto
                );

        onView(withId(R.id.joinWaitlistButton)).perform(click());
        onView(withId(R.id.joinWaitlistButton)).check(matches(isDisplayed()));
    }
}


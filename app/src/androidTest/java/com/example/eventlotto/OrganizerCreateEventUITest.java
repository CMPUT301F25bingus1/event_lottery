package com.example.eventlotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlotto.ui.entrant.EntHomeFragment;
import com.example.eventlotto.ui.organizer.OrgCreateEventFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * UI tests for OrgCreateEventFragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerCreateEventUITest {

    private static final String MOCK_EVENT_ID = "testCreateEvent";
    private static final String MOCK_EVENT_ID2 = "testCreateEventOnlyOptional";

    @Before
    public void setUp() {
        // Clean up test events
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(MOCK_EVENT_ID).delete();
        db.collection("events").document(MOCK_EVENT_ID2).delete();

        SystemClock.sleep(1000);
    }

    public void disableAnimations() {
        // Disable animations via adb commands
        try {
            Runtime.getRuntime().exec(new String[]{
                    "adb", "shell", "settings", "put", "global", "window_animation_scale", "0"
            }).waitFor();
            Runtime.getRuntime().exec(new String[]{
                    "adb", "shell", "settings", "put", "global", "transition_animation_scale", "0"
            }).waitFor();
            Runtime.getRuntime().exec(new String[]{
                    "adb", "shell", "settings", "put", "global", "animator_duration_scale", "0"
            }).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAllFieldsVisible() {
        FragmentScenario<OrgCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrgCreateEventFragment.class,
                        null,
                        R.style.Theme_EventLotto
                );
        // Check all required fields are visible
        onView(withId(R.id.input_event_title)).check(matches(isDisplayed()));
        onView(withId(R.id.input_description)).check(matches(isDisplayed()));
        onView(withId(R.id.input_event_url)).check(matches(isDisplayed()));
        onView(withId(R.id.input_capacity)).check(matches(isDisplayed()));
        onView(withId(R.id.input_location)).check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        onView(withId(R.id.input_max_entrants)).check(matches(isDisplayed()));
        onView(withId(R.id.input_event_start)).check(matches(isDisplayed()));
        onView(withId(R.id.input_event_end)).check(matches(isDisplayed()));
        onView(withId(R.id.input_time_start)).check(matches(isDisplayed()));
        onView(withId(R.id.input_time_end)).check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        onView(withId(R.id.input_reg_open)).check(matches(isDisplayed()));
        onView(withId(R.id.input_reg_close)).check(matches(isDisplayed()));
        onView(withId(R.id.days_toggle_group)).check(matches(isDisplayed()));
        onView(withId(R.id.check_geo_consent)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_event)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateEventWithoutRequiredFields() {
        FragmentScenario<OrgCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrgCreateEventFragment.class,
                        null,
                        R.style.Theme_EventLotto
                );
        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        // Try to submit without filling required fields then check that screen hasn't changed
        onView(withId(R.id.btn_create_event)).perform(click());
        onView(withId(R.id.btn_create_event)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateEvent() {
        FragmentScenario<OrgCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrgCreateEventFragment.class,
                        null,
                        R.style.Theme_EventLotto
                );
        onView(withId(R.id.input_event_title)).perform(
                typeText(MOCK_EVENT_ID),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_description)).perform(
                typeText("Description for the event"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_capacity)).perform(
                typeText("10"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_location)).perform(
                typeText("40.7128, -74.0060"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_event_url)).perform(
                typeText("https://example.com/event"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_max_entrants)).perform(
                typeText("50"),
                closeSoftKeyboard()
        );

        onView(withText("Mon")).perform(click());

        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        onView(withId(R.id.check_geo_consent)).perform(click());
        onView(withId(R.id.info_geo)).perform(click());
        onView(withId(R.id.txt_geo_info)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.info_geo)).perform(click());
        onView(withId(R.id.txt_geo_info)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Submit
        onView(withId(R.id.btn_create_event)).perform(click());
    }


    @Test
    public void testOptionalFieldsNotRequired() {
        FragmentScenario<OrgCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrgCreateEventFragment.class,
                        null,
                        R.style.Theme_EventLotto
                );
        // Fill only required fields
        onView(withId(R.id.input_event_title)).perform(
                typeText(MOCK_EVENT_ID2),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_description)).perform(
                typeText("Description for testing"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_capacity)).perform(
                typeText("50"),
                closeSoftKeyboard()
        );

        onView(withId(R.id.input_location)).perform(
                typeText("40.7128, -74.0060"),
                closeSoftKeyboard()
        );

        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        onView(withId(R.id.btn_create_event)).perform(click());
    }

    @Test
    public void testScrollViewFunctionality() {
        FragmentScenario<OrgCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrgCreateEventFragment.class,
                        null,
                        R.style.Theme_EventLotto
                );
        // Scroll to bottom
        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeUp());

        // Verify bottom elements are visible
        onView(withId(R.id.btn_create_event)).check(matches(isDisplayed()));

        // Scroll back to top
        Espresso.onView(withId(R.id.create_event_scroll))
                .perform(ViewActions.swipeDown());
    }
}
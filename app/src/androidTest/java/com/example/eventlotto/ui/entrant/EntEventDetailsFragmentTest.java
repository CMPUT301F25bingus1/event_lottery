package com.example.eventlotto.ui.entrant;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlotto.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntEventDetailsFragmentTest {

    private FragmentScenario<EntEventDetailsFragment> launch() {
        Bundle args = new Bundle();
        args.putString("eventId", "fakeEvent123");

        return FragmentScenario.launchInContainer(
                EntEventDetailsFragment.class,
                args,
                R.style.Theme_EventLotto
        );
    }


    @Test
    public void testDialogUIElementsVisible() {
        launch();

        onView(withId(R.id.eventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.signupDates)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDates)).check(matches(isDisplayed()));
        onView(withId(R.id.waitlistCount)).check(matches(isDisplayed()));

        onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));
        onView(withId(R.id.joinWaitlistButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testCancelButtonClosesDialog() {
        launch();

        // Click cancel
        onView(withId(R.id.cancelButton)).perform(click());

        onView(withId(R.id.cancelButton))
                .check(doesNotExist());
    }

}

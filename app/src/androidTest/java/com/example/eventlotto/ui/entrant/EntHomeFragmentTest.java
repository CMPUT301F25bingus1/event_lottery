package com.example.eventlotto.ui.entrant;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlotto.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntHomeFragmentTest {

    private void launch() {
        FragmentScenario.launchInContainer(
                EntHomeFragment.class,
                null,
                R.style.Theme_EventLotto
        );
    }


    @Test
    public void testUIElementsVisible() {
        launch();

        onView(withId(R.id.recycler_view_events)).check(matches(isDisplayed()));
        onView(withId(R.id.filter_button)).check(matches(isDisplayed()));
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.search_button)).check(matches(isDisplayed()));
        onView(withId(R.id.help_row)).check(matches(isDisplayed()));
    }

    @Test
    public void testHelpPopupAppearsAndDismisses() {
        launch();

        // Tap "help" row
        onView(withId(R.id.help_row)).perform(click());

        // Popup should appear
        onView(withId(R.id.selection_popup)).check(matches(isDisplayed()));

        // Tap "Got it"
        onView(withId(R.id.btn_got_it)).perform(click());

        // Popup should disappear
        onView(withId(R.id.selection_popup)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testSearchFieldAcceptsInput() {
        launch();

        onView(withId(R.id.search_edit_text))
                .perform(typeText("zoo"))
                .check(matches(withText("zoo")));
    }
}

package com.example.eventlotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlotto.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testActivityLaunch_ShowsFragmentContainer() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }

    @Test
    public void testBottomNav_IsHiddenOnLaunch() {
        onView(withId(R.id.bottom_navigation)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testShowBanner_WhenSelected_ShowsHappyCat() {
        activityRule.getScenario().onActivity(activity -> {
            activity.showInAppBanner("You have been selected!", true);
        });
        onView(withText("You have been selected!")).check(matches(isDisplayed()));
    }

    // Optional: only if bottom navigation menus exist at runtime
    @Test
    public void testBottomNav_ClickProfile_ShowsProfileScreen() {
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withText("Profile")).check(matches(isDisplayed()));
    }
}

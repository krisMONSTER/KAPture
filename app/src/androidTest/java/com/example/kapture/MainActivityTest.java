package com.example.kapture;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.kapture.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testClicks(){
        //check default fragment
        onView(withId(R.id.settings_fragment)).check(matches(isDisplayed()));

        //click views in settings fragment
        onView(withId(R.id.delayEditText)).perform(click());
        pressBack();
        onView(withId(R.id.durationEditText)).perform(click());
        pressBack();
        onView(withId(R.id.chooseAlarmSoundText)).perform(click());
        pressBack();
        onView(withId(R.id.choosePhoneNumber)).perform(click());

        //open language options
        onView(withId(R.id.languageChoiceText)).perform(click());
        pressBack();
        onView(withId(R.id.ic_language)).perform(click());
        pressBack();
        //hide keyboard
        pressBack();

        //switch to history fragment
        onView(withId(R.id.ic_history)).perform(click());
        onView(withId(R.id.history_fragment)).check(matches(isDisplayed()));

        //click views in history fragment
        onView(withId(R.id.deleteAllBtn)).perform(click());

        //switch back to settings fragment
        onView(withId(R.id.ic_settings)).perform(click());
        onView(withId(R.id.settings_fragment)).check(matches(isDisplayed()));
    }
}

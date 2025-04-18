package com.example.novemberproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;


@RunWith(AndroidJUnit4.class)
public class AppEspressoTest {

    @Test
    public void testMainActivityDisplay() {
        // Check if the topic list and buttons are visible
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.hashtagListView)).check(matches(isDisplayed()));
            onView(withId(R.id.btnViewFeed)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSettings)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testGoToFeedActivity() {
        // Go to FeedActivity from MainActivity
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Click on the first topic
            onView(childAtPosition(withId(R.id.hashtagListView), 0)).perform(click());
            onView(withId(R.id.btnViewFeed)).perform(click());
        }
        // check if feed activity is properly displayed
        onView(withId(R.id.recyclerViewFeed)).check(matches(isDisplayed()));
    }

    @Test
    public void testLikeToggle() {
        // Go to feed with an intent containing one tag
        ArrayList<String> tags = new ArrayList<>();
        tags.add("nature");
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FeedActivity.class);
        intent.putStringArrayListExtra("selectedTags", tags);
        try (ActivityScenario<FeedActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for posts
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            // Toggle like on and off
            onView(withId(R.id.recyclerViewFeed))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnLike)));
            onView(withId(R.id.recyclerViewFeed))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnLike)));
        }
    }

    @Test
    public void testPaginationLoading() {
        // Go to feed with an intent containing one tag
        ArrayList<String> tags = new ArrayList<>();
        tags.add("nature");
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FeedActivity.class);
        intent.putStringArrayListExtra("selectedTags", tags);
        try (ActivityScenario<FeedActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for posts to load
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // count displayed posts
            final int[] initialCount = new int[1];
            onView(withId(R.id.recyclerViewFeed)).check((view, noViewFoundException) -> {
                RecyclerView recyclerView = (RecyclerView) view;
                initialCount[0] = recyclerView.getAdapter().getItemCount();
            });

            // scroll till end
            onView(withId(R.id.recyclerViewFeed))
                    .perform(RecyclerViewActions.scrollToPosition(initialCount[0] - 1));

            // load more items
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // check if more posts loaded based on count
            onView(withId(R.id.recyclerViewFeed)).check((view, noViewFoundException) -> {
                RecyclerView recyclerView = (RecyclerView) view;
                int newCount = recyclerView.getAdapter().getItemCount();
                if (newCount <= initialCount[0]) {
                    throw new AssertionError("Pagination didnt work" +
                            "Before count: " + initialCount[0] + ", After count: " + newCount);
                }
            });
        }
    }


    @Test
    public void testDoubleTapLike() {
        // Go to feed with an intent containing one tag
        ArrayList<String> tags = new ArrayList<>();
        tags.add("nature");
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FeedActivity.class);
        intent.putStringArrayListExtra("selectedTags", tags);
        try (ActivityScenario<FeedActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for posts to load
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            // Double tap the first image
            onView(new RecyclerViewMatcher(R.id.recyclerViewFeed).atPositionOnView(0, R.id.ivPostImage))
                    .perform(doubleClick());
            // double tap again for off toggle
            onView(new RecyclerViewMatcher(R.id.recyclerViewFeed).atPositionOnView(0, R.id.ivPostImage))
                    .perform(doubleClick());
        }
    }

    @Test
    public void testSettingsMenu() {
        try (ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class)) {
            // check display
            onView(withId(R.id.switchPulsating)).check(matches(isDisplayed()));
            onView(withId(R.id.seekBarPulseInterval)).check(matches(isDisplayed()));
            onView(withId(R.id.btnViewTracking)).check(matches(isDisplayed()));

            // Test
            onView(withId(R.id.switchPulsating)).perform(click());
            onView(withId(R.id.seekBarPulseInterval)).perform(new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return isDisplayed();
                }
                @Override
                public String getDescription() {
                    return "";
                }
                @Override
                public void perform(UiController uiController, View view) {
                    if (view instanceof android.widget.SeekBar) {
                        ((android.widget.SeekBar) view).setProgress(10);
                    }
                }
            });
            onView(withId(R.id.tvPulseIntervalValue)).check(matches(withText("10")));
            onView(withId(R.id.btnViewTracking)).perform(click());
        }
        onView(withId(R.id.toolbarTracking)).check(matches(isDisplayed()));
    }


    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "";
            }
            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(id);
                if (child != null && child.isClickable()) {
                    child.performClick();
                }
            }
        };
    }

    public static Matcher<View> childAtPosition(final Matcher<View> parentMatcher, final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position);
                parentMatcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return (parent instanceof ViewGroup) && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static class RecyclerViewMatcher {
        private final int recyclerViewId;
        public RecyclerViewMatcher(int recyclerViewId) {
            this.recyclerViewId = recyclerViewId;
        }
        public Matcher<View> atPosition(final int position) {
            return atPositionOnView(position, -1);
        }
        public Matcher<View> atPositionOnView(final int position, final int targetViewId) {
            return new TypeSafeMatcher<View>() {
                View childView;
                @Override
                public void describeTo(Description description) {
                    description.appendText("ID: " + recyclerViewId + " Position: " + position);
                }
                @Override
                public boolean matchesSafely(View view) {
                    if (childView == null) {
                        RecyclerView recyclerView = view.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null && recyclerView.getId() == recyclerViewId) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                            if (viewHolder != null) {
                                childView = viewHolder.itemView;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    if (targetViewId == -1) {
                        return view == childView;
                    } else {
                        View targetView = childView.findViewById(targetViewId);
                        return view == targetView;
                    }
                }
            };
        }
    }
}

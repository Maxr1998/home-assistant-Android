package io.homeassistant.android;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginTest {

    private static SharedPreferences PREFS;
    @Rule
    public ActivityTestRule<HassActivity> mActivityTestRule = new ActivityTestRule<>(HassActivity.class);
    private Resources testRes;

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @SuppressLint("ApplySharedPref")
    @BeforeClass
    public static void setup() {
        PREFS = Utils.getPrefs(InstrumentationRegistry.getTargetContext());
        PREFS.edit().clear().commit();
        PREFS.edit().clear().apply();
    }

    @Before
    public void before() {
        testRes = InstrumentationRegistry.getContext().getResources();
    }

    @Test
    public void hassActivityDefaultTest() {
        hassActivityTest(testRes.getString(io.homeassistant.android.test.R.string.test_hass_url), testRes.getString(io.homeassistant.android.test.R.string.test_hass_password));
    }

    @Test
    public void hassActivityUnprotectedTest() {
        hassActivityTest(testRes.getString(io.homeassistant.android.test.R.string.test_unprotected_hass_url), null);
    }

    @Test
    public void hassActivityBasicAuthTest() {
        hassActivityTest(testRes.getString(io.homeassistant.android.test.R.string.test_basic_auth_hass_url), testRes.getString(io.homeassistant.android.test.R.string.test_hass_password));
    }

    private void hassActivityTest(String url, String password) {
        sleep(1000);
        final HassActivity activity = mActivityTestRule.getActivity();
        // Check prefs
        PREFS = Utils.getPrefs(activity);

        assertFalse(PREFS.contains(Common.PREF_HASS_URL_KEY));
        assertFalse(PREFS.contains(Common.PREF_HASS_PASSWORD_KEY));

        // Login
        ViewInteraction textInput = onView(
                allOf(withId(R.id.url_input),
                        childAtPosition(childAtPosition(withId(R.id.url_input_layout), 0), 0),
                        isDisplayed()));
        textInput.perform(replaceText(url), closeSoftKeyboard());

        if (password != null) {
            ViewInteraction passwordInput = onView(
                    allOf(withId(R.id.password_input),
                            childAtPosition(childAtPosition(withId(R.id.password_input_layout), 0), 0),
                            isDisplayed()));
            passwordInput.perform(replaceText(password));
        }

        ViewInteraction connectButton = onView(
                allOf(withId(R.id.connect_button),
                        withText(R.string.button_connect),
                        childAtPosition(childAtPosition(withId(R.id.login_layout), 3), 1),
                        isDisplayed()));
        connectButton.perform(click());

        sleep(1000);

        AtomicBoolean basicAuthRequired = new AtomicBoolean(false);
        onView(withText(R.string.dialog_basic_auth_title)).withFailureHandler((throwable, matcher) -> basicAuthRequired.set(true))
                .check(doesNotExist());

        if (basicAuthRequired.get()) {
            onView(allOf(withId(R.id.dialog_basic_auth_username), isDisplayed()))
                    .perform(replaceText(testRes.getString(io.homeassistant.android.test.R.string.test_basic_auth_user)));

            onView(allOf(withId(R.id.dialog_basic_auth_password), isDisplayed()))
                    .perform(replaceText(testRes.getString(io.homeassistant.android.test.R.string.test_basic_auth_password)));

            onView(allOf(withId(android.R.id.button1), withText(R.string.dialog_basic_auth_button_login), isDisplayed()))
                    .perform(click());

            sleep(1000);
        }

        // Assert service states
        HassService service = activity.service;

        assertFalse(service.connecting.get());
        assertTrue(service.connected.get());
        assertEquals(service.authenticationState.get(), HassService.AUTH_STATE_AUTHENTICATED);

        // Recreate and re-assert that still connected
        Handler handler = new Handler(activity.getMainLooper());
        handler.post(activity::recreate);
        sleep(20);

        assertFalse(service.connecting.get());
        assertTrue(service.connected.get());
        assertEquals(service.authenticationState.get(), HassService.AUTH_STATE_AUTHENTICATED);

        // Logout
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        ViewInteraction logoutButton = onView(allOf(withId(R.id.title), withText(R.string.menu_logout), isDisplayed()));
        logoutButton.perform(click());

        sleep(200);

        onView(withId(R.id.login_layout)).check(ViewAssertions.matches(isDisplayed()));

        assertFalse(service.connecting.get());
        assertFalse(service.connected.get());
        assertEquals(service.authenticationState.get(), HassService.AUTH_STATE_NOT_AUTHENTICATED);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
package io.homeassistant.android;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static io.homeassistant.android.IsEqualTrimmingAndIgnoringCase.equalToTrimmingAndIgnoringCase;
import static io.homeassistant.android.VisibleViewMatcher.isVisible;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HassActivityTest {

  @Rule
  public ActivityTestRule<HassActivity> mActivityTestRule =
      new ActivityTestRule<>(HassActivity.class);

  @Test
  public void hassActivityTest() {
    System.out.println("Starting run of ETGTestCaseForPR");
    ViewInteraction android_widget_EditText =
        onView(
            allOf(
                withId(R.id.url_input),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.url_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_EditText.perform(replaceText("Maltz"));

    ViewInteraction root = onView(isRoot());
    root.perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction android_widget_ImageButton =
        onView(
            allOf(
                withId(R.id.text_input_password_toggle),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.password_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_ImageButton.perform(getClickAction());

    ViewInteraction root2 = onView(isRoot());
    root2.perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    ViewInteraction android_widget_Button =
        onView(
            allOf(
                withId(R.id.connect_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CONNECT")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))));
    android_widget_Button.perform(getClickAction());

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction root3 = onView(isRoot());
    root3.perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    ViewInteraction android_widget_EditText2 =
        onView(
            allOf(
                withId(R.id.url_input),
                withTextOrHint(equalToTrimmingAndIgnoringCase("Maltz")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.url_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_EditText2.perform(replaceText("stony-blind"));

    ViewInteraction root4 = onView(isRoot());
    root4.perform(getSwipeAction(540, 897, 540, 1794));

    waitToScrollEnd();

    ViewInteraction android_widget_EditText3 =
        onView(
            allOf(
                withId(R.id.url_input),
                withTextOrHint(equalToTrimmingAndIgnoringCase("stony-blind")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.url_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_EditText3.perform(replaceText("Maydelle"));

    ViewInteraction root5 = onView(isRoot());
    root5.perform(getSwipeAction(540, 897, 540, 1794));

    waitToScrollEnd();

    ViewInteraction android_widget_EditText4 =
        onView(
            allOf(
                withId(R.id.password_input),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.password_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_EditText4.perform(replaceText("bungle kalpa"));

    ViewInteraction android_widget_Button2 =
        onView(
            allOf(
                withId(R.id.connect_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CONNECT")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))));
    android_widget_Button2.perform(getClickAction());

    ViewInteraction android_widget_EditText5 =
        onView(
            allOf(
                withId(R.id.password_input),
                withTextOrHint(equalToTrimmingAndIgnoringCase("bungle kalpa")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.password_input_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))))));
    android_widget_EditText5.perform(replaceText("cyaphenine"));

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction android_widget_Button3 =
        onView(
            allOf(
                withId(R.id.connect_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CONNECT")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.login_layout), isDescendantOfA(withId(R.id.root))))));
    android_widget_Button3.perform(getClickAction());

    try {
      Espresso.pressBack();
    } catch (NoActivityResumedException e) {
      // expected
    }
  }

  private static Matcher<View> withTextOrHint(final Matcher<String> stringMatcher) {
    return anyOf(withText(stringMatcher), withHint(stringMatcher));
  }

  private ViewAction getSwipeAction(
      final int fromX, final int fromY, final int toX, final int toY) {
    return ViewActions.actionWithAssertions(
        new GeneralSwipeAction(
            Swipe.SLOW,
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                float[] coordinates = {fromX, fromY};
                return coordinates;
              }
            },
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                float[] coordinates = {toX, toY};
                return coordinates;
              }
            },
            Press.FINGER));
  }

  private void waitToScrollEnd() {
    SystemClock.sleep(500);
  }

  private ClickWithoutDisplayConstraint getClickAction() {
    return new ClickWithoutDisplayConstraint(
        Tap.SINGLE,
        GeneralLocation.VISIBLE_CENTER,
        Press.FINGER);
  }
}

package com.williamzabot.notascoloridas.ui.notes

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.ui.ToastMatcher
import com.williamzabot.notascoloridas.ui.main.MainActivity
import com.williamzabot.notascoloridas.ui.note.NoteFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class NoteFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        navController.setGraph(R.navigation.main_graph)
        val bundle = Bundle()
        bundle.putParcelable("", null)
        val scenario = launchFragmentInContainer(bundle) {
            NoteFragment()
                .also { noteFragment ->
                    noteFragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                        if (viewLifecycleOwner != null) {
                            Navigation.setViewNavController(
                                noteFragment.requireView(),
                                navController
                            )
                        }
                    }
                }
        }
    }

    @Test
    fun test_components_isVisible() {
        onView(withId(R.id.constraint_notes_formulary)).check(matches(isDisplayed()))
        onView(withId(R.id.formulary_note_title)).check(matches(isDisplayed()))
        onView(withId(R.id.formulary_note_description)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_colors)).check(matches(isDisplayed()))
    }


    @Test
    fun test_clickSaveNote_withEmptyDescription() {
        onView(withId(R.id.formulary_note_title)).perform(typeText("Title"))
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.save_note)).perform(click())
        onView(withText(R.string.empty_fields)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun test_clickSaveNote_withEmptyTitle() {
        onView(withId(R.id.formulary_note_description)).perform(typeText("Description"))
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.save_note)).perform(click())
        onView(withText(R.string.empty_fields)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun test_clickSaveNote_withFieldsOk() {
        onView(withId(R.id.formulary_note_title)).perform(typeText("Title"))
        onView(withId(R.id.formulary_note_description)).perform(typeText("Description"))
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.save_note)).perform(click())
        onView(withText(R.string.note_added)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

}
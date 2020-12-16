package com.williamzabot.notascoloridas.ui.notes

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.ui.main.MainActivity
import com.williamzabot.notascoloridas.ui.noteslist.NotesListFragment
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class NotesListFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        navController.setGraph(R.navigation.main_graph)

        val scenario = launchFragmentInContainer() {
            NotesListFragment()
                .also { fragment ->
                    fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                        if (viewLifecycleOwner != null) {
                            Navigation.setViewNavController(fragment.requireView(), navController)
                        }
                    }
                }
        }
    }


    @Test
    fun test_components_isVisible() {
        onView(withId(R.id.constraint_fragment_noteslist)).check(matches(isDisplayed()))
        onView(withId(R.id.txt_add_note)).check(matches(isDisplayed()))
        //onView(withId(R.id.recyclerview_noteslist)).check(matches(isDisplayed()))
    }

    @Test
    fun test_textViewAddNoteClicked_destination() {
        onView(withId(R.id.txt_add_note)).perform(click())
        Assert.assertEquals(navController.currentDestination?.id, R.id.noteFragment)
    }
}
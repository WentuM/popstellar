package com.github.dedis.popstellar.ui.lao.socialmedia

import android.content.res.Configuration
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.dedis.popstellar.R
import com.github.dedis.popstellar.model.objects.Lao.Companion.generateLaoId
import com.github.dedis.popstellar.model.objects.security.KeyPair
import com.github.dedis.popstellar.testutils.Base64DataUtils
import com.github.dedis.popstellar.testutils.BundleBuilder
import com.github.dedis.popstellar.testutils.fragment.ActivityFragmentScenarioRule
import com.github.dedis.popstellar.testutils.pages.lao.LaoActivityPageObject
import com.github.dedis.popstellar.testutils.pages.lao.socialmedia.SocialMediaHomePageObject
import com.github.dedis.popstellar.ui.lao.LaoActivity
import com.github.dedis.popstellar.utility.Constants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoTestRule

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SocialMediaHomeFragmentTest {
  @JvmField @Rule var rule = InstantTaskExecutorRule()

  @JvmField @Rule(order = 0) val mockitoRule: MockitoTestRule = MockitoJUnit.testRule(this)

  @JvmField @Rule(order = 1) val hiltRule = HiltAndroidRule(this)

  @JvmField
  @Rule(order = 2)
  val setupRule: ExternalResource =
    object : ExternalResource() {
      override fun before() {
        hiltRule.inject()
      }
    }

  @JvmField
  @Rule(order = 3)
  var activityScenarioRule: ActivityFragmentScenarioRule<LaoActivity, SocialMediaHomeFragment> =
    ActivityFragmentScenarioRule.launchIn(
      LaoActivity::class.java,
      BundleBuilder().putString(Constants.LAO_ID_EXTRA, LAO_ID).build(),
      LaoActivityPageObject.containerId(),
      SocialMediaHomeFragment::class.java
    ) {
      SocialMediaHomeFragment()
    }

  @Test
  fun testBackButtonBehaviour() {
    SocialMediaHomePageObject.getRootView().perform(ViewActions.pressBack())
    // Check current fragment displayed is event list
    SocialMediaHomePageObject.getEventListFragment()
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
  }

  @Test
  fun handleRotationTest() {
    activityScenarioRule.scenario.onActivity { activity: LaoActivity ->
      val before = activity.supportFragmentManager.findFragmentById(R.id.fragment_container_lao)
      val config = Configuration(activity.resources.configuration)
      config.orientation = Configuration.ORIENTATION_LANDSCAPE
      activity.onConfigurationChanged(config)
      val after = activity.supportFragmentManager.findFragmentById(R.id.fragment_container_lao)

      Assert.assertEquals(after, before)
      Assert.assertTrue(after is SocialMediaHomeFragment)
    }
  }

  companion object {
    private const val CREATION_TIME: Long = 1631280815
    private const val LAO_NAME = "laoName"
    private val SENDER_KEY_1: KeyPair = Base64DataUtils.generatePoPToken()
    private val SENDER_KEY_2: KeyPair = Base64DataUtils.generatePoPToken()
    private val SENDER_1 = SENDER_KEY_1.publicKey
    private val LAO_ID = generateLaoId(SENDER_1, CREATION_TIME, LAO_NAME)
  }
}

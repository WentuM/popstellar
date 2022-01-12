package com.github.dedis.popstellar.ui.socialmedia;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.dedis.popstellar.R;
import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.utility.ActivityUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
import java.util.function.Supplier;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SocialMediaActivity extends AppCompatActivity {
  private SocialMediaViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.social_media_activity);
    mViewModel = obtainViewModel(this);

    // When we launch the social media from a lao, it directly sets its id and name
    if (getIntent().getExtras().get("OPENED_FROM").equals("LaoDetailActivity")) {
      mViewModel.setLaoId((String) getIntent().getExtras().get("LAO_ID"));
      mViewModel.setLaoName((String) getIntent().getExtras().get("LAO_NAME"));
    }
    setupSocialMediaHomeFragment();
    setupNavigationBar();

    // Subscribe to "lao name" string
    mViewModel
        .getLaoName()
        .observe(
            this,
            newLaoName -> {
              if (newLaoName != null) {
                Objects.requireNonNull(getSupportActionBar())
                    .setTitle(String.format("popstellar - %s", newLaoName));
              }
            });

    // Subscribe to "open home" event
    mViewModel
        .getOpenHomeEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupSocialMediaHomeFragment();
              }
            });

    // Subscribe to "open search" event
    mViewModel
        .getOpenSearchEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupSocialMediaSearchFragment();
              }
            });

    // Subscribe to "open following" event
    mViewModel
        .getOpenFollowingEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupSocialMediaFollowingFragment();
              }
            });

    // Subscribe to "open profile" event
    mViewModel
        .getOpenProfileEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupSocialMediaProfileFragment();
              }
            });
  }

  public static SocialMediaViewModel obtainViewModel(FragmentActivity activity) {
    return new ViewModelProvider(activity).get(SocialMediaViewModel.class);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.social_media_top_menu, menu);

    // Get the submenu and clear its unique item. The item was needed to create the submenu
    SubMenu laosList = menu.findItem(R.id.laos_list).getSubMenu();

    // Adding all currently opened lao name to the submenu
    mViewModel
        .getLAOs()
        .observe(
            this,
            LAOsList -> {
              if (LAOsList != null) {
                laosList.clear();
                for (int i = 0; i < LAOsList.size(); ++i) {
                  // Creating a unique id using laos_list and laos ids such that it doesn't override
                  // them in onOptionsItemSelected
                  laosList.add(
                      Menu.NONE, R.id.laos_list + R.id.laos + i, i, LAOsList.get(i).getName());
                }
              }
            });

    return true;
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Retrieve the index of the lao within the list
    int i = item.getItemId() - R.id.laos_list - R.id.laos;
    if (mViewModel.getLAOs().getValue() != null) {
      if (i >= 0) {
        Lao lao = mViewModel.getLAOs().getValue().get(i);
        mViewModel.setLaoId(lao.getId());
        mViewModel.setLaoName(lao.getName());
        return true;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @SuppressLint("NonConstantResourceId")
  public void setupNavigationBar() {
    BottomNavigationView bottomNavigationView =
        (BottomNavigationView) findViewById(R.id.social_media_nav_bar);
    bottomNavigationView.setOnItemSelectedListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.social_media_home_menu:
              mViewModel.openHome();
              break;
            case R.id.social_media_search_menu:
              mViewModel.openSearch();
              break;
            case R.id.social_media_following_menu:
              mViewModel.openFollowing();
              break;
            case R.id.social_media_profile_menu:
              mViewModel.openProfile();
              break;
            default:
          }
          return true;
        });
  }

  public void setupSocialMediaHomeFragment() {
    setCurrentFragment(R.id.fragment_social_media_home, SocialMediaHomeFragment::newInstance);
  }

  public void setupSocialMediaSearchFragment() {
    setCurrentFragment(R.id.fragment_social_media_search, SocialMediaSearchFragment::newInstance);
  }

  public void setupSocialMediaFollowingFragment() {
    setCurrentFragment(
        R.id.fragment_social_media_following, SocialMediaFollowingFragment::newInstance);
  }

  public void setupSocialMediaProfileFragment() {
    setCurrentFragment(R.id.fragment_social_media_profile, SocialMediaProfileFragment::newInstance);
  }

  /**
   * Set the current fragment in the container of the activity
   *
   * @param id of the fragment
   * @param fragmentSupplier provides the fragment if it is missing
   */
  private void setCurrentFragment(@IdRes int id, Supplier<Fragment> fragmentSupplier) {
    Fragment fragment = getSupportFragmentManager().findFragmentById(id);
    // If the fragment was not created yet, create it now
    if (fragment == null) fragment = fragmentSupplier.get();

    // Set the new fragment in the container
    ActivityUtils.replaceFragmentInActivity(
        getSupportFragmentManager(), fragment, R.id.fragment_container_social_media);
  }
}

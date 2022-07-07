package com.github.dedis.popstellar.ui.digitalcash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.dedis.popstellar.R;
import com.github.dedis.popstellar.ui.detail.LaoDetailActivity;
import com.github.dedis.popstellar.utility.ActivityUtils;
import com.github.dedis.popstellar.utility.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.function.Supplier;

import dagger.hilt.android.AndroidEntryPoint;

/** Activity for the digital cash */
@AndroidEntryPoint
public class DigitalCashActivity extends AppCompatActivity {
  private DigitalCashViewModel mViewModel;
  public static final String TAG = DigitalCashActivity.class.getSimpleName();
  private BottomNavigationView bottomNavigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.digital_cash_main_activity);
    mViewModel = obtainViewModel(this);
    setupNavigationBar();
    setupBackButton();

    setTheIntent();
    // Subscribe to "open home"
    openHomeEvent();
    // Subscribe to "open history"
    openHistoryEvent();
    // Subscribe to "open send"
    openSendEvent();
    // Subscribe to "open receive"
    openReceiveEvent();
    // Subscribe to "open issue"
    openIssueEvent();
    // Subscribe to "open receipt"
    openReceiptEvent();
    // subscribe to "return to lao"
    laoReturnEvent();

    mViewModel.openHome();
  }

  public void setTheIntent() {
    if (getIntent().getExtras() != null) {
      String id = getIntent().getExtras().getString(Constants.LAO_ID_EXTRA, "");
      mViewModel.subscribeToLao(id);
      mViewModel.setLaoId(id);
      mViewModel.setLaoName(getIntent().getExtras().getString(Constants.LAO_NAME, ""));
      mViewModel.setRollCallId(getIntent().getExtras().getString(Constants.ROLL_CALL_ID, ""));
    }
  }

  public void laoReturnEvent() {
    mViewModel
        .getOpenReturnLAO()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open return to lao");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                openLaoDetailActivity();
              }
            });
  }

  public void openSendEvent() {
    mViewModel
        .getOpenSendEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash send Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupFragment(R.id.fragment_digital_cash_send);
              }
            });
  }

  public void openReceiveEvent() {
    // Subscribe to "open receive"
    mViewModel
        .getOpenReceiveEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash receive Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupFragment(R.id.fragment_digital_cash_receive);
              }
            });
  }

  public void openHomeEvent() {
    // Subscribe to "open home"
    mViewModel
        .getOpenHomeEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash home Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupFragment(R.id.fragment_digital_cash_home);
              }
            });
  }

  public void openIssueEvent() {
    // Subscribe to "open issue"
    mViewModel
        .getOpenIssueEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash issue Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();

              if (event != null) {
                Log.d(
                    TAG,
                    "the key from organizer is : "
                        + mViewModel.getCurrentLaoValue().getOrganizer().getEncoded());
                Log.d(
                    TAG,
                    "the key from the person is "
                        + mViewModel.getKeyManager().getMainPublicKey().getEncoded());
                if (!mViewModel
                    .getCurrentLaoValue()
                    .getOrganizer()
                    .getEncoded()
                    .equals(mViewModel.getKeyManager().getMainPublicKey().getEncoded())) {
                  Toast.makeText(this, "You have to be the Lao Organizer", Toast.LENGTH_SHORT)
                      .show();
                } else {

                  setupFragment(R.id.fragment_digital_cash_issue);
                }
              }
            });
  }

  public void openHistoryEvent() {
    // Subscribe to "open history"
    mViewModel
        .getOpenHistoryEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash history Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupFragment(R.id.fragment_digital_cash_history);
              }
            });
  }

  public void openReceiptEvent() {
    // Subscribe to "open receipt"
    mViewModel
        .getOpenReceiptEvent()
        .observe(
            this,
            booleanEvent -> {
              Log.d(TAG, "Open digital cash receipt Fragment");
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupFragment(R.id.fragment_digital_cash_receipt);
              }
            });
  }

  public void openLaoDetailActivity() {
    Intent intent = new Intent(this, LaoDetailActivity.class);
    Log.d(
        TAG,
        "Trying to open lao detail for lao with id " + mViewModel.getCurrentLaoValue().getId());
    intent.putExtra(Constants.LAO_ID_EXTRA, mViewModel.getCurrentLaoValue().getId());
    intent.putExtra(Constants.FRAGMENT_TO_OPEN_EXTRA, Constants.LAO_DETAIL_EXTRA);

    startActivity(intent);
  }

  public void openLao() {
    mViewModel.returnLAO();
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
    if (menuItem.getItemId() == android.R.id.home) {
      Fragment fragment =
          getSupportFragmentManager().findFragmentById(R.id.fragment_container_digital_cash);
      if (fragment instanceof DigitalCashHomeFragment) {
        openLao();
      } else {
        bottomNavigationView.setSelectedItemId(R.id.home_coin);
      }
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  private void setupBackButton() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  public static DigitalCashViewModel obtainViewModel(FragmentActivity activity) {
    return new ViewModelProvider(activity).get(DigitalCashViewModel.class);
  }

  public void setupNavigationBar() {
    bottomNavigationView = findViewById(R.id.digital_cash_nav_bar);
    bottomNavigationView.setOnItemSelectedListener(
        item -> {
          int id = item.getItemId();
          if (id == R.id.home_coin) {
            mViewModel.openHome();
          } else if (id == R.id.history_coin) {
            mViewModel.openHistory();
          } else if (id == R.id.send_coin) {
            mViewModel.openSend();
          } else if (id == R.id.receive_coin) {
            mViewModel.openReceive();
          } else if (id == R.id.issue_coin) {
            mViewModel.openIssue();
          }
          return true;
        });
  }

  public void setupFragment(int id) {
    if (id == R.id.fragment_digital_cash_home) {
      setCurrentFragment(R.id.fragment_digital_cash_home, DigitalCashHomeFragment::newInstance);
    } else if (id == R.id.fragment_digital_cash_history) {
      setCurrentFragment(
          R.id.fragment_digital_cash_history, DigitalCashHistoryFragment::newInstance);
    } else if (id == R.id.fragment_digital_cash_send) {
      setCurrentFragment(R.id.fragment_digital_cash_send, DigitalCashSendFragment::newInstance);
    } else if (id == R.id.fragment_digital_cash_receive) {
      setCurrentFragment(
          R.id.fragment_digital_cash_receive, DigitalCashReceiveFragment::newInstance);
    } else if (id == R.id.fragment_digital_cash_issue) {
      setCurrentFragment(R.id.fragment_digital_cash_issue, DigitalCashIssueFragment::newInstance);
    } else if (id == R.id.fragment_digital_cash_receipt) {
      setCurrentFragment(
          R.id.fragment_digital_cash_receipt, DigitalCashReceiptFragment::newInstance);
    }
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
        getSupportFragmentManager(), fragment, R.id.fragment_container_digital_cash);
  }
}
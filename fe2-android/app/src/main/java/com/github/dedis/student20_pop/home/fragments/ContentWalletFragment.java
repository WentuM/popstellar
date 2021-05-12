package com.github.dedis.student20_pop.home.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.github.dedis.student20_pop.databinding.FragmentContentWalletBinding;
import com.github.dedis.student20_pop.databinding.FragmentHomeBinding;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.home.adapters.LAOListAdapter;
import com.github.dedis.student20_pop.model.Lao;

import java.util.ArrayList;

/** Fragment used to display the content wallet UI */
public class ContentWalletFragment extends Fragment {
  public static final String TAG = ContentWalletFragment.class.getSimpleName();
  public static ContentWalletFragment newInstance() {
    return new ContentWalletFragment();
  }

  private FragmentHomeBinding mHomeFragBinding;
  private HomeViewModel mHomeViewModel;
  private LAOListAdapter mListAdapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mHomeFragBinding = FragmentHomeBinding.inflate(inflater, container, false);

    mHomeViewModel = HomeActivity.obtainViewModel(getActivity());

    mHomeFragBinding.setViewmodel(mHomeViewModel);
    mHomeFragBinding.setLifecycleOwner(getActivity());

    return mHomeFragBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupListAdapter();
    setupListUpdates();
  }

  private void setupListUpdates() {
    mHomeViewModel
            .getLAOs()
            .observe(
                getActivity(),
                laos -> {
                  Log.d(TAG, "Got a list update");

                  mListAdapter.replaceList(laos);

                  // TODO: perhaps move this to data binding
                  if (laos.size() > 0) {
                    mHomeFragBinding.welcomeScreen.setVisibility(View.GONE);
                    mHomeFragBinding.listScreen.setVisibility(View.VISIBLE);
                  }
                });
  }

  private void setupListAdapter() {
    ListView listView = mHomeFragBinding.laoList;

    mListAdapter = new LAOListAdapter(new ArrayList<Lao>(0), mHomeViewModel, getActivity(), false);

    listView.setAdapter(mListAdapter);
  }
}

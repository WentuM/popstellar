package com.github.dedis.student20_pop.detail.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dedis.student20_pop.databinding.FragmentLaoDetailBinding;
import com.github.dedis.student20_pop.databinding.FragmentLaoWalletBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.detail.adapters.WalletListAdapter;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.event.Event;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

public class LaoWalletFragment extends Fragment {
    public static final String TAG = LaoWalletFragment.class.getSimpleName();

    private LaoDetailViewModel mLaoDetailViewModel;
    private WalletListAdapter mWalletListAdapter;
    private FragmentLaoWalletBinding mFragmentLaoWalletBinding;

    public static LaoWalletFragment newInstance() {
        return new LaoWalletFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mFragmentLaoWalletBinding = FragmentLaoWalletBinding.inflate(inflater, container, false);

        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());
        Log.d(TAG, "hihi");

        return mFragmentLaoWalletBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupWalletListAdapter();
        setupWalletListUpdates();

        mLaoDetailViewModel
                .getLaoRollCalls()
                .observe(
                        getActivity(),
                        rollCalls -> {
                            Log.d(TAG, "Got a list update for LAO roll calls");
                            mWalletListAdapter.replaceList(rollCalls);
                        });

        /*mLaoDetailViewModel
                .getCurrentLao()
                .observe(
                        getActivity(),
                        lao -> {
                            Bitmap myBitmap = QRCode.from(lao.getChannel()).bitmap();
                            mLaoDetailFragBinding.channelQrCode.setImageBitmap(myBitmap);
                        });*/
    }

    private void setupWalletListAdapter() {
        ListView listView = mFragmentLaoWalletBinding.walletList;

        mWalletListAdapter = new WalletListAdapter(new ArrayList<RollCall>(0), mLaoDetailViewModel, getActivity());

        listView.setAdapter(mWalletListAdapter);
    }

    private void setupWalletListUpdates() {
        mLaoDetailViewModel
                .getLaoRollCalls()
                .observe(
                        getActivity(),
                        rollCalls -> {
                            Log.d(TAG, "Got a wallet list update");
                            mWalletListAdapter.replaceList(rollCalls);
                        }
                );
    }
}

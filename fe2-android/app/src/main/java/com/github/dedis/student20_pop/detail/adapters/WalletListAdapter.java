package com.github.dedis.student20_pop.detail.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;

import com.github.dedis.student20_pop.databinding.LayoutLaoHomeBinding;
import com.github.dedis.student20_pop.databinding.LayoutRollCallEventBinding;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.home.listeners.LAOItemUserActionsListener;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.event.EventState;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletListAdapter extends BaseAdapter {
    private List<RollCall> rollCalls;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
    private LifecycleOwner lifecycleOwner;
    private final LaoDetailViewModel viewModel;

    public WalletListAdapter(List<RollCall> rollCalls, LaoDetailViewModel viewModel, LifecycleOwner activity) {
        this.viewModel = viewModel;
        setList(rollCalls);
        lifecycleOwner = activity;
    }

    public void replaceList(List<RollCall> rollCalls) {
        setList(rollCalls);
    }

    private void setList(List<RollCall> rollCalls) {
        this.rollCalls = rollCalls;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rollCalls != null ? rollCalls.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return rollCalls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutRollCallEventBinding binding;
        if (view == null) {
            // inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            binding = LayoutRollCallEventBinding.inflate(inflater, viewGroup, false);
        } else {
            binding = DataBindingUtil.getBinding(view);
        }

        RollCall rollCall = rollCalls.get(position);
        binding.rollcallDate.setText("Start: "+DATE_FORMAT.format(new Date(1000*rollCall.getStart())));
        binding.rollcallTitle.setText("Roll Call: "+rollCall.getName());
        binding.rollcallLocation.setText("Location: "+rollCall.getLocation());

        binding.rollcallOpenButton.setVisibility(View.GONE);
        binding.rollcallReopenButton.setVisibility(View.GONE);
        binding.rollcallScheduledButton.setVisibility(View.GONE);
        binding.rollcallEnterButton.setVisibility(View.GONE);
        binding.rollcallClosedButton.setVisibility(View.GONE);

        binding.rollcallAttendeesListButton.setVisibility(View.VISIBLE);

        binding.rollcallAttendeesListButton.setOnClickListener(
                clicked -> viewModel.openAttendeesList(rollCall.getId())
        );

        /*LAOItemUserActionsListener userActionsListener =
                new LAOItemUserActionsListener() {
                    @Override
                    public void onLAOClicked(Lao lao) {
                        if(openLaoDetail) {
                            homeViewModel.openLAO(lao.getChannel());
                        }else{
                            homeViewModel.openLaoWallet(lao.getChannel());
                        }
                    }
                };*/

        binding.setLifecycleOwner(lifecycleOwner);
        binding.executePendingBindings();

        return binding.getRoot();
    }
}

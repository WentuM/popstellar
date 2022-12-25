package com.github.dedis.popstellar.ui.digitalcash;

import android.os.Bundle;
import android.util.Log;
import android.view.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.github.dedis.popstellar.R;
import com.github.dedis.popstellar.utility.error.ErrorUtils;
import com.github.dedis.popstellar.utility.error.keys.KeyException;

public class DigitalCashHistoryFragment extends Fragment {
  private static final String TAG = DigitalCashHistoryFragment.class.getSimpleName();

  public static DigitalCashHistoryFragment newInstance() {
    return new DigitalCashHistoryFragment();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.digital_cash_history_fragment, container, false);
    DigitalCashViewModel viewModel = DigitalCashActivity.obtainViewModel(getActivity());
    RecyclerView transactionList = view.findViewById(R.id.transaction_history_list);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    RecyclerView.ItemDecoration decoration =
        new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

    HistoryListAdapter adapter;
    adapter = new HistoryListAdapter(viewModel, requireActivity());

    transactionList.setLayoutManager(layoutManager);
    transactionList.addItemDecoration(decoration);
    transactionList.setAdapter(adapter);

    // Update dynamically the events in History
    try {
      viewModel.addDisposable(
          viewModel
              .getTransactionsObservable()
              .subscribe(
                  adapter::setList, error -> Log.d(TAG, "error with history update " + error)));
    } catch (KeyException e) {
      ErrorUtils.logAndShow(requireContext(), TAG, e, R.string.error_retrieve_own_token);
    }
    //    viewModel
    //        .getTransactionHistory()
    //        .observe(
    //            requireActivity(),
    //            transactionObjects -> {
    //              if (transactionObjects != null) {
    //                transactionObjects.forEach(object -> Log.d(TAG, "Object is " +
    // object.toString()));
    //                Log.d(TAG, "Transaction got updated " + transactionObjects.size());
    //                adapter.replaceList(transactionObjects, viewModel.getTokens().getValue());
    //              }
    //            });
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    DigitalCashViewModel viewModel = DigitalCashActivity.obtainViewModel(requireActivity());
    viewModel.setPageTitle(R.string.digital_cash_history);
  }
}

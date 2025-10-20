package com.tomtomkenya.africanludo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.activity.DepositActivity;
import com.tomtomkenya.africanludo.activity.OfflinePayActivity;
import com.tomtomkenya.africanludo.activity.WithdrawActivity;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.ludouikit.components.LudoButton;

/**
 * WalletFragment surfaces balance information and shortcuts into deposit/withdraw flows.
 */
public class WalletFragment extends Fragment {

    private TextView totalBalanceText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        totalBalanceText = view.findViewById(R.id.textTotalBalance);
        LudoButton depositButton = view.findViewById(R.id.buttonDeposit);
        LudoButton withdrawButton = view.findViewById(R.id.buttonWithdraw);
        LudoButton offlineButton = view.findViewById(R.id.buttonOffline);

        Preferences preferences = Preferences.getInstance(requireContext());
        String depositBalance = preferences.getString(Preferences.KEY_DEPOSIT_BAL);
        String wonBalance = preferences.getString(Preferences.KEY_WON_BAL);
        String totalBalance = sumBalances(depositBalance, wonBalance);
        totalBalanceText.setText(getString(R.string.currency_format, totalBalance));

        depositButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), DepositActivity.class)));
        withdrawButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), WithdrawActivity.class)));
        offlineButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), OfflinePayActivity.class)));
    }

    private String sumBalances(String first, String second) {
        double deposit = TextUtils.isEmpty(first) ? 0 : parseDouble(first);
        double won = TextUtils.isEmpty(second) ? 0 : parseDouble(second);
        return String.format("%.2f", deposit + won);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

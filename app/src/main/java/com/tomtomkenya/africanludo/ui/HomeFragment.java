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
import androidx.navigation.Navigation;

import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.activity.DepositActivity;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.ludouikit.components.LudoButton;

/**
 * Modernised home surface showing wallet snapshot and quick access actions.
 */
public class HomeFragment extends Fragment {

    private TextView welcomeTitle;
    private TextView walletBalance;
    private TextView bonusBalance;
    private LudoButton quickPlayButton;
    private LudoButton quickDepositButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        welcomeTitle = view.findViewById(R.id.textWelcome);
        walletBalance = view.findViewById(R.id.textWalletBalance);
        bonusBalance = view.findViewById(R.id.textBonusBalance);
        quickPlayButton = view.findViewById(R.id.buttonQuickPlay);
        quickDepositButton = view.findViewById(R.id.buttonQuickDeposit);

        Preferences preferences = Preferences.getInstance(requireContext());
        String name = preferences.getString(Preferences.KEY_FULL_NAME);
        if (TextUtils.isEmpty(name)) {
            name = preferences.getString(Preferences.KEY_USERNAME);
        }
        if (!TextUtils.isEmpty(name)) {
            welcomeTitle.setText(getString(R.string.welcome_back_format, name));
        }

        walletBalance.setText(formatCurrency(preferences.getString(Preferences.KEY_DEPOSIT_BAL)));
        bonusBalance.setText(formatCurrency(preferences.getString(Preferences.KEY_BONUS_BAL)));

        quickPlayButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("click_action", "MainActivity");
            Navigation.findNavController(v).navigate(R.id.playFragment, args);
        });
        quickDepositButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), DepositActivity.class)));
    }

    private String formatCurrency(String balance) {
        if (TextUtils.isEmpty(balance)) {
            return getString(R.string.currency_format, "0");
        }
        return getString(R.string.currency_format, balance);
    }
}

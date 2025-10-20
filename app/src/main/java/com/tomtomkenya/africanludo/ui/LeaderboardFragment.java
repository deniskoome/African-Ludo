package com.tomtomkenya.africanludo.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.activity.LeaderBoardActivity;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.ludouikit.components.LudoButton;

/**
 * LeaderboardFragment connects the modern navigation shell to the legacy leaderboard experiences.
 */
public class LeaderboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LudoButton viewLeaderboard = view.findViewById(R.id.buttonLeaderboard);
        viewLeaderboard.setOnClickListener(v -> {
            try {
                startActivity(new Intent(requireContext(), LeaderBoardActivity.class));
            } catch (Exception e) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(requireContext(), Uri.parse(AppConstant.HOW_TO_PLAY));
            }
        });
    }
}

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
import com.tomtomkenya.africanludo.activity.LoginActivity;
import com.tomtomkenya.africanludo.activity.ProfileActivity;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.ludouikit.components.LudoButton;
import com.tomtomkenya.ludouikit.components.LudoDialog;

/**
 * Profile surface centralising identity and sign-out controls.
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView name = view.findViewById(R.id.textProfileName);
        TextView email = view.findViewById(R.id.textProfileEmail);
        TextView phone = view.findViewById(R.id.textProfilePhone);
        LudoButton editButton = view.findViewById(R.id.buttonEditProfile);
        LudoButton logoutButton = view.findViewById(R.id.buttonLogout);

        Preferences preferences = Preferences.getInstance(requireContext());
        String displayName = preferences.getString(Preferences.KEY_FULL_NAME);
        if (TextUtils.isEmpty(displayName)) {
            displayName = preferences.getString(Preferences.KEY_USERNAME);
        }
        if (!TextUtils.isEmpty(displayName)) {
            name.setText(displayName);
        }
        email.setText(preferences.getString(Preferences.KEY_EMAIL));
        String phoneNumber = preferences.getString(Preferences.KEY_COUNTRY_CODE) + " " + preferences.getString(Preferences.KEY_MOBILE);
        phone.setText(phoneNumber.trim());

        editButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileActivity.class)));
        logoutButton.setOnClickListener(v -> LudoDialog.build(
                requireContext(),
                getString(R.string.logout_title),
                getString(R.string.logout_message),
                () -> {
                    preferences.setlogout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                },
                null
        ).show());
    }
}

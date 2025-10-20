package com.tomtomkenya.africanludo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tomtomkenya.africanludo.R;

/**
 * Hosts the match tabs inside a modern MotionLayout with celebratory dice animation feedback.
 */
public class MatchFragment extends Fragment {

    private static final long CELEBRATION_DURATION_MS = 3200L;

    private MotionLayout motionLayout;
    private LottieAnimationView celebrationLottie;
    private Runnable hideCelebrationRunnable;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private final int[] tabTitleResIds = new int[]{
            R.string.match_tab_upcoming,
            R.string.match_tab_ongoing,
            R.string.match_tab_completed
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_match, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        motionLayout = view.findViewById(R.id.matchMotionLayout);
        celebrationLottie = view.findViewById(R.id.matchCelebrationLottie);
        MaterialCardView celebrationCard = view.findViewById(R.id.matchCelebrationCard);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new MatchPagerAdapter(this));
        viewPager.setOffscreenPageLimit(tabTitleResIds.length);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(getString(tabTitleResIds[position]))
        ).attach();

        celebrationCard.setOnClickListener(v -> {
            if (motionLayout != null) {
                motionLayout.transitionToStart();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String clickAction = bundle.getString("click_action", "default");
            if ("MainActivity".equals(clickAction)) {
                viewPager.setCurrentItem(1, false);
                triggerCelebration();
            } else {
                viewPager.setCurrentItem(0, false);
            }
        } else if (savedInstanceState == null) {
            triggerCelebration();
        }
    }

    /**
     * Animates the celebration card into view and plays the dice roll feedback.
     */
    private void triggerCelebration() {
        if (motionLayout == null) {
            return;
        }
        motionLayout.post(() -> {
            if (motionLayout == null) {
                return;
            }
            motionLayout.transitionToEnd();
            if (celebrationLottie != null) {
                celebrationLottie.cancelAnimation();
                celebrationLottie.setProgress(0f);
                celebrationLottie.playAnimation();
            }
            scheduleHideCelebration();
        });
    }

    /**
     * Schedules the celebration card to gracefully retreat after a short delay.
     */
    private void scheduleHideCelebration() {
        if (motionLayout == null) {
            return;
        }
        if (hideCelebrationRunnable != null) {
            motionLayout.removeCallbacks(hideCelebrationRunnable);
        }
        hideCelebrationRunnable = () -> {
            if (motionLayout != null) {
                motionLayout.transitionToStart();
            }
        };
        motionLayout.postDelayed(hideCelebrationRunnable, CELEBRATION_DURATION_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (motionLayout != null && hideCelebrationRunnable != null) {
            motionLayout.removeCallbacks(hideCelebrationRunnable);
        }
        hideCelebrationRunnable = null;
        celebrationLottie = null;
        motionLayout = null;
        tabLayout = null;
        viewPager = null;
    }

    /**
     * Lightweight ViewPager2 adapter bridging the legacy match fragments.
     */
    private static class MatchPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        MatchPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new UpcomingFragment();
                case 1:
                    return new OngoingFragment();
                default:
                    return new CompletedFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

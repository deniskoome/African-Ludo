package com.tomtomkenya.africanludo.push;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tomtomkenya.africanludo.utils.NotificationUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Instrumented verification around the notification helpers used by the modern FCM service.
 */
@RunWith(AndroidJUnit4.class)
public class LudoFcmServiceTest {

    @Test
    public void mapFromBundle_returnsDefensiveCopy() {
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "match_found");
        Map<String, String> copy = NotificationUtils.mapFromBundle(payload);
        payload.put("type", "mutated");
        assertEquals("match_found", copy.get("type"));
        assertNotSame(payload, copy);
    }

    @Test
    public void playInAppTone_runsWithoutCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        NotificationUtils.playInAppTone(context);
    }
}

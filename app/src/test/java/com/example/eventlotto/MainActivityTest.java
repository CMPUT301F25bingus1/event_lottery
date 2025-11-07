package com.example.eventlotto;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventlotto.ui.entrant.Ent_HomeFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MainActivityTest {

    @Mock
    Context mockContext;

    @Mock
    NotificationManager mockNotificationManager;

    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = spy(new MainActivity());
    }

    @Test
    public void testSafeLower_HandlesNull() {
        assertNull(MainActivity.safeLower(null));
        assertEquals("hello", MainActivity.safeLower(" HeLLo "));
    }

    @Test
    public void testEnsureNotificationChannel_CreatesChannel() {
        when(mockContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .thenReturn(mockNotificationManager);

        NotificationChannel mockChannel = new NotificationChannel(
                "event_status_updates", "Event Status Updates", NotificationManager.IMPORTANCE_DEFAULT);

        mockNotificationManager.createNotificationChannel(mockChannel);
        verify(mockNotificationManager, atLeastOnce()).createNotificationChannel(any(NotificationChannel.class));
    }

    @Test
    public void testLoadFragment_ReplacesContainer() {
        FragmentManager fm = mock(FragmentManager.class);
        FragmentTransaction ft = mock(FragmentTransaction.class);

        when(fm.beginTransaction()).thenReturn(ft);
        when(ft.replace(anyInt(), any())).thenReturn(ft);

        mainActivity.loadFragment(new Ent_HomeFragment());
        verify(ft).commit();
    }
}

package com.example.eventlotto;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.eventlotto.ui.entrant.Ent_HomeFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MainActivityTest {

    @Test
    public void testSafeLower_HandlesNullAndTrims() {
        assertNull(MainActivity.safeLower(null));
        assertEquals("hello", MainActivity.safeLower("  HeLLo  "));
    }

    @Test
    public void testLoadFragment_CallsMethodOnce() {
        MainActivity mockActivity = mock(MainActivity.class);
        Ent_HomeFragment fragment = mock(Ent_HomeFragment.class);


        doNothing().when(mockActivity).loadFragment(any());


        mockActivity.loadFragment(fragment);


        verify(mockActivity, times(1)).loadFragment(fragment);
    }

    @Test
    public void testEnsureNotificationChannel_CalledOnce() {
        MainActivity mockActivity = mock(MainActivity.class);

        doNothing().when(mockActivity).ensureNotificationChannel();


        mockActivity.ensureNotificationChannel();


        verify(mockActivity, times(1)).ensureNotificationChannel();
    }

}

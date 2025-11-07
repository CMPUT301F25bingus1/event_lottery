package com.example.eventlotto;

import com.example.eventlotto.model.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void userConstructor_setsFieldsCorrectly() {
        User user = new User(
                "device123",
                "Alice Johnson",
                "alice@example.com",
                "6045551234"
        );

        assertEquals("device123", user.getUid());      // UID set from deviceId in your class
        assertEquals("Alice Johnson", user.getFullName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("6045551234", user.getPhone());
        // your class also defaults role to "entrant"
        assertEquals("entrant", user.getRole());
    }

    @Test
    public void userSetters_updateValues() {
        User user = new User();
        user.setFullName("Bob Lee");
        user.setEmail("bob@example.com");
        user.setPhone("7785559999");

        assertEquals("Bob Lee", user.getFullName());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("7785559999", user.getPhone());
    }
}

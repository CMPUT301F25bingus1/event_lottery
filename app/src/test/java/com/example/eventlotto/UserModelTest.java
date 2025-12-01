package com.example.eventlotto;

import com.example.eventlotto.model.User;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for User model class
 * Tests cover user profile management and authentication
 */
public class UserModelTest {

    private String testDeviceId;

    @Before
    public void setUp() {
        testDeviceId = "device_12345";
    }

    /**
     * US 01.02.01: Test user can provide personal information
     */
    @Test
    public void testUserCreationWithPersonalInfo() {
        String fullName = "John Doe";
        String email = "john@example.com";
        String phone = "+1234567890";

        User user = new User(testDeviceId, fullName, email, phone);

        assertEquals(fullName, user.getFullName());
        assertEquals(email, user.getEmail());
        assertEquals(phone, user.getPhone());
        assertEquals(testDeviceId, user.getUid());
        assertEquals(testDeviceId, user.getDeviceId());
        assertEquals("entrant", user.getRole());
    }

    /**
     * US 01.02.01: Test user with optional phone number
     */
    @Test
    public void testUserCreationWithoutPhone() {
        User user = new User(testDeviceId, "Jane Doe", "jane@example.com", null);

        assertEquals("Jane Doe", user.getFullName());
        assertEquals("jane@example.com", user.getEmail());
        assertNull(user.getPhone());
    }

    /**
     * US 01.02.02: Test user can update profile information
     */
    @Test
    public void testUserUpdateProfile() throws InterruptedException {
        User user = new User(testDeviceId, "Old Name", "old@example.com", "123456");
        Timestamp originalCreatedAt = user.getCreatedAt();
        Timestamp originalUpdatedAt = user.getUpdatedAt();

        Thread.sleep(1010);

        // Update profile
        user.setFullName("New Name");
        user.setEmail("new@example.com");
        user.setPhone("+9876543210");
        user.touch();

        assertEquals("New Name", user.getFullName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("+9876543210", user.getPhone());
        assertEquals(originalCreatedAt, user.getCreatedAt());
        assertTrue(user.getUpdatedAt().getSeconds() > originalUpdatedAt.getSeconds());
    }

    /**
     * US 01.02.02: Test touch() method updates timestamp
     */
    @Test
    public void testUserTouchUpdatesTimestamp() {
        User user = new User(testDeviceId, "Test", "test@example.com", null);
        Timestamp originalUpdatedAt = user.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        user.touch();
        assertTrue(user.getUpdatedAt().getSeconds() >= originalUpdatedAt.getSeconds());
    }

    /**
     * US 01.04.03: Test user notification preference
     */
    @Test
    public void testUserNotificationPreference() {
        User user = new User(testDeviceId, "Test", "test@example.com", null);

        // Default should be true (can receive notifications)
        assertTrue(user.getNotifyWhenNotSelected());

        // User opts out
        user.setNotifyWhenNotSelected(false);
        assertFalse(user.getNotifyWhenNotSelected());

        // User opts back in
        user.setNotifyWhenNotSelected(true);
        assertTrue(user.getNotifyWhenNotSelected());
    }

    /**
     * US 01.07.01: Test device-based identification
     */
    @Test
    public void testDeviceIdentification() {
        String deviceId1 = "device_abc123";
        String deviceId2 = "device_xyz789";

        User user1 = new User(deviceId1, "User 1", "user1@example.com", null);
        User user2 = new User(deviceId2, "User 2", "user2@example.com", null);

        assertEquals(deviceId1, user1.getDeviceId());
        assertEquals(deviceId2, user2.getDeviceId());
        assertEquals(user1.getUid(), user1.getDeviceId());
        assertEquals(user2.getUid(), user2.getDeviceId());
        assertNotEquals(user1.getDeviceId(), user2.getDeviceId());
    }

    /**
     * US 01.02.04, 03.02.01: Test user profile deletion
     */
    @Test
    public void testUserProfileDeletion() {
        User user = new User(testDeviceId, "John Doe", "john@example.com", "+123456");

        assertNull(user.getDeletedAt());

        // Mark user as deleted
        user.setDeletedAt(Timestamp.now());

        assertNotNull(user.getDeletedAt());
        // User data should still be retrievable
        assertEquals("John Doe", user.getFullName());
        assertEquals("john@example.com", user.getEmail());
    }

    /**
     * Test user avatar URL management
     */
    @Test
    public void testUserAvatarManagement() {
        User user = new User(testDeviceId, "Test", "test@example.com", null);
        assertNull(user.getAvatarUrl());

        String avatarUrl = "https://example.com/avatars/user123.jpg";
        user.setAvatarUrl(avatarUrl);
        assertEquals(avatarUrl, user.getAvatarUrl());
    }

    /**
     * US 02.02.03: Test user geo-location consent
     */
    @Test
    public void testGeoLocationConsent() {
        User user = new User(testDeviceId, "Test", "test@example.com", null);

        // Default should be false (no consent)
        assertFalse(user.getGeoConsent());

        // User grants geo consent
        user.setGeoConsent(true);
        assertTrue(user.getGeoConsent());

        // User revokes geo consent
        user.setGeoConsent(false);
        assertFalse(user.getGeoConsent());
    }

    /**
     * Test user role assignment
     */
    @Test
    public void testUserRoleAssignment() {
        User entrant = new User(testDeviceId, "John", "john@example.com", null);
        assertEquals("entrant", entrant.getRole());

        User organizer = new User(testDeviceId, "Jane", "jane@example.com", null);
        organizer.setRole("organizer");
        assertEquals("organizer", organizer.getRole());

        User admin = new User(testDeviceId, "Admin", "admin@example.com", null);
        admin.setRole("admin");
        assertEquals("admin", admin.getRole());
    }
}
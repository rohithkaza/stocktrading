package com.stockapp.stock_backend.model;

import org.junit.jupiter.api.*;

public class UserTest {

    @BeforeEach
    void createUserTest() {
        User.createUser("testcase-user", "testcase-user", "testcaseuser@gmail.com");
    }

    @AfterEach
    void deleteUserTest() {
        User.deleteUser("testcase-user");
    }

    @Test
    void checkEmptyPortfolioTest() {
        Assertions.assertEquals(0, User.calcPortfolioVal("testcase-user"));
    }

    @Test
    void checkUserExistsTest() {
        Assertions.assertTrue(User.userExists("testcase-user"));
    }

    @Test
    void resetUserTest() {
        Assertions.assertDoesNotThrow(() -> {
            User.resetUser("testcase-user");
        });
    }

    @Test
    void getUserNameTest() {
        Assertions.assertEquals("testcase-user", User.getUsername("testcase-user"));
    }

    @Test
    void getEmailTest() {
        Assertions.assertEquals("testcaseuser@gmail.com", User.getEmail("testcase-user"));
    }

    @Test
    void checkEmptyPortfolioTest2() {
        Assertions.assertEquals(0, User.getPortfolioValue("testcase-user"));
    }

    @Test
    void checkEmptyUninvested() {
        Assertions.assertEquals(100000, User.getUninvested("testcase-user"));
    }

    @Test
    void updateUninvestedValue() {
        User.setUninvested("testcase-user", 1);
        Assertions.assertEquals(1, User.getUninvested("testcase-user"));
    }

    @Test
    void checkEmptyInvested() {
        Assertions.assertEquals(0, User.getInvested("testcase-user"));
    }

    @Test
    void updateInvestedValue() {
        User.setInvested("testcase-user", 1);
        Assertions.assertEquals(1, User.getInvested("testcase-user"));
    }
}

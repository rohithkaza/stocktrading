package com.stockapp.stock_backend.model;

import org.junit.jupiter.api.*;

public class BigtableManagerTest {

    static BigtableManager btm;

    @BeforeAll
    static void makeBtm() {
        Assertions.assertDoesNotThrow(() -> {
            btm = new BigtableManager("rice-comp-539-spring-2022", "comp-539-bigtable");
        });
        btm.createUser("testcase-user", "testcase-user", "testcaseuser@gmail.com");
    }

    @AfterAll
    static void closeBtm() {
        btm.deleteUser("testcase-user");
        btm.close();
    }

    @Test
    void userDoesNotExistTest() {
        Assertions.assertFalse(btm.userExists("testcase-user2"));
    }

    @Test
    void userExistsTest() {
        Assertions.assertTrue(btm.userExists("testcase-user"));
    }

    @Test
    void getUsernameTest() {
        Assertions.assertEquals("testcase-user", btm.getUsername("testcase-user"));
    }

    @Test
    void getEmailTest() {
        Assertions.assertEquals("testcaseuser@gmail.com", btm.getEmail("testcase-user"));
    }

    @Test
    void getEmptyPortfolioValueTest() {
        Assertions.assertEquals(0, btm.getPortfolioValue("testcase-user"));
    }

    @Test
    void updatePortfolioValueTest() {
        btm.updatePortfolioValue("testcase-user", 1);
        Assertions.assertEquals(1, btm.getPortfolioValue("testcase-user"));
    }

    @Test
    void checkEmptyUninvested() {
        Assertions.assertEquals(100000, btm.getUnivestedValue("testcase-user"));
    }

    @Test
    void updateUninvestedValue() {
        User.setUninvested("testcase-user", 1);
        Assertions.assertEquals(1, btm.getUnivestedValue("testcase-user"));
    }

    @Test
    void checkEmptyInvested() {
        Assertions.assertEquals(0, btm.getIvestedValue("testcase-user"));
    }

    @Test
    void updateInvestedValue() {
        btm.updateInvestedValue("testcase-user", 1);
        Assertions.assertEquals(1, btm.getIvestedValue("testcase-user"));
    }

}

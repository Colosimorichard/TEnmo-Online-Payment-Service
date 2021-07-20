package com.techelevator.tenmo;

import com.techelevator.tenmo.model.Authority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorityTest {
    Authority authority = new Authority("Hello");

    @Test
    public void getName_equal_to_5() {
        authority.setName("Hello");
        assertEquals(authority.getName(), "Hello");
    }
}

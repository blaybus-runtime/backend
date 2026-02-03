package com.blaybus.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenTest {
    @Test
    void gen() {
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
    }
}

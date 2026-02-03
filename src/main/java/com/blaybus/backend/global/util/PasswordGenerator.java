package com.blaybus.backend.global.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final SecureRandom random = new SecureRandom();

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SYMBOL = "!@#$%^&*()-_=+[]{}";

    public static String generate(int length) {
        if (length < 12) throw new IllegalArgumentException("temp password length should be >= 12");

        List<Character> chars = new ArrayList<>();
        chars.add(pick(UPPER));
        chars.add(pick(LOWER));
        chars.add(pick(DIGIT));
        chars.add(pick(SYMBOL));

        String all = UPPER + LOWER + DIGIT + SYMBOL;
        while (chars.size() < length) chars.add(pick(all));

        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private static char pick(String s) {
        return s.charAt(random.nextInt(s.length()));
    }
}

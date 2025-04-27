package com.university.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHash {
    public static void main(String[] args) {
        String hashed = BCrypt.hashpw("pass123", BCrypt.gensalt());
        System.out.println(hashed);
    }
}
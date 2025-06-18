package com.example.promotion_service.utils;

import org.apache.logging.log4j.util.StringBuilders;

import java.security.SecureRandom;
import java.util.Random;

public class VoucherCodeUtils {
    public static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int defaultLength = 10;
    public static String generateVoucherCode(){
        SecureRandom random = new SecureRandom();
        StringBuilder voucherCode = new StringBuilder();
        for (int i = 0; i < defaultLength; i++)
        {
            int index = random.nextInt(10);
            voucherCode.append(characters.charAt(index));
        }
        return voucherCode.toString();
    }
}

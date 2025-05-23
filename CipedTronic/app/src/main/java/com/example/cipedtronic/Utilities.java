package com.example.cipedtronic;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utilities {

    public static String bytesToHex(byte[] data) {
        StringBuilder response = new StringBuilder();
        for (byte b : data) {
            response.append(String.format("%02X", b)).append(" ");
        }
        return response.toString().trim();
    }

    public static String bytesToString(byte[] data) {
        StringBuilder response = new StringBuilder();
        for (byte b : data) {
            response.append(String.format("%c", b)).append("");
        }
        return response.toString().trim();
    }
    public static String getStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        PrintWriter writer = new PrintWriter(errors);
        e.printStackTrace(writer);
        return errors.toString().replaceAll("\t", "").replaceAll("\n", " | ").replaceAll("\r", "").trim();
    }
}

package com.example.android.proximitymanager;


import android.content.Context;
import android.widget.Toast;

public class Utils {

    /**
     * Convert section of a byte[] into a hexadecimal string
     */
    public static String toHexString(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();

        for (int i=offset; i < length; i++) {
            sb.append(String.format("%02x", data[i] & 0xFF));
        }

        return sb.toString();
    }

    /**
     * Show a Toast message
     */
    public static void showToast(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

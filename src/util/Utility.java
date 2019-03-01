package util;

import java.util.HashSet;

public abstract class Utility {

    public static String charMultiply(final char c, final int times) {
        char[] arr = new char[times];
        for (int i = 0; i < times; i++) arr[i] = c;
        return new String(arr);
    }

    public static boolean arrayContains(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return true;
        return false;
    }
}

package util;

import java.util.HashSet;

public class TypeChecker {

    private final static String DIGITS = "0123456789";
    private final static String IDENTIFIER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVEXYZ_";
    private final static String ARITHMETIC = "+-*/%";

    private HashSet<TypeTuple> concatenateSet = new HashSet<>();

    public TypeChecker() {
        init();
    }

    private void init() {
        for (int i: new int[]{0, 1, 8, 9, 10, 11, 14}) {
            concatenateSet.add(new TypeTuple(i, i));
        }
        concatenateSet.add(new TypeTuple(0, 12));
        concatenateSet.add(new TypeTuple(1, 0));
        concatenateSet.add(new TypeTuple(8, 9));
        concatenateSet.add(new TypeTuple(9, 8));
        concatenateSet.add(new TypeTuple(10, 9));
        concatenateSet.add(new TypeTuple(11, 9));
        concatenateSet.add(new TypeTuple(12, 0));
        concatenateSet.add(new TypeTuple(15, 9));
        concatenateSet.add(new TypeTuple(16, 9));
        concatenateSet.add(new TypeTuple(17, 9));
    }

    private static int charType(char c) {
        if (containsChar(DIGITS, c)) return 0;
        else if (containsChar(IDENTIFIER, c)) return 1;
        else if (containsChar(ARITHMETIC, c)) return 17;
        else {
            int res;
            switch (c) {
                case '{':
                    res = 2;
                    break;
                case '}':
                    res = 3;
                    break;
                case '(':
                    res = 4;
                    break;
                case ')':
                    res = 5;
                    break;
                case ';':
                    res = 6;
                    break;
                case '\n':
                    res = 7;
                    break;
                case '<':
                    res = 8;
                    break;
                case '>':
                    res = 8;
                    break;
                case '=':
                    res = 9;
                    break;
                case '&':
                    res = 10;
                    break;
                case '|':
                    res = 11;
                    break;
                case '.':
                    res = 12;
                    break;
                case ',':
                    res = 13;
                    break;
                case '!':
                    res = 15;
                    break;
                case '^':
                    res = 16;
                    break;
                case '@':
                    res = 18;
                    break;
                case ':':
                    res = 19;
                    break;
                default:
                    res = -1;
                    break;
            }
            return res;
        }
    }

    public boolean concatenateAble(char lastChar, char thisChar) {
        int lastType = charType(lastChar);
        int thisType = charType(thisChar);
        TypeTuple tt = new TypeTuple(lastType, thisType);
        return concatenateSet.contains(tt);
    }

    private static boolean containsChar(final String s, final char c) {
        for (char x : s.toCharArray())
            if (x == c) return true;
        return false;
    }

    public static boolean isIdentifier(String s) {
        if (s.length() == 0) return false;
        else {
            if (!containsChar(IDENTIFIER, s.charAt(0))) return false;
            else {
                for (int i = 1; i < s.length(); i++) {
                    char ch = s.charAt(i);
                    if (!(containsChar(IDENTIFIER, ch) || containsChar(DIGITS, ch))) return false;
                }
                return true;
            }
        }
    }

    public static boolean isDigits(String s) {
        for (char c: s.toCharArray()) {
            if (!containsChar(DIGITS, c)) return false;
        }
        return s.length() > 0;  // Empty string is not digits.
    }

    public static boolean isFloat(String s) {
        int dotIndex = s.indexOf('.');
        if (dotIndex >= 0) {
            String front = s.substring(0, dotIndex);
            String back = s.substring(dotIndex + 1);
            if (dotIndex == 0) {
                front = "0";
            }
            return isDigits(front) && isDigits(back);
        } else {
            return false;
        }
    }
}

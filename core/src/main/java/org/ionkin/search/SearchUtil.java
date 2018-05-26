package org.ionkin.search;

public class SearchUtil {

    public static int hashCode(byte[] bytes) {
        int state = 0;
        for (int i = 0; i < bytes.length; i++) {
            state += bytes[i];
            for (int j = 0; j < 4; j++) {
                state *= 0x7C824F73;
                state ^= 0x5C12FE83;
                state = Integer.rotateLeft(state, 5);
            }
        }
        return state;
    }
}

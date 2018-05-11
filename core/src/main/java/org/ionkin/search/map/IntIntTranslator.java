package org.ionkin.search.map;

import org.ionkin.search.VariableByte;

import java.io.Serializable;

public class IntIntTranslator extends IntTranslator<Integer> implements Serializable {

    private static final long serialVersionUID = 2711367714314452904L;

    @Override
    public byte[] serialize(Integer key, Integer value) {
        return VariableByte.compress(new int[] {key, value});
    }

    @Override
    public Integer deserializeValue(byte[] packed) {
        int[] ar = VariableByte.uncompress(packed);
        return ar[1];
    }
}

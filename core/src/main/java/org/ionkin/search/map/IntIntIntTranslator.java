package org.ionkin.search.map;

import org.ionkin.search.model.IntIntPair;
import org.ionkin.search.IntWrapper;
import org.ionkin.search.VariableByte;

import java.io.Serializable;

public class IntIntIntTranslator extends IntTranslator<IntIntPair> implements Serializable {

    @Override
    public byte[] serialize(Integer key, IntIntPair pair) {
        return VariableByte.compress(new int[] {key, pair.first(), pair.second()});
    }

    @Override
    public IntIntPair deserializeValue(byte[] packed) {
        int[] ar = VariableByte.uncompress(packed, new IntWrapper(), 3);
        return new IntIntPair(ar[1], ar[2]);
    }
}

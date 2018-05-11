package org.ionkin.search.set;

import org.ionkin.search.LightString;

import java.io.Serializable;

public class StringTranslator implements CompactSetTranslator<LightString>, Serializable {

    private static final long serialVersionUID = 8148651780798167604L;

    @Override
    public boolean isInstance(Object obj) {
        return obj instanceof LightString;
    }

    @Override
    public int getHash(LightString string) {
        return string.hashCode();
    }

    @Override
    public byte[] serialize(LightString string) {
        return string.getBytes();
    }

    @Override
    public LightString deserialize(byte[] packed) {
        return new LightString(packed);
    }
}

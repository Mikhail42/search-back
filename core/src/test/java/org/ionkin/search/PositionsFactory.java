package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PositionsFactory {

    @Test
    public void create() {
        IntBytesMap ibm = new IntBytesMap();
        byte[] ar1 = new byte[]{1, 3, 100, 13, 50, 2, 24};
        byte[] ar2 = new byte[]{1, 3, 100, 21};
        ibm.put(4, new BytesRange(ar1));
        ibm.put(5, new BytesRange(ar2));
        Positions pos = new Positions(ibm);
        assertArrayEquals(pos.positions(4).getCopy(), ar1);
        assertArrayEquals(pos.positions(5).getCopy(), ar2);
    }


    @Test
    public void create4() {
        IntBytesMap intBytesMap = new IntBytesMap();
        int[] poss1 = new int[]{2, 10};
        int[] poss2 = new int[]{2, 10};

        intBytesMap.put(15, new BytesRange(Compressor.compressVbWithMemory(poss1)));
        intBytesMap.put(25, new BytesRange(Compressor.compressVbWithMemory(poss2)));

        Positions pos = new Positions(intBytesMap);
        assertArrayEquals(Compressor.decompressVb(pos.positions(15)), poss1);
        assertArrayEquals(Compressor.decompressVb(pos.positions(25)), poss2);
    }

    @Test
    public void create2() {
        IntBytesMap ibm = new IntBytesMap();
        byte[] ar1 = Compressor.compressVbWithMemory(new int[] {1});
        byte[] ar6 = Compressor.compressVbWithMemory(new int[] {1, 3});
        ibm.put(4, new BytesRange(ar1));
        ibm.put(5, new BytesRange(ar1));
        ibm.put(50, new BytesRange(ar1));
        ibm.put(105, new BytesRange(ar1));
        ibm.put(205, new BytesRange(ar1));
        ibm.put(500, new BytesRange(ar6));

        Positions pos = new Positions(ibm);
        assertArrayEquals(pos.positions(4).getCopy(), ar1);
        assertArrayEquals(pos.positions(5).getCopy(), ar1);
        assertArrayEquals(pos.positions(50).getCopy(), ar1);
        assertArrayEquals(pos.positions(105).getCopy(), ar1);
        assertArrayEquals(pos.positions(205).getCopy(), ar1);
        assertArrayEquals(pos.positions(500).getCopy(), ar6);
    }

    @Test
    public void create3() {
        IntBytesMap ibm = new IntBytesMap();
        byte[] ar1 = fromWithoutDiff(1, 3, 100, 13, 50, 2, 24, 1, 3, 100, 13, 50, 2, 24);
        byte[] ar2 = fromWithoutDiff(1, 3, 100, 21, 1, 3, 100, 21);
        byte[] ar3 = fromWithoutDiff(1, 3, 100, 13, 50, 2, 24, 1, 3, 100, 13, 50, 2, 24);
        byte[] ar4 = fromWithoutDiff(1, 3, 100, 21, 1, 3, 100, 21);
        byte[] ar5 = fromWithoutDiff(1, 3, 100, 13, 50, 2, 24, 1, 3, 100, 13, 50, 2, 24);
        byte[] ar6 = fromWithoutDiff(1, 3, 100, 21, 1, 3, 100, 21);
        ibm.put(4, new BytesRange(ar1));
        ibm.put(5, new BytesRange(ar2));
        ibm.put(50, new BytesRange(ar3));
        ibm.put(105, new BytesRange(ar4));
        ibm.put(205, new BytesRange(ar5));
        ibm.put(500, new BytesRange(ar6));

        Positions pos = new Positions(ibm);
        assertArrayEquals(pos.positions(4).getCopy(), ar1);
        assertArrayEquals(pos.positions(5).getCopy(), ar2);
        assertArrayEquals(pos.positions(50).getCopy(), ar3);
        assertArrayEquals(pos.positions(105).getCopy(), ar4);
        assertArrayEquals(pos.positions(205).getCopy(), ar5);
        assertArrayEquals(pos.positions(500).getCopy(), ar6);
    }

    @Test
    public void deserialize() {
        ByteArray buf = new ByteArray();

        buf.add(VariableByte.compress(1)); // index length

        buf.add(VariableByte.compress(15)); // index

        int[] positions = new int[]{2, 10};
        byte[] bytes = Compressor.compressVbWithMemory(positions);
        buf.add(VariableByte.compress(bytes.length));
        buf.add(bytes);

        byte[] copy = buf.getCopy();

        Positions pos = Positions.deserialize(copy, 0);
        BytesRange range = pos.positions(15);

        assertArrayEquals(Compressor.decompressVb(range), positions);
    }

    private byte[] fromWithoutDiff(int... ar) {
        return VariableByte.compress(ar);
    }
}

package com.parisoft.supertiler;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.palNum;
import static com.parisoft.supertiler.SuperTiler.priority;
import static com.parisoft.supertiler.SuperTiler.tilesetNum;

class Obj {

    static final byte VERTICAL_MIRROR = (byte) 0x80;
    static final byte HORIZONTAL_MIRROR = 0x7e;
    static final byte SMALL_SIZE = 0;
    static final byte LARGE_SIZE = 2;

    byte x;
    byte y;
    byte tile;
    byte attr;
    byte size;

    Obj(byte x, byte y, byte tile, byte size, byte attr) {
        this.x = x;
        this.y = y;
        this.tile = tile;
        this.size = size;
        this.attr = (byte) (attr | (priority << 5) | (palNum << 3) | tilesetNum);
    }

    void write(FileOutputStream output) throws IOException {
        output.write(new byte[]{x, y, tile, attr, size});
    }
}
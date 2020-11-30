package com.parisoft.supertiler.pojo;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.objPalNum;
import static com.parisoft.supertiler.SuperTiler.objPriority;
import static com.parisoft.supertiler.SuperTiler.objTileOff;
import static com.parisoft.supertiler.SuperTiler.objTilesetNum;

class Obj {

    static final byte VERTICAL_FLIP = (byte) 0x80;
    static final byte HORIZONTAL_FLIP = 0x40;
    static final byte SMALL_SIZE = 0;
    static final byte LARGE_SIZE = 2;

    private byte x;
    private byte y;
    private byte tile;
    private byte attr;
    private byte size;

    Obj(byte x, byte y, int tile, byte size, boolean hFlip, boolean vFlip) {
        if (tile < 0x100) {
            this.tile = (byte) tile;
        } else if (tile < 0x200 && objTilesetNum == 0) {
            this.tile = (byte) (tile - 0x100);
            this.attr = 1;
        } else {
            throw new IndexOutOfBoundsException("Sprite tileset cannot have more than 512 tiles");
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.attr = (byte) ((vFlip ? VERTICAL_FLIP : 0) | (hFlip ? HORIZONTAL_FLIP : 0) | (objPriority << 4) | (objPalNum << 1) | objTilesetNum);
    }

    void write(FileOutputStream output) throws IOException {
        output.write(new byte[]{x, y, (byte) (tile + objTileOff), attr, size});
    }
}
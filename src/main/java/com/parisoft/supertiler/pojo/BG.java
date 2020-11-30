package com.parisoft.supertiler.pojo;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.objPalNum;
import static com.parisoft.supertiler.SuperTiler.objPriority;
import static com.parisoft.supertiler.SuperTiler.objTileOff;

class BG {

    private static final int VERTICAL_FLIP = 0b10000000_00000000;
    private static final int HORIZONTAL_FLIP = 0b01000000_00000000;
    private static final int PRIORITY = 0b00100000_00000000;

    private int tile;
    private byte palette;
    private boolean priority;
    private boolean hFlip;
    private boolean vFlip;

    BG(int tile, boolean hFlip, boolean vFlip) {
        this(tile, objPalNum, objPriority != 0, hFlip, vFlip);
    }

    private BG(int tile, byte palette, boolean priority, boolean hFlip, boolean vFlip) {
        if (tile > 0x400){
            throw new IndexOutOfBoundsException("Background tileset cannot have more than 1000 tiles");
        }

        this.tile = tile;
        this.palette = palette;
        this.priority = priority;
        this.hFlip = hFlip;
        this.vFlip = vFlip;
    }

    void write(FileOutputStream output) throws IOException {
        byte msb = (byte) (((vFlip ? VERTICAL_FLIP : 0) | (hFlip ? HORIZONTAL_FLIP : 0) | (priority ? PRIORITY : 0) | (palette << 10) | (tile + objTileOff)) >> 8);
        byte lsb = (byte) ((tile + objTileOff) & 0xff);
        output.write(new byte[]{lsb, msb});
    }
}

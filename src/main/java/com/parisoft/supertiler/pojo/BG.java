package com.parisoft.supertiler.pojo;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.objTileOff;

public class BG {

    static final int VERTICAL_MIRROR = 0b10000000_00000000;
    static final int HORIZONTAL_MIRROR = 0b01000000_00000000;
    static final int PRIORITY = 0b00100000_00000000;

    private int tile;
    private byte palette;
    private boolean priority;
    private boolean hMirror;
    private boolean vMirror;

    public BG(int tile, byte palette, boolean priority, boolean hMirror, boolean vMirror) {
        this.tile = tile;
        this.palette = palette;
        this.priority = priority;
        this.hMirror = hMirror;
        this.vMirror = vMirror;
    }

    void write(FileOutputStream output) throws IOException {
        byte msb = (byte) (((vMirror ? VERTICAL_MIRROR : 0) | (hMirror ? HORIZONTAL_MIRROR : 0) | (priority ? PRIORITY : 0) | (palette << 10) | tile) >> 8);
        byte lsb = (byte) (tile & 0xff);
        output.write(new byte[]{msb, lsb});
    }
}

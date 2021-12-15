package com.parisoft.supertiler.pojo;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.bpp;
import static com.parisoft.supertiler.SuperTiler.mode;
import static com.parisoft.supertiler.SuperTiler.objPalNum;
import static com.parisoft.supertiler.SuperTiler.objPriority;
import static com.parisoft.supertiler.SuperTiler.objTileOff;
import static com.parisoft.supertiler.SuperTiler.objTilesetNum;
import static com.parisoft.supertiler.SuperTiler.tileSize;

class Obj {

    private static final byte SNES_VERTICAL_FLIP = (byte) 0x80;
    private static final byte SNES_HORIZONTAL_FLIP = 0x40;
    private static final byte GBA_VERTICAL_FLIP = 1 << 5;
    private static final byte GBA_HORIZONTAL_FLIP = 1 << 4;
    static final byte SMALL_SIZE = 0;
    static final byte LARGE_SIZE = 2;

    private byte x;
    private byte y;
    private int tile;
    private byte attr;
    private byte size;

    Obj(byte x, byte y, int tile, byte size, boolean hFlip, boolean vFlip) {
        this.x = x;
        this.y = y;

        switch (mode) {
            case SNES:
                if (tile < 0x100) {
                    this.tile = (byte) tile;
                } else if (tile < 0x200 && objTilesetNum == 0) {
                    this.tile = (byte) (tile - 0x100);
                    this.attr = 1;
                } else {
                    throw new IndexOutOfBoundsException("Sprite tileset cannot have more than 512 tiles");
                }

                this.size = size;
                this.attr = (byte) ((vFlip ? SNES_VERTICAL_FLIP : 0) | (hFlip ? SNES_HORIZONTAL_FLIP : 0) | (objPriority << 4) | (objPalNum << 1) | objTilesetNum);
                break;

            case GBA:
                //@see https://www.coranac.com/tonc/text/regobj.htm
                if (tile >= 0x200) {
                    throw new IndexOutOfBoundsException("Sprite tileset cannot have more than 512 tiles");
                }

                this.tile = tile;
                this.attr = (byte) ((hFlip ? GBA_HORIZONTAL_FLIP : 0) | (vFlip ? GBA_VERTICAL_FLIP : 0) | ((log2(size == SMALL_SIZE ? tileSize.small : tileSize.large) - 1) << 6));
                break;

            case NES:
                //ToDo NES obj
                break;
        }
    }

    void write(FileOutputStream output) throws IOException {
        switch (mode) {
            case SNES:
                output.write(new byte[]{x, y, (byte) (tile + objTileOff), attr, size});
                break;

            case GBA:
                output.write(new byte[]{
                        y, (byte) (bpp == 8 ? 1 << 5 : 0), // attribute 0: Y and Color mode
                        x, attr,//attribute 1: X, Flipping and Size
                        (byte) (tile + objTileOff), (byte) ((objPriority << 2) | (objPalNum << 4)),//attribute 2: Tile ID, Priority and Palette
                        0, 0 // dummy for alignment
                });
                break;
                
            case NES:
                //ToDo NES obj write
                break;
        }
    }

    private static byte log2(byte n) {
        return (byte) (Math.log(n) / Math.log(2));
    }
}
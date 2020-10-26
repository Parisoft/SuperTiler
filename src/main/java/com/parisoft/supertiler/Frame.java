package com.parisoft.supertiler;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.Obj.LARGE_SIZE;
import static com.parisoft.supertiler.Obj.SMALL_SIZE;
import static com.parisoft.supertiler.SuperTiler.applyLarge;
import static com.parisoft.supertiler.SuperTiler.applySmall;
import static com.parisoft.supertiler.SuperTiler.frameHeight;
import static com.parisoft.supertiler.SuperTiler.frameWidth;
import static com.parisoft.supertiler.SuperTiler.objSpSize;
import static com.parisoft.supertiler.SuperTiler.objXOff;
import static com.parisoft.supertiler.SuperTiler.objYOff;

class Frame {

    private List<BigTile> largeTiles = new ArrayList<>();
    private List<BigTile> smallTiles = new ArrayList<>();
    private List<Obj> metasprite = new ArrayList<>();
    private int x, y;

    Frame(Raster img, int x, int y) {
        this.x = x;
        this.y = y;

        if (applyLarge && applySmall) {
            for (y = this.y; y < this.y + frameHeight; y += objSpSize.large) {
                for (x = this.x; x < this.x + frameWidth; x += objSpSize.large) {
                    BigTile largeTile = new BigTile(objSpSize.large, img, x, y);

                    if (largeTile.isEmpty()) {
                        continue;
                    }

                    List<BigTile> smallSubTiles = largeTile.split();

                    if (smallSubTiles.isEmpty()) {
                        largeTiles.add(largeTile);
                    } else {
                        smallTiles.addAll(smallSubTiles);
                    }
                }
            }
        }else {
            byte tilePixels = applyLarge ? objSpSize.large : objSpSize.small;

            for (y = this.y; y < this.y + frameHeight; y += tilePixels) {
                for (x = this.x; x < this.x + frameWidth; x += tilePixels) {
                    BigTile tile = new BigTile(tilePixels, img, x, y);

                    if (tile.isEmpty()) {
                        continue;
                    }

                    if (applyLarge) {
                        largeTiles.add(tile);
                    } else {
                        smallTiles.add(tile);
                    }
                }
            }
        }
    }

    boolean isEmpty() {
        return largeTiles.isEmpty() && smallTiles.isEmpty();
    }

    private void createMetasprites(List<BigTile> tiles, byte size) {
        for (BigTile tile : tiles) {
            byte row = (byte) (tile.y - this.y);
            byte col = (byte) (tile.x - this.x);
            Obj obj = tile.getObj((byte) (objXOff + row), (byte) (objYOff + col), size);
            metasprite.add(obj);
        }
    }

    void createSmallMetasprites() {
        if (applySmall) {
            createMetasprites(smallTiles, SMALL_SIZE);
        }
    }

    void createLargeMetasprites() {
        if (applyLarge) {
            createMetasprites(largeTiles, LARGE_SIZE);
        }
    }

    void write(FileOutputStream output) throws IOException {
        for (Obj obj : metasprite) {
            obj.write(output);
        }

        output.write(0x80);//eof
    }
}


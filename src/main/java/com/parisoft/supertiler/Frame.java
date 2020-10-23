package com.parisoft.supertiler;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.Obj.LARGE_SIZE;
import static com.parisoft.supertiler.Obj.SMALL_SIZE;
import static com.parisoft.supertiler.SuperTiler.frameHeight;
import static com.parisoft.supertiler.SuperTiler.frameWidth;
import static com.parisoft.supertiler.SuperTiler.largeTilePixels;
import static com.parisoft.supertiler.SuperTiler.smallTilePixels;
import static com.parisoft.supertiler.SuperTiler.xOff;
import static com.parisoft.supertiler.SuperTiler.yOff;

class Frame {

    private List<BigTile> largeTiles = new ArrayList<>();
    private List<BigTile> smallTiles = new ArrayList<>();
    private List<Obj> metasprite = new ArrayList<>();
    private int x, y;

    Frame(Raster img, int x, int y) {
        this.x = x;
        this.y = y;

        for (y = this.y; y < this.y + frameHeight; y += largeTilePixels) {
            for (x = this.x; x < this.x + frameWidth; x += largeTilePixels) {
                BigTile largeTile = new BigTile(largeTilePixels, img, x, y);

                if (largeTile.isEmpty()) {
                    continue;
                }

                if (smallTilePixels < largeTilePixels) {
                    List<BigTile> smallSubTiles = largeTile.split();

                    if (smallSubTiles.size() > 0) {
                        smallTiles.addAll(smallSubTiles);
                        continue;
                    }
                }

                largeTiles.add(largeTile);
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
            Obj obj = tile.getObj((byte) (xOff + row), (byte) (yOff + col), size);
            metasprite.add(obj);
        }
    }

    void createSmallMetasprites() {
        createMetasprites(smallTiles, SMALL_SIZE);
    }

    void createLargeMetasprites() {
        createMetasprites(largeTiles, largeTilePixels > smallTilePixels ? LARGE_SIZE : SMALL_SIZE);
    }

    void write(FileOutputStream output) throws IOException {
        for (Obj obj : metasprite) {
            obj.write(output);
        }

        output.write(0x80);//eof
    }
}


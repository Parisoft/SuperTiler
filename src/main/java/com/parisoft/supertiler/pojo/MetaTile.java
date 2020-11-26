package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.pojo.Obj.LARGE_SIZE;
import static com.parisoft.supertiler.pojo.Obj.SMALL_SIZE;
import static com.parisoft.supertiler.SuperTiler.applyLarge;
import static com.parisoft.supertiler.SuperTiler.applySmall;
import static com.parisoft.supertiler.SuperTiler.metatileHeight;
import static com.parisoft.supertiler.SuperTiler.metatileWidth;
import static com.parisoft.supertiler.SuperTiler.objSpSize;
import static com.parisoft.supertiler.SuperTiler.objXOff;
import static com.parisoft.supertiler.SuperTiler.objYOff;

public class MetaTile {

    private List<BigTile> largeTiles = new ArrayList<>();
    private List<BigTile> smallTiles = new ArrayList<>();
    private List<Obj> metatile = new ArrayList<>();
    private int x, y;

    public MetaTile(Raster img, int x, int y) {
        this.x = x;
        this.y = y;

        if (applyLarge && applySmall) {
            for (y = this.y; y < this.y + metatileHeight; y += objSpSize.large) {
                for (x = this.x; x < this.x + metatileWidth; x += objSpSize.large) {
                    BigTile largeTile;

                    try {
                        largeTile = new BigTile(objSpSize.large, img, x, y);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        for (int sy = y; sy < y + objSpSize.large; sy += objSpSize.small) {
                            for (int sx = x; sx < x + objSpSize.large; sx += objSpSize.small) {
                                BigTile smallTile;

                                try {
                                    smallTile = new BigTile(objSpSize.small, img, sx, sy);
                                } catch (ArrayIndexOutOfBoundsException ignore) {
                                    continue;
                                }

                                if (!smallTile.isEmpty()) {
                                    smallTiles.add(smallTile);
                                }
                            }
                        }

                        continue;
                    }

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
        } else {
            byte tilePixels = applyLarge ? objSpSize.large : objSpSize.small;

            for (y = this.y; y < this.y + metatileHeight; y += tilePixels) {
                for (x = this.x; x < this.x + metatileWidth; x += tilePixels) {
                    BigTile tile;

                    try {
                        tile = new BigTile(tilePixels, img, x, y);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }

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

    public boolean isEmpty() {
        return largeTiles.isEmpty() && smallTiles.isEmpty();
    }

    private void createMetatiles(List<BigTile> tiles, byte size) {
        for (BigTile tile : tiles) {
            byte row = (byte) (tile.y - this.y);
            byte col = (byte) (tile.x - this.x);
            Obj obj = tile.getObj((byte) (objXOff + col), (byte) (objYOff + row), size);
            metatile.add(obj);
        }
    }

    public void build() {
        if (applyLarge) {
            createMetatiles(largeTiles, LARGE_SIZE);
        }

        if (applySmall) {
            createMetatiles(smallTiles, SMALL_SIZE);
        }
    }

    public void write(FileOutputStream output) throws IOException {
        for (Obj obj : metatile) {
            obj.write(output);
        }

        output.write(0x80);//eof
    }
}


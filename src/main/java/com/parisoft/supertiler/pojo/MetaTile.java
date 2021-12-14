package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_APPLYLARGE;
import static com.parisoft.supertiler.SuperTiler.ARG_APPLYSMALL;
import static com.parisoft.supertiler.SuperTiler.applyLarge;
import static com.parisoft.supertiler.SuperTiler.applySmall;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.metatileHeight;
import static com.parisoft.supertiler.SuperTiler.metatileWidth;
import static com.parisoft.supertiler.SuperTiler.objXOff;
import static com.parisoft.supertiler.SuperTiler.objYOff;
import static com.parisoft.supertiler.SuperTiler.tileSize;
import static com.parisoft.supertiler.pojo.Obj.LARGE_SIZE;
import static com.parisoft.supertiler.pojo.Obj.SMALL_SIZE;

public class MetaTile {

    private int x, y;
    private List<Obj> metatile = new ArrayList<>();
    List<BigTile> largeTiles = new ArrayList<>();
    List<BigTile> smallTiles = new ArrayList<>();

    public MetaTile(Raster img, int x, int y) {
        this.x = x;
        this.y = y;

        if (metatileWidth == 0) {
            metatileWidth = input.getWidth();
        }

        if (metatileHeight == 0) {
            metatileHeight = input.getHeight();
        }

        if (applyLarge && applySmall) {
            for (y = this.y; y < this.y + metatileHeight; y += tileSize.large) {
                for (x = this.x; x < this.x + metatileWidth; x += tileSize.large) {
                    BigTile largeTile;

                    try {
                        largeTile = new BigTile(tileSize.large, img, x, y);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        for (int sy = y; sy < y + tileSize.large; sy += tileSize.small) {
                            for (int sx = x; sx < x + tileSize.large; sx += tileSize.small) {
                                BigTile smallTile;

                                try {
                                    smallTile = new BigTile(tileSize.small, img, sx, sy);
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
            byte tilePixels;

            if (applyLarge) {
                tilePixels = tileSize.large;
            } else if (applySmall) {
                tilePixels = tileSize.small;
            } else {
                throw new IllegalArgumentException(ARG_APPLYLARGE + " and " + ARG_APPLYSMALL + " are both false");
            }

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

    private void buildMetatiles(List<BigTile> tiles, byte size) {
        for (BigTile tile : tiles) {
            byte row = (byte) (tile.y - this.y);
            byte col = (byte) (tile.x - this.x);
            metatile.add(tile.toObj((byte) (objXOff + col), (byte) (objYOff + row), size));
        }
    }

    public void build() {
        if (applyLarge) {
            buildMetatiles(largeTiles, LARGE_SIZE);
        }

        if (applySmall) {
            buildMetatiles(smallTiles, SMALL_SIZE);
        }
    }

    public void write(FileOutputStream output) throws IOException {
        for (Obj obj : metatile) {
            obj.write(output);
        }

        output.write(0x80);//eof
    }
}


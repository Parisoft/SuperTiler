package com.parisoft.supertiler.pojo;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_TILEMAP;
import static com.parisoft.supertiler.SuperTiler.applyLarge;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.mode;
import static com.parisoft.supertiler.SuperTiler.namespace;
import static java.util.stream.Collectors.toList;

public class TileMap {

    private List<List<BG>> tilemaps;
    private TileSet tileset;

    public TileMap() {
        final int mapWidth = 256;
        final int mapHeight = mode == Mode.NES ? 240 : 256;
        final byte tilePixels = (byte) (applyLarge && mode == Mode.SNES ? 16 : 8);
        final WritableRaster img = input.getRaster();

        tilemaps = new ArrayList<>();
        tileset = new TileSet();

        for (int screenY = 0; screenY < img.getHeight(); screenY += mapHeight) {
            for (int screenX = 0; screenX < img.getWidth(); screenX += mapWidth) {
                List<BigTile> tiles = new ArrayList<>();

                for (int y = screenY; y < screenY + mapHeight; y += tilePixels) {
                    for (int x = screenX; x < screenX + mapWidth; x += tilePixels) {
                        BigTile tile;

                        try {
                            if (x < img.getWidth() && y < img.getHeight()) {
                                tile = new BigTile(tilePixels, img, x, y);
                            } else {
                                tile = new BigTile(tilePixels, img, 0, 0);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            continue;
                        }

                        if (tile.isNull()) {
                            continue;
                        }

                        tiles.add(tile);
                    }
                }

                tileset.addAll(tiles);
                tileset.build();//may not work well for SNES 16x16 tiles
                tilemaps.add(tiles.stream().map(BigTile::toBG).collect(toList()));
            }
        }
    }

    public boolean isEmpty() {
        return tilemaps.isEmpty() || tilemaps.stream().allMatch(List::isEmpty);
    }

    public void write() throws IOException {
        tileset.write();

        String metatile = namespace.getString(ARG_TILEMAP);

        if (metatile != null) {
            if (tilemaps.size() == 1) {
                try (FileOutputStream output = new FileOutputStream(new File(metatile))) {
                    for (BG bg : tilemaps.get(0)) {
                        bg.write(output);
                    }
                }
            } else {
                int dotIdx = metatile.lastIndexOf('.');
                String filename = dotIdx >= 0 ? metatile.substring(0, dotIdx) : metatile;
                String extension = dotIdx >= 0 && dotIdx < metatile.length() ? metatile.substring(dotIdx) : "";

                for (int i = 0; i < tilemaps.size(); i++) {
                    try (FileOutputStream output = new FileOutputStream(new File(filename + i + extension))) {
                        for (BG bg : tilemaps.get(i)) {
                            bg.write(output);
                        }
                    }
                }
            }
        }
    }
}

package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_METATILE;
import static com.parisoft.supertiler.SuperTiler.ARG_TILEMAP;
import static com.parisoft.supertiler.SuperTiler.applyLarge;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.namespace;
import static com.parisoft.supertiler.SuperTiler.objSpSize;
import static java.util.stream.Collectors.toList;

public class TileMap {

    private List<BigTile> tiles = new ArrayList<>();
    private List<BG> tilemap;

    public TileMap() {
        WritableRaster img = input.getRaster();
        byte tilePixels = (byte) (applyLarge ? 16 : 8);

        for (int y = 0; y < img.getHeight(); y += tilePixels) {
            for (int x = 0; x < img.getWidth(); x += tilePixels) {
                BigTile tile;

                try {
                    tile = new BigTile(tilePixels, img, x, y);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }

                if (tile.isNull()) {
                    continue;
                }

                tiles.add(tile);
            }
        }

        tilemap = tiles.stream().map(BigTile::getBG).collect(toList());
    }

    public boolean isEmpty() {
        return tilemap.isEmpty();
    }

    public void write() throws IOException {
        String metatile = namespace.getString(ARG_TILEMAP);

        if (metatile != null) {
            try (FileOutputStream output = new FileOutputStream(new File(metatile))) {
                for (BG bg : tilemap) {
                    bg.write(output);
                }
            }
        }
    }
}

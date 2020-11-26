package com.parisoft.supertiler.util;

import com.parisoft.supertiler.SuperTiler;
import com.parisoft.supertiler.pojo.Tile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.namespace;

public class TileSet {

    public static void write() throws IOException {
        String tilesetPath = namespace.getString(ARG_TILESET);

        if (tilesetPath != null) {
            try (FileOutputStream output = new FileOutputStream(new File(tilesetPath))) {
                for (Tile[] row : SuperTiler.tileset) {
                    for (Tile tile : row) {
                        if (tile == null) {
                            return;
                        }

                        tile.write(output);
                    }
                }
            }
        }
    }
}

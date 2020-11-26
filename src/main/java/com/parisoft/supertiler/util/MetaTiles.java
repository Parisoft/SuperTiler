package com.parisoft.supertiler.util;

import com.parisoft.supertiler.pojo.MetaTile;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_METATILE;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.metatileHeight;
import static com.parisoft.supertiler.SuperTiler.metatileWidth;
import static com.parisoft.supertiler.SuperTiler.namespace;

public class MetaTiles extends ArrayList<MetaTile> {

    private final List<MetaTile> metaTiles;

    public MetaTiles() {
        WritableRaster img = input.getRaster();
        metaTiles = new ArrayList<>();

        for (int y = 0; y < img.getHeight(); y += metatileHeight) {
            for (int x = 0; x < img.getWidth(); x += metatileWidth) {
                MetaTile metaTile = new MetaTile(img, x, y);

                if (metaTile.isEmpty()) {
                    continue;
                }

                metaTiles.add(metaTile);
            }
        }

        metaTiles.forEach(MetaTile::build);
    }

    @Override
    public boolean isEmpty() {
        return metaTiles.isEmpty();
    }

    public void write() throws IOException {
        String metatile = namespace.getString(ARG_METATILE);

        if (metatile != null) {
            for (int i = 0; i < metaTiles.size(); i++) {
                try (FileOutputStream output = new FileOutputStream(new File(metatile + i + ".bin"))) {
                    metaTiles.get(i).write(output);
                }
            }
        }
    }
}

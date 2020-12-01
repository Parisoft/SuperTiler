package com.parisoft.supertiler.util;

import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.parisoft.supertiler.SuperTiler.ARG_PALETTE;
import static com.parisoft.supertiler.SuperTiler.colorMap;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.namespace;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Palettes {

    public static void write() throws IOException {
        String palette = namespace.getString(ARG_PALETTE);

        if (palette != null) {
            IndexColorModel colorModel = (IndexColorModel) input.getColorModel();
            int[] rgb32 = new int[colorModel.getMapSize()];
            byte[] rgb15 = new byte[rgb32.length * 2];

            colorModel.getRGBs(rgb32);

            // remap
            for (int i = 0; i < rgb32.length; i++) {
                int r = colorMap.get(i);

                if (r > i) {
                    int tmp = rgb32[i];
                    rgb32[i] = rgb32[r];
                    rgb32[r] = tmp;
                }
            }

            // convert
            for (int i = 0; i < rgb32.length; i++) {
                int c = rgb32[i];
                int r = (c & 0x00f80000) >> 19;
                int g = (c & 0x0000f800) >> 6;
                int b = (c & 0x000000f8) << 7;
                int color = b | g | r;
                rgb15[2 * i] = (byte) (color & 0xff);
                rgb15[2 * i + 1] = (byte) (color >> 8);
            }

            Files.write(Paths.get(palette), rgb15, CREATE, TRUNCATE_EXISTING);
        }
    }
}

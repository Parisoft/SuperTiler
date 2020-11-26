package com.parisoft.supertiler.cmd;

import com.parisoft.supertiler.SuperTiler;
import com.parisoft.supertiler.pojo.Frame;
import com.parisoft.supertiler.pojo.Tile;

import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_METATILE;
import static com.parisoft.supertiler.SuperTiler.ARG_PALETTE;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.input;
import static com.parisoft.supertiler.SuperTiler.metatileHeight;
import static com.parisoft.supertiler.SuperTiler.metatileWidth;
import static com.parisoft.supertiler.SuperTiler.namespace;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class SpriteCmd implements Cmd {

    @Override
    public void execute() throws IOException {
        WritableRaster img = input.getRaster();
        List<Frame> frames = new ArrayList<>();

        for (int y = 0; y < img.getHeight(); y += metatileHeight) {
            for (int x = 0; x < img.getWidth(); x += metatileWidth) {
                Frame frame = new Frame(img, x, y);

                if (frame.isEmpty()) {
                    continue;
                }

                frames.add(frame);
            }
        }

        if (frames.isEmpty()) {
            System.err.println("No frames");
            return;
        }

        frames.forEach(Frame::createLargeMetatiles);
        frames.forEach(Frame::createSmallMetatiles);

        String tilesetPath = namespace.getString(ARG_TILESET);

        if (tilesetPath != null) {
            try (FileOutputStream output = new FileOutputStream(new File(tilesetPath))) {
                exit:
                for (Tile[] row : SuperTiler.tileset) {
                    for (Tile tile : row) {
                        if (tile == null) {
                            break exit;
                        }

                        tile.write(output);
                    }
                }
            }
        }

        String metatile = namespace.getString(ARG_METATILE);

        if (metatile != null) {
            for (int i = 0; i < frames.size(); i++) {
                try (FileOutputStream output = new FileOutputStream(new File(metatile + i + ".bin"))) {
                    frames.get(i).write(output);
                }
            }
        }

        String palette = namespace.getString(ARG_PALETTE);

        if (palette != null) {
            IndexColorModel colorModel = (IndexColorModel) input.getColorModel();
            int[] rgb32 = new int[colorModel.getMapSize()];
            byte[] rgb15 = new byte[rgb32.length * 2];

            colorModel.getRGBs(rgb32);

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

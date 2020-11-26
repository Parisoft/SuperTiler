package com.parisoft.supertiler.cmd;

import com.parisoft.supertiler.SuperTiler;
import com.parisoft.supertiler.pojo.Frame;
import com.parisoft.supertiler.pojo.Tile;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparser;

import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.parisoft.supertiler.SuperTiler.ARG_APPLYLARGE;
import static com.parisoft.supertiler.SuperTiler.ARG_APPLYSMALL;
import static com.parisoft.supertiler.SuperTiler.ARG_BPP;
import static com.parisoft.supertiler.SuperTiler.ARG_INPUT;
import static com.parisoft.supertiler.SuperTiler.ARG_METAH;
import static com.parisoft.supertiler.SuperTiler.ARG_METATILE;
import static com.parisoft.supertiler.SuperTiler.ARG_METAW;
import static com.parisoft.supertiler.SuperTiler.ARG_MODE;
import static com.parisoft.supertiler.SuperTiler.ARG_PALETTE;
import static com.parisoft.supertiler.SuperTiler.ARG_PALNUM;
import static com.parisoft.supertiler.SuperTiler.ARG_PRIORITY;
import static com.parisoft.supertiler.SuperTiler.ARG_TILEOFF;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESETNUM;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESIZE;
import static com.parisoft.supertiler.SuperTiler.ARG_XOFF;
import static com.parisoft.supertiler.SuperTiler.ARG_YOFF;
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
            throw new IllegalStateException("No metatiles found");
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

    public static void create(ArgumentParser parser) {
        Subparser sprite = parser.addSubparsers()
                .addParser("sprite", true)
                .description("Generates " + ARG_TILESET + ", " + ARG_PALETTE + "s, and " + ARG_METATILE + "s from image")
                .setDefault("cmd", new SpriteCmd());
        sprite.addArgument("-i", "--" + ARG_INPUT).nargs("?").required(true).type(String.class).help("Input indexed PNG image");
        sprite.addArgument("-t", "--" + ARG_TILESET).nargs("?").required(false).type(String.class).help("Output " + ARG_TILESET + " file");
        sprite.addArgument("-p", "--" + ARG_PALETTE).nargs("?").required(false).type(String.class).help("Output " + ARG_PALETTE + " file");
        sprite.addArgument("-m", "--" + ARG_METATILE).nargs("?").required(false).type(String.class).help("Prefix for " + ARG_METATILE + " files");
        sprite.addArgument("-M", "--" + ARG_MODE).nargs("?").required(false).type(String.class).choices("snes", "nes").setDefault("snes").help("Mode for target console");
        sprite.addArgument("-B", "--" + ARG_BPP).nargs("?").required(false).type(Integer.class).choices(2, 4, 8).setDefault(4).help("Depth or number of colors per pixel");
        sprite.addArgument("-W", "--" + ARG_METAW).nargs("?").required(false).type(Integer.class).setDefault(8).help("Metatile width in pixels");
        sprite.addArgument("-H", "--" + ARG_METAH).nargs("?").required(false).type(Integer.class).setDefault(8).help("Metatile height in pixels");
        sprite.addArgument("-s", "--" + ARG_TILESIZE).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5).setDefault(0)
                .help("Size of the sprites (SNES only):" + System.lineSeparator()
                              + "0 =  8x8  and 16x16 sprites" + System.lineSeparator()
                              + "1 =  8x8  and 32x32 sprites" + System.lineSeparator()
                              + "2 =  8x8  and 64x64 sprites" + System.lineSeparator()
                              + "3 = 16x16 and 32x32 sprites" + System.lineSeparator()
                              + "4 = 16x16 and 64x64 sprites" + System.lineSeparator()
                              + "5 = 32x32 and 64x64 sprites");
        sprite.addArgument("-S", "--" + ARG_APPLYSMALL).nargs("?").required(false).type(Boolean.class).setDefault(true)
                .help("If " + ARG_APPLYLARGE + " is not set, all tiles size are the one define in tileize. See applylarge when both are set. (SNES only)");
        sprite.addArgument("-L", "--" + ARG_APPLYLARGE).nargs("?").required(false).type(Boolean.class).setDefault(false)
                .help("When " + ARG_MODE + " is snes, SuperTiler will first scan for large tiles defined in " + ARG_TILESIZE + " then, if " + ARG_APPLYSMALL + " is set, it will replace the large tile for N small tiles if N <= large/small" + System.lineSeparator()
                              + "When " + ARG_MODE + " is nes, tiles are 8x16");
        sprite.addArgument("--" + ARG_PALNUM).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5, 6, 7).setDefault(0).help("Object " + ARG_PALETTE + " number");
        sprite.addArgument("--" + ARG_PRIORITY).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3).setDefault(2).help("Object " + ARG_PRIORITY + " (SNES only)");
        sprite.addArgument("--" + ARG_TILESETNUM).nargs("?").required(false).type(Integer.class).choices(0, 1).setDefault(0).help("Object " + ARG_TILESET + " number (SNES only)");
        sprite.addArgument("-X", "--" + ARG_XOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object X offset");
        sprite.addArgument("-Y", "--" + ARG_YOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object Y offset");
        sprite.addArgument("-T", "--" + ARG_TILEOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object tile offset");
    }
}

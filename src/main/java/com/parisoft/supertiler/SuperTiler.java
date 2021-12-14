package com.parisoft.supertiler;

import com.parisoft.supertiler.cmd.BackgroundCmd;
import com.parisoft.supertiler.cmd.Cmd;
import com.parisoft.supertiler.cmd.SpriteCmd;
import com.parisoft.supertiler.pojo.Mode;
import com.parisoft.supertiler.pojo.TileSize;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class SuperTiler {

    public static final String VERSION = "1.0";

    public static final String ARG_INPUT = "input";
    public static final String ARG_TILESET = "tileset";
    public static final String ARG_PALETTE = "palette";
    public static final String ARG_METATILE = "metatile";
    public static final String ARG_TILEMAP = "tilemap";
    public static final String ARG_MODE = "mode";
    public static final String ARG_BPP = "bpp";
    public static final String ARG_METAW = "metaw";
    public static final String ARG_METAH = "metah";
    public static final String ARG_TILESIZE = "tilesize";
    public static final String ARG_APPLYSMALL = "applysmall";
    public static final String ARG_APPLYLARGE = "applylarge";
    public static final String ARG_NO_DISCARD_REDUNDANT = "nodiscard";
    public static final String ARG_NO_DISCARD_FLIP = "noflip";
    public static final String ARG_PALNUM = "palnum";
    public static final String ARG_PRIORITY = "priority";
    public static final String ARG_XOFF = "xoff";
    public static final String ARG_YOFF = "yoff";
    public static final String ARG_TILEOFF = "tileoff";
    public static final String ARG_TILESETNUM = "tilesetnum";
    public static final String ARG_VERBOSE = "verbose";
    public static final String ARG_COLORMAP = "colormap";

    public static BufferedImage input;
    public static boolean verbose;
    public static Mode mode;
    public static byte bpp;
    public static byte objPalNum;
    public static byte objPriority;
    public static byte objTilesetNum;
    public static int objXOff;
    public static int objYOff;
    public static int objTileOff;
    public static TileSize tileSize;
    public static boolean applySmall;
    public static boolean applyLarge;
    public static boolean discardRedundant;
    public static boolean discardFlip;
    public static int metatileWidth;
    public static int metatileHeight;
    public static List<Integer> colorMap;
    public static Namespace namespace;

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("supertiler-" + VERSION + ".jar")
                .build()
                .defaultHelp(true)
                .description("Tile generation toolkit for NES and Super NES");
        SpriteCmd.create(parser);
        BackgroundCmd.create(parser);

        namespace = parser.parseArgsOrFail(args);
        verbose = getBool(ARG_VERBOSE);
        mode = Mode.valueOf(namespace.getString(ARG_MODE).toUpperCase());
        bpp = getByte(ARG_BPP);
        objPalNum = getByte(ARG_PALNUM);
        objPriority = getByte(ARG_PRIORITY);
        objTilesetNum = getByte(ARG_TILESETNUM);
        objXOff = getInt(ARG_XOFF);
        objYOff = getInt(ARG_YOFF);
        objTileOff = getInt(ARG_TILEOFF);
        tileSize = TileSize.valueOf(namespace.getInt(ARG_TILESIZE));
        applySmall = getBool(ARG_APPLYSMALL);
        applyLarge = getBool(ARG_APPLYLARGE);
        discardRedundant = !getBool(ARG_NO_DISCARD_REDUNDANT);
        discardFlip = !getBool(ARG_NO_DISCARD_FLIP);
        metatileWidth = getInt(ARG_METAW);
        metatileHeight = getInt(ARG_METAH);
        colorMap = namespace.getList(ARG_COLORMAP);
        input = ImageIO.read(new File(namespace.getString(ARG_INPUT)));

        try {
            ((Cmd) namespace.get("cmd")).execute();
        } catch (Exception e) {
            if (e.getMessage() != null) {
                System.err.println("ERROR: ".concat(e.getMessage()));
            } else {
                System.err.println("ERROR: ".concat(e.toString()));
            }

            if (verbose) {
                e.printStackTrace();
            }
        }
    }

    private static Boolean getBool(String arg) {
        Boolean bool = namespace.getBoolean(arg);

        if (bool == null) {
            return false;
        }

        return bool;
    }

    private static byte getByte(String arg) {
        return getInt(arg).byteValue();
    }

    private static Integer getInt(String arg) {
        Integer anInt = namespace.getInt(arg);

        if (anInt == null) {
            return 0;
        }

        return anInt;
    }
}

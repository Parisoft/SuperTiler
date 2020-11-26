package com.parisoft.supertiler;

import com.parisoft.supertiler.cmd.Cmd;
import com.parisoft.supertiler.cmd.SpriteCmd;
import com.parisoft.supertiler.pojo.SpriteSize;
import com.parisoft.supertiler.pojo.Tile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class SuperTiler {

    public static final String ARG_INPUT = "input";
    public static final String ARG_TILESET = "tileset";
    public static final String ARG_PALETTE = "palette";
    public static final String ARG_METATILE = "metatile";
    public static final String ARG_MODE = "mode";
    public static final String ARG_BPP = "bpp";
    public static final String ARG_METAW = "metaw";
    public static final String ARG_METAH = "metah";
    public static final String ARG_TILESIZE = "tilesize";
    public static final String ARG_APPLYSMALL = "applysmall";
    public static final String ARG_APPLYLARGE = "applylarge";
    public static final String ARG_PALNUM = "palnum";
    public static final String ARG_PRIORITY = "priority";
    public static final String ARG_XOFF = "xoff";
    public static final String ARG_YOFF = "yoff";
    public static final String ARG_TILEOFF = "tileoff";
    public static final String ARG_TILESETNUM = "tilesetnum";

    public static BufferedImage input;
    public static String mode;
    public static byte bpp;
    public static byte objPalNum;
    public static byte objPriority;
    public static byte objTilesetNum;
    public static byte objXOff;
    public static byte objYOff;
    public static SpriteSize objSpSize;
    public static byte objTileOff;
    public static boolean applySmall;
    public static boolean applyLarge;
    public static byte metatileWidth;
    public static byte metatileHeight;
    public static Namespace namespace;
    public static Tile[][] tileset = new Tile[16][16];

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("SuperTiler v1.0")
                .build()
                .defaultHelp(true)
                .description("Tile generation toolkit for NES and Super NES");
        SpriteCmd.create(parser);

        namespace = parser.parseArgsOrFail(args);
        mode = namespace.getString(ARG_MODE);
        bpp = namespace.getInt(ARG_BPP).byteValue();
        objPalNum = namespace.getInt(ARG_PALNUM).byteValue();
        objPriority = namespace.getInt(ARG_PRIORITY).byteValue();
        objTilesetNum = namespace.getInt(ARG_TILESETNUM).byteValue();
        objXOff = namespace.getInt(ARG_XOFF).byteValue();
        objYOff = namespace.getInt(ARG_YOFF).byteValue();
        objSpSize = SpriteSize.valueOf(namespace.getInt(ARG_TILESIZE));
        objTileOff = namespace.getInt(ARG_TILEOFF).byteValue();
        applySmall = namespace.getBoolean(ARG_APPLYSMALL);
        applyLarge = namespace.getBoolean(ARG_APPLYLARGE);
        metatileWidth = namespace.getInt(ARG_METAW).byteValue();
        metatileHeight = namespace.getInt(ARG_METAH).byteValue();
        input = ImageIO.read(new File(namespace.getString(ARG_INPUT)));

        try {
            ((Cmd) namespace.get("cmd")).execute();
        } catch (Exception e) {
            System.err.println("ERROR: ".concat(e.getMessage()));
        }

        System.out.println("Bye");
    }
}

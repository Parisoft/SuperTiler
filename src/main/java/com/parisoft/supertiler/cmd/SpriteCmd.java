package com.parisoft.supertiler.cmd;

import com.parisoft.supertiler.util.MetaTiles;
import com.parisoft.supertiler.util.Palettes;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.IOException;

import static com.parisoft.supertiler.SuperTiler.ARG_APPLYLARGE;
import static com.parisoft.supertiler.SuperTiler.ARG_APPLYSMALL;
import static com.parisoft.supertiler.SuperTiler.ARG_BPP;
import static com.parisoft.supertiler.SuperTiler.ARG_INPUT;
import static com.parisoft.supertiler.SuperTiler.ARG_METAH;
import static com.parisoft.supertiler.SuperTiler.ARG_METATILE;
import static com.parisoft.supertiler.SuperTiler.ARG_METAW;
import static com.parisoft.supertiler.SuperTiler.ARG_MODE;
import static com.parisoft.supertiler.SuperTiler.ARG_NO_DISCARD_FLIP;
import static com.parisoft.supertiler.SuperTiler.ARG_NO_DISCARD_REDUNDANT;
import static com.parisoft.supertiler.SuperTiler.ARG_PALETTE;
import static com.parisoft.supertiler.SuperTiler.ARG_PALNUM;
import static com.parisoft.supertiler.SuperTiler.ARG_PRIORITY;
import static com.parisoft.supertiler.SuperTiler.ARG_TILEOFF;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESETNUM;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESIZE;
import static com.parisoft.supertiler.SuperTiler.ARG_VERBOSE;
import static com.parisoft.supertiler.SuperTiler.ARG_XOFF;
import static com.parisoft.supertiler.SuperTiler.ARG_YOFF;

public class SpriteCmd implements Cmd {

    @Override
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "ConstantConditions"})
    public void execute() throws IOException {
        MetaTiles metaTiles = new MetaTiles();

        if (metaTiles.isEmpty()) {
            throw new IllegalStateException("No metatiles found");
        }

        metaTiles.write();
        Palettes.write();
    }

    public static void create(ArgumentParser parser) {
        Subparser sprite = parser.addSubparsers()
                .addParser("sprite", true)
                .aliases("sp")
                .description("Generates " + ARG_TILESET + ", " + ARG_PALETTE + "s, and " + ARG_METATILE + "s from image")
                .defaultHelp(true)
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
        sprite.addArgument("-S", "--" + ARG_APPLYSMALL).nargs("?").required(false).type(Boolean.class).setDefault(true).setConst(true)
                .help("If " + ARG_APPLYLARGE + " is not set, all tiles size are the one define in " + ARG_TILESIZE + ". See applylarge when both are set. (SNES only)");
        sprite.addArgument("-L", "--" + ARG_APPLYLARGE).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true)
                .help("When " + ARG_MODE + " is snes, SuperTiler will first scan for large tiles defined in " + ARG_TILESIZE + " then, if " + ARG_APPLYSMALL + " is set, it will replace the large tile for N small tiles if N <= large/small" + System.lineSeparator()
                              + "When " + ARG_MODE + " is nes, tiles are 8x16");
        sprite.addArgument("-D", "--" + ARG_NO_DISCARD_REDUNDANT).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Don't discard redundant tiles");
        sprite.addArgument("-F", "--" + ARG_NO_DISCARD_FLIP).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Don't discard using tile flipping");
        sprite.addArgument("--" + ARG_PALNUM).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5, 6, 7).setDefault(0).help("Object " + ARG_PALETTE + " number");
        sprite.addArgument("--" + ARG_PRIORITY).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3).setDefault(2).help("Object " + ARG_PRIORITY + " (SNES only)");
        sprite.addArgument("--" + ARG_TILESETNUM).nargs("?").required(false).type(Integer.class).choices(0, 1).setDefault(0).help("Object " + ARG_TILESET + " number (SNES only)");
        sprite.addArgument("-X", "--" + ARG_XOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object X offset");
        sprite.addArgument("-Y", "--" + ARG_YOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object Y offset");
        sprite.addArgument("-T", "--" + ARG_TILEOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object tile offset");
        sprite.addArgument("-v", "--"+ARG_VERBOSE).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Enable verbose error output");
    }
}

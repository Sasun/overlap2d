package com.uwsoft.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by CyberJoe on 4/24/2015.
 */
public class FontUtils {

    private static final String cache_name = "overlap2d-fonts-cache";

    private Preferences prefs;

    private HashMap<String, String> systemFontMap = new HashMap<>();

    public FontUtils() {
        prefs = Gdx.app.getPreferences(cache_name);
    }

    public String[] getSystemFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    public String[] getSystemFontsPaths() {
        String[] result;
        if (SystemUtils.IS_OS_WINDOWS) {
            result = new String[1];
            String path = System.getenv("WINDIR");
            result[0] = path + "\\" + "Fonts";
            return result;
        } else if(SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
            result = new String[4];
            result[0] = "~/Library/Fonts";
            result[1] = "/Library/Fonts";
            result[2] = "/System/Library/Fonts";
            return result;
        } else if (SystemUtils.IS_OS_LINUX) {
            result = new String[1];
            result[0] = "~/.fonts";
            return result;
        }

        return null;
    }

    public List<File> getSystemFontFiles() {
        // only retrieving ttf files
        String[] extensions = new String[] { "ttf", "TTF" };
        String[] paths = getSystemFontsPaths();

        ArrayList<File> files = new ArrayList<>();

        for(int i = 0; i < paths.length; i++) {
            File fontDirectory = new File(paths[i]);
            files.addAll(FileUtils.listFiles(fontDirectory, extensions, true));
        }

        return files;
    }

    public void preCacheSystemFontsMap() {
        List<File> fontFiles = getSystemFontFiles();

        for (File file : fontFiles) {
            Font f = null;
            try {
                if(!systemFontMap.containsValue(file.getAbsolutePath())) {
                    f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file.getAbsolutePath()));
                    String name = f.getFamily();
                    systemFontMap.put(name, file.getAbsolutePath());
                }
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        prefs.put(systemFontMap);
        prefs.flush();
    }

    public void loadCachedSystemFontMap() {
        systemFontMap = (HashMap<String, String>)prefs.get();
    }

    public void invalidateFontMap() {
        Array<String> names = new Array<>(getSystemFontNames());
        for(Iterator<Map.Entry<String, String>> it = systemFontMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            if(!names.contains(entry.getKey(), false)) {
                it.remove();
            }
        }
    }

    public void generateFontsMap() {
        loadCachedSystemFontMap();
        preCacheSystemFontsMap();
        invalidateFontMap();
    }

    public HashMap<String, String> getFontsMap() {
        if(systemFontMap.size() == 0) {
            generateFontsMap();
        }
        return systemFontMap;
    }

    public Array<String> getFontNamesFromMap() {
        AlphabeticalComparator comparator = new AlphabeticalComparator();
        Array<String> fontNames = new Array<>();

        for (Map.Entry<String, String> entry : systemFontMap.entrySet()) {
            fontNames.add(entry.getKey());
        }
        fontNames.sort(comparator);

        return fontNames;
    }

    public FileHandle getTTFByName(String fontName) {
        return new FileHandle(systemFontMap.get(fontName));
    }


    public class AlphabeticalComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}

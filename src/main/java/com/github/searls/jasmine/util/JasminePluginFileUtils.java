package com.github.searls.jasmine.util;

import com.github.searls.jasmine.io.FileUtilsWrapper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JasminePluginFileUtils {
    private static FileUtilsWrapper fileUtilsWrapper = new FileUtilsWrapper();

    public static List<File> filesForScriptsInDirectory(File directory, String prefix) throws IOException {
        List<File> files = new ArrayList<File>();
        if (directory != null) {
            fileUtilsWrapper.forceMkdir(directory);
            files = new ArrayList<File>(fileUtilsWrapper.listFiles(directory, new String[]{prefix}, true));
            Collections.sort(files);
        }
        return files;
    }

    public static String fileToString(File file) {
        try {
            return file.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

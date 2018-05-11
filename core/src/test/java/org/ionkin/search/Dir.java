package org.ionkin.search;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class Dir {

    public static void main(String... args) throws Exception {
        File dir = new File("/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/oldArticleWords/");
        FileUtils.cleanDirectory(dir);
    }
}

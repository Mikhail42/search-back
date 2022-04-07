package org.ionkin.search.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AppConfig {
    private static final Config config = ConfigFactory.load();
    public static final String basePath = config.getString("base.path");
    public static final String locale = config.getString("base.locale");
    public static final String wordSymbols = config.getString("word.symbols");
    public static final String searchableSymbols = config.getString("word.searchable-symbols");
}

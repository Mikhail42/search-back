# Wikipedia Search Engine
This search libraries allow to create a search server for Russian Wikipedia.
For use GUI to search, see [this project](https://github.com/Mikhail42/search-front).

## Global pre-requirement
- at least 12 GB free on disk
- at least 6 GB RAM. 8 GB is good enough
- internet to download wikipedia dump
- python 3
- *nix OS preferred (see wikiextractor)
- java 11, mvn, sbt
- understanding console applications
- Intellij Idea Community Edition (optional)

## How to use
**When you run Java, specify -Xmx5000m for better performance and to avoid of OutOfMemory error**.
For example, you can run class via Intellij Idea, then copy command from Idea `run` tab,
and add the option before main class.

1. Download [Russian Wiki Dump](https://dumps.wikimedia.org/ruwiki/). 4.3 GB bz2, unpacked size is 24 GB.
   Copy it to `$basePath = ~/workspace/wiki-bz2`. 
   See [Util](core/src/main/java/org/ionkin/search/Util.java) `basePath`.
2. Use Python [Wiki Extractor](https://github.com/attardi/wikiextractor) module to create instances of wiki files.
   In new version you also can use `xml-parser` module for that purpose, but it will be easy to use Wiki Extractor.
   `cd $basePath && python -m wikiextractor.WikiExtractor ruwiki-20211201-pages-articles-multistream.xml.bz2`
   It will take ~20 minutes. After that check `$basePath/text` directory.
3. Run [Tokenizer](index/src/main/java/org/ionkin/search/Tokenizer.java).
   After that there are 2 files in a `$basePath`: firstDocidFilenameMap.csv & tokens.chsls.
   firstDocidFilenameMap contains map of (first doc id in file -> fileName),
   tokens contains all normalized unique words (both russian and english) from wikipedia.
4. Run [Indexer](index/src/main/java/org/ionkin/search/Indexer.java) to create inverse index.
   It may take a few minutes. See logs to trace progress.
   After than index.chmsb should be created. This file contains map of (token -> list of pageId) in compact form.
5. Run [PositionsIndex](index/src/main/java/org/ionkin/search/PositionsIndex.java) to create inverse index with word positions.
   It may take about hour. See logs to trace progress.
   After than positions.chmsp should be created.
   This file contains map of (token -> (map of pageId → word positions on page)) in compact form.
6. Run [EvaluatorPerformance](boolparser/src/main/java/org/ionkin/search/EvaluatorPerformance.java)
   to create docids.chsi (set of doc id) and positions.sm (search map).
7. Execute `mvn clean install -DskipTests=true` in project directory in command line.
   After than you can use libs in search-front project.
8. Create lemmas via lemmatization module. Lemmas is used to search similar words. For that you need to download some dictionaries.
9. Run [search-front](https://github.com/Mikhail42/search-front).
   For that, go to `search-front`, and execute `sbt run` in command line.
10. Use GUI to search.

If you want to test it on small amount of wiki pages, you can, for example, rename `text` dir to `full-text`,
create `text` dir with at least 4 folders from `full-text` (see Util#threadPoolSize), and execute Java code.
If you want to remove generated files (you **need** to do it, if you want to regenerate them),
you can run [script](clean-generated-files.sh).

## Dictionary
- CHM -- [CompactHashMap](core/src/main/java/org/ionkin/search/map/CompactHashMap.java).
  chmss -- map of string -> string, chmsb -- map of string -> bytes.
- CHS -- [CompactHashSet](core/src/main/java/org/ionkin/search/set/CompactHashSet.java).
- CSV -- comma separated values. Text file format for tables

## Pre-requirement
- RAM about size of bz2 file. You need it to create and store index (bz2 is 6x times compressed in our case).
- Java 11
- To start GUI you need to have sbt or Intellij Idea with Scala plugin, but you can make build for Java 11.

## Modules

### core
Core classes (model) to make search engine possible.
E.g., this module provides `CompactHashMap`, a very fast & compact hash map.
`SearchMap` is adopted to words. 
`Index` allows to store it very compact & have fast access.

**Pre-requirement:** none, because it is set of pure functions and classes.

### xml-parser
Parse wiki.xml.bz2 file. Allows iterating over (Java typed) wiki pages with action:

```java
  File file = new File(Util.basePath + "ruwiki-20211201-pages-articles-multistream.xml.bz2");
  Consumer<WikiPage> action = ((wikiPage: WikiPage) -> System.err.println(wikiPage.getRevision().getText()));
  XmlParser xmlParser = new XmlParser();
  xmlParser.parseBz2File(file, action);
```

**Pre-requirement:** downloaded xml.bz2 file.

### graph
This module allows to create a word's frequency graph.

**Pre-requirement:** file with all words from wiki dump.

### index
- Tokenizer create list of tokens (words) for dump. It means that he create list of *valid* words to index and search.
- WikiParser parse wiki dump from XML, create list of articles
  and create association docId -> start & end position of article in the dump file.
- TextArticleIterator iterate over Text Articles
- Indexer create index files and join them.
- PositionsIndex create index with word positions.

**Pre-requirement:**

### lemmatization
Create [lemmas](https://en.wikipedia.org/wiki/Lemma_(morphology)) from words.
For example, word "модули" converts to "модул" (word "модуль" probably is better variant).
English example: word "lemmas" converts to "lemma". So, lemmatisation allow you to type "lemmas" and find articles with
word "lemma", and reverse, find acrticles with "lemmas" by "lemma".

### parsington
It is expression parser library. Allow to parse words syntax tree by search expression, where word are leaf, operations
like "or", "and" are links, and subtree means "braces".

**Pre-requirement:** none, because it is set of pure functions and classes.

### boolparser
helper for parsington.

**Pre-requirement:**
- created index files
- lemms
- maybe something else

### ranking
This module is used to range search result. Algorithm used [TF/IDF](https://ru.wikipedia.org/wiki/TF-IDF).

**Pre-requirement:** none, because it is set of pure functions.

### snippet
This module create snippet for search results. For example, if you type "cниппет поиск", your GUI
returns you some text from [Сниппет](https://ru.wikipedia.org/wiki/Сниппет) article:
```html
<h3><br>Сниппеты</br> в <br>поисковых</br> системах<h3>
Термином <br>сниппет</br> называют небольшие отрывки текста из найденной <br>поисковой</br> машиной страницы сайта...
```

**Pre-requirement:** none, because it is set of pure functions.

### collocation
This module is used to find associations between words.

**Pre-requirement:**
- set of wiki files after applying [Wiki Extractor](https://github.com/attardi/wikiextractor)
- language-part.ssm -- map of word to part of speach.
- lemm/allWordMap.chmss -- map of word to lemma.
- siRelat.si -- map of word to word_id
- isRelat.is -- map of word_id to word

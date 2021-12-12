# Wikipedia Search Engine
This search libraries allow to create a search server for Russian Wikipedia.
For use GUI to search, see [this project](https://github.com/Mikhail42/search-front).

## How to use
1. Download [Russian Wiki Dump, 4.3 GB bz2](https://dumps.wikimedia.org/ruwiki/).
2. Set [Util](core/src/main/java/org/ionkin/search/Util.java) `basePath` to directory with downloaded file.
3. Create lemmas via lemmatization module.
4. Create inverse index via index module.
5. Run search-front.
6. Use GUI to search.

## Pre-requirement
- RAM about size of bz2 file. You need it to create and store index.
  (bz2 is compressed format, can compress [about 3 times](https://tukaani.org/lzma/benchmarks.html)).
- Java 11
- To start GUI you need to have sbt or Intellij Idea with Scala plugin, but you can make build for Java 11.

## Modules

### core
Core classes (model) to make search engine possible.
E.g., this module provides `CompactHashMap`, a very fast & compact hash map.
`SearchMap` is adopted to words. 
`Index` allows to store it very compact & have fast access.

### graph
This module allows to create a word's frequency graph.

### index
- Tokenizer create list of tokens (words) for dump. It means that he create list of *valid* words to index and search.
- WikiParser parse wiki dump from XML, create list of articles
  and create association docId -> start & end position of article in the dump file.
- TextArticleIterator iterate over Text Articles
- Indexer create index files and join them.
- PositionsIndex create index with word positions

### lemmatization
Create [lemmas](https://en.wikipedia.org/wiki/Lemma_(morphology)) from words.
For example, word "модули" converts to "модул" (word "модуль" probably is better variant).
English example: word "lemmas" converts to "lemma". So, lemmatisation allow you to type "lemmas" and find articles with
word "lemma", and reverse, find acrticles with "lemmas" by "lemma".

### parsington
It is expression parser library. Allow to parse words syntax tree by search expression, where word are leaf, operations
like "or", "and" are links, and subtree means "braces".

### boolparser
helper for parsington.

### ranking
This module is used to range search result. Algorithm used [TF/IDF](https://ru.wikipedia.org/wiki/TF-IDF).

### snippet
This module create snippet for search results. For example, if you type "cниппет поиск", your GUI
returns you some text from [Сниппет](https://ru.wikipedia.org/wiki/Сниппет) article:
```html
<h3><br>Сниппеты</br> в <br>поисковых</br> системах<h3>
Термином <br>сниппет</br> называют небольшие отрывки текста из найденной <br>поисковой</br> машиной страницы сайта...
```

### collocation
This module is used to find associations between words.

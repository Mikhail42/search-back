package org.ionkin.search.xmlparser;

import org.ionkin.search.Util;
import org.ionkin.search.xmlparser.model.WikiPage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class Main {

    private static Consumer<WikiPage> action = (wikiPage -> System.err.println(wikiPage.getRevision().getText()));

    public static void main(String[] args) throws Exception {
        //testOnePage();
        testPartOfFile();
        //testWholeFile();
    }

    private static void testWholeFile() throws XMLStreamException, JAXBException, IOException {
        File file = new File(Util.basePath + "ruwiki-20211201-pages-articles-multistream.xml.bz2");
        XmlParser xmlParser = new XmlParser();
        xmlParser.parseBz2File(file, action);
    }

    private static void testPartOfFile() throws XMLStreamException, JAXBException {
        // change package-info namespace to http://www.mediawiki.org/xml/export-0.10/ before test
        String pageXml =
                "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd\" version=\"0.10\" xml:lang=\"ru\">\n" +
                "  <siteinfo>\n" +
                "    <sitename>Википедия</sitename>\n" +
                "    <dbname>ruwiki</dbname>\n" +
                "    <base>https://ru.wikipedia.org/wiki/%D0%97%D0%B0%D0%B3%D0%BB%D0%B0%D0%B2%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0</base>\n" +
                "    <generator>MediaWiki 1.38.0-wmf.9</generator>\n" +
                "    <case>first-letter</case>\n" +
                "    <namespaces>\n" +
                "      <namespace key=\"-2\" case=\"first-letter\">Медиа</namespace>\n" +
                "      <namespace key=\"-1\" case=\"first-letter\">Служебная</namespace>\n" +
                "      <namespace key=\"0\" case=\"first-letter\" />\n" +
                "      <namespace key=\"1\" case=\"first-letter\">Обсуждение</namespace>\n" +
                "      <namespace key=\"2\" case=\"first-letter\">Участник</namespace>\n" +
                "      <namespace key=\"3\" case=\"first-letter\">Обсуждение участника</namespace>\n" +
                "      <namespace key=\"4\" case=\"first-letter\">Википедия</namespace>\n" +
                "      <namespace key=\"5\" case=\"first-letter\">Обсуждение Википедии</namespace>\n" +
                "      <namespace key=\"6\" case=\"first-letter\">Файл</namespace>\n" +
                "      <namespace key=\"7\" case=\"first-letter\">Обсуждение файла</namespace>\n" +
                "      <namespace key=\"8\" case=\"first-letter\">MediaWiki</namespace>\n" +
                "      <namespace key=\"9\" case=\"first-letter\">Обсуждение MediaWiki</namespace>\n" +
                "      <namespace key=\"10\" case=\"first-letter\">Шаблон</namespace>\n" +
                "      <namespace key=\"11\" case=\"first-letter\">Обсуждение шаблона</namespace>\n" +
                "      <namespace key=\"12\" case=\"first-letter\">Справка</namespace>\n" +
                "      <namespace key=\"13\" case=\"first-letter\">Обсуждение справки</namespace>\n" +
                "      <namespace key=\"14\" case=\"first-letter\">Категория</namespace>\n" +
                "      <namespace key=\"15\" case=\"first-letter\">Обсуждение категории</namespace>\n" +
                "      <namespace key=\"100\" case=\"first-letter\">Портал</namespace>\n" +
                "      <namespace key=\"101\" case=\"first-letter\">Обсуждение портала</namespace>\n" +
                "      <namespace key=\"102\" case=\"first-letter\">Инкубатор</namespace>\n" +
                "      <namespace key=\"103\" case=\"first-letter\">Обсуждение Инкубатора</namespace>\n" +
                "      <namespace key=\"104\" case=\"first-letter\">Проект</namespace>\n" +
                "      <namespace key=\"105\" case=\"first-letter\">Обсуждение проекта</namespace>\n" +
                "      <namespace key=\"106\" case=\"first-letter\">Арбитраж</namespace>\n" +
                "      <namespace key=\"107\" case=\"first-letter\">Обсуждение арбитража</namespace>\n" +
                "      <namespace key=\"828\" case=\"first-letter\">Модуль</namespace>\n" +
                "      <namespace key=\"829\" case=\"first-letter\">Обсуждение модуля</namespace>\n" +
                "      <namespace key=\"2300\" case=\"first-letter\">Гаджет</namespace>\n" +
                "      <namespace key=\"2301\" case=\"first-letter\">Обсуждение гаджета</namespace>\n" +
                "      <namespace key=\"2302\" case=\"case-sensitive\">Определение гаджета</namespace>\n" +
                "      <namespace key=\"2303\" case=\"case-sensitive\">Обсуждение определения гаджета</namespace>\n" +
                "    </namespaces>\n" +
                "  </siteinfo>\n" +
                "  <page>\n" +
                "    <title>Базовая статья</title>\n" +
                "    <ns>0</ns>\n" +
                "    <id>4</id>\n" +
                "    <redirect title=\"Заглавная страница\" />\n" +
                "    <revision>\n" +
                "      <id>237414</id>\n" +
                "      <parentid>22491</parentid>\n" +
                "      <timestamp>2004-08-09T04:36:57Z</timestamp>\n" +
                "      <contributor>\n" +
                "        <username>Maximaximax</username>\n" +
                "        <id>450</id>\n" +
                "      </contributor>\n" +
                "      <comment>redir</comment>\n" +
                "      <model>wikitext</model>\n" +
                "      <format>text/x-wiki</format>\n" +
                "      <text bytes=\"49\" xml:space=\"preserve\">#REDIRECT [[Заглавная страница]]</text>\n" +
                "      <sha1>fv75zcmgjvd2h4rn3jqc0yrlf07afkh</sha1>\n" +
                "    </revision>\n" +
                "  </page>" +
                "</mediawiki>";
        XmlParser xmlParser = new XmlParser();
        InputStream in = new ByteArrayInputStream(pageXml.getBytes(StandardCharsets.UTF_8));
        xmlParser.parse(in, action);
    }

    private static void testOnePage() throws XMLStreamException, JAXBException {
        // change package-info namespace to empty before test
        XmlParser xmlParser = new XmlParser();
        String pageXml =
                "<page>\n" +
                        "    <title>Базовая статья</title>\n" +
                        "    <ns>0</ns>\n" +
                        "    <id>4</id>\n" +
                        "    <redirect title=\"Заглавная страница\" />\n" +
                        "    <revision>\n" +
                        "      <id>237414</id>\n" +
                        "      <parentid>22491</parentid>\n" +
                        "      <timestamp>2004-08-09T04:36:57Z</timestamp>\n" +
                        "      <contributor>\n" +
                        "        <username>Maximaximax</username>\n" +
                        "        <id>450</id>\n" +
                        "      </contributor>\n" +
                        "      <comment>redir</comment>\n" +
                        "      <model>wikitext</model>\n" +
                        "      <format>text/x-wiki</format>\n" +
                        "      <text bytes=\"49\" xml:space=\"preserve\">#REDIRECT [[Заглавная страница]]</text>\n" +
                        "      <sha1>fv75zcmgjvd2h4rn3jqc0yrlf07afkh</sha1>\n" +
                        "    </revision>\n" +
                        "  </page>";
        InputStream in = new ByteArrayInputStream(pageXml.getBytes(StandardCharsets.UTF_8));
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = factory.createXMLStreamReader(in, StandardCharsets.UTF_8.name());

        JAXBContext jc = JAXBContext.newInstance(WikiPage.class);
        WikiPage page = xmlParser.readPage(xmlReader, jc);
        action.accept(page);
        int a = 5;
    }
}

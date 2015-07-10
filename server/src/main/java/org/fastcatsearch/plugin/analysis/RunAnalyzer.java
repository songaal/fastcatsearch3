package org.fastcatsearch.plugin.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.*;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.LicenseInvalidException;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by swsong on 2015. 7. 10..
 */
public class RunAnalyzer {
    protected static Logger logger = LoggerFactory.getLogger(RunAnalyzer.class);

    private AnalysisPlugin plugin;
    private String pluginId;

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            printUsage();
            System.exit(0);
        }

        File pluginDir = new File(args[0]);
        String pluginClassName = args[1];
        String analyzerId = args[2];
        RunAnalyzer runAnalyzer = new RunAnalyzer(pluginDir, pluginClassName);
        AnalyzerPool analyzerPool = runAnalyzer.getAnalyzerPool(analyzerId);
        Analyzer analyzer = null;

        try {
            analyzer = analyzerPool.getFromPool();
            //사용자 입력을 계속 받아들인다.

            Scanner sc = new Scanner(System.in);
            System.out.println("==================================");
            System.out.println(" Fastcat analyzer");
            System.out.println(" Enter 'quit' for exit program. ");
            System.out.println("==================================");
            System.out.print("Input String: ");
            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                if (str.equalsIgnoreCase("quit")) {
                    break;
                }
                try {
                    char[] value = str.toCharArray();
                    TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(value), new AnalyzerOption());
                    tokenStream.reset();

                    CharsRefTermAttribute termAttribute = null;
                    if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
                        termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
                    }
                    SynonymAttribute synonymAttribute = null;
                    if (tokenStream.hasAttribute(SynonymAttribute.class)) {
                        synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
                    }
                    AdditionalTermAttribute additionalTermAttribute = null;
                    if (tokenStream.hasAttribute(AdditionalTermAttribute.class)) {
                        additionalTermAttribute = tokenStream.getAttribute(AdditionalTermAttribute.class);
                    }

                    StopwordAttribute stopwordAttribute = null;
                    if (tokenStream.hasAttribute(StopwordAttribute.class)) {
                        stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
                    }

                    CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

                    while (tokenStream.incrementToken()) {
                        String word = "";
                        //기본적으로 분석된 단어는 CharsRefTermAttribute 에 들어있다.
                        if (termAttribute != null) {
                            word = termAttribute.toString();
                        } else {
                            //CharsRefTermAttribute 에 넣지 않는 분석기의 경우 CharTermAttribute 에 들어있게 된다.
                            word = charTermAttribute.toString();
                        }

                        //불용어로 판단되면 건너뛴다.
                        if (stopwordAttribute.isStopword()) {
                            continue;
                        }

                        //
                        // 분석된 단어를 출력한다.
                        //
                        System.out.print(">> ");
                        System.out.println(word);

                        //유사어가 존재하면 리스트를 출력한다.
                        if (synonymAttribute != null) {
                            List synonyms = synonymAttribute.getSynonyms();
                            if (synonyms != null) {
                                for (Object synonymObj : synonyms) {
                                    if (synonymObj instanceof CharVector) {
                                        CharVector synonym = (CharVector) synonymObj;
                                        System.out.print("S> ");
                                        System.out.println(synonym);
                                    } else if (synonymObj instanceof List) {
                                        List synonymList = (List) synonymObj;
                                        for (Object synonym : synonymList) {
                                            System.out.print("S> ");
                                            System.out.println(synonym);
                                        }
                                    }
                                }
                            }
                        }

                        //추가단어가 존재하면 출력한다.
                        //추가단어는 상품명분석기에서 규칙에 의해 추가로 생성되는 단어들이며, 일반적으로는 존재하지 않는다.
                        if (additionalTermAttribute != null && additionalTermAttribute.size() > 0) {
                            Iterator<String> termIter = additionalTermAttribute.iterateAdditionalTerms();
                            while (termIter.hasNext()) {
                                String token = termIter.next();
                                System.out.print("A> ");
                                System.out.println(word);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.print("Input String: ");
            }
        } finally {
            if (analyzer != null) {
                analyzerPool.releaseToPool(analyzer);
            }
        }
        System.out.print("Bye!");
    }

    private static void printUsage() {
        System.out.println("Usage : java " + RunAnalyzer.class.getName() + " <pluginDir> <pluginClassName> <analyzerId>");
        System.out.println("Example");
        System.out.println("$ java " + RunAnalyzer.class.getName()+ " plugin/analysis/Korean org.fastcatsearch.plugin.analysis.ko.KoreanAnalysisPlugin standard");
    }


    public RunAnalyzer(File pluginDir, String pluginClassName) {
        Environment env = new Environment(pluginDir.getAbsolutePath());

        File pluginConfigFile = new File(pluginDir, SettingFileNames.pluginConfig);
        try {
            InputStream is = new FileInputStream(pluginConfigFile);
            JAXBContext analysisJc = JAXBContext.newInstance(AnalysisPluginSetting.class);
            Unmarshaller analysisUnmarshaller = analysisJc.createUnmarshaller();
            PluginSetting pluginSetting = (PluginSetting) analysisUnmarshaller.unmarshal(is);
            String serverId = env.getServerId();

            boolean useDB = false;
            plugin = (AnalysisPlugin) DynamicClassLoader.loadObject(pluginClassName, Plugin.class, new Class<?>[]{File.class, PluginSetting.class, String.class}, new Object[]{pluginDir, pluginSetting, serverId});
            plugin.load(useDB);
            pluginId = plugin.getPluginSetting().getId();
        } catch (FileNotFoundException e) {
            logger.error("{} plugin 설정파일을 읽을수 없음.", pluginDir.getName());
        } catch (JAXBException e) {
            logger.error("plugin 설정파일을 읽는중 에러. {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IO에러발생. {}", e.getMessage());
        } catch (LicenseInvalidException e) {
            logger.error("라이선스가 유효하지 않습니다. {}", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public AnalyzerPool getAnalyzerPool(String analyzerId) {
        if(plugin.isLoaded()) {
            AnalyzerPool pool = plugin.getAnalyzerPool(analyzerId);
            if(pool != null) {
                return pool;
            } else {
                throw new RuntimeException("Cannot find analyzer >> " + (pluginId + "." + analyzerId));
            }
        }
        return null;
    }

}

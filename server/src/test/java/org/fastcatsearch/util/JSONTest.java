package org.fastcatsearch.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import net.minidev.json.writer.JsonReader;
import net.minidev.json.writer.JsonReaderI;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minidev.json.parser.JSONParser.*;

/**
 * Created by swsong on 2015. 6. 24..
 */
public class JSONTest {
    @Test
    public void testSpecialString() throws Exception {
        String str = "“3D프린터로 아이디어 형상을 현실로 실현하다”";
        String value2 = JSONObject.escape(str);

        System.out.println(str);
        System.out.println(value2);
    }

    @Test
    public void testCustomJSONWriter() throws Exception {
        StringWriter sw = new StringWriter();
        CustomJSONWriter w = new CustomJSONWriter(sw);

        String str = "“3D프린터로 아이디어 형상을 현실로 실현하다”";
        w.object()
                .key("unicode").value(str)
                .key("no-unicode").value(str, false)
                .endObject();

        System.out.println(sw.toString());

    }

    @Test
     public void test1() throws IOException {
        String json = "{\"Abc\":\"123\", \"DEF\":\"123\", \"ghF\":\"123\"}";
        Map<String, Object> map = JsonUtil.json2ObjectWithLowercaseKey(json);
        for(String key : map.keySet()) {
            System.out.println(key);
        }
    }

    @Test
    public void testSpeed() throws IOException {
        String json = "{\"ID\":\"64269\",\"PRODUCTCODE\":\"64269\",\"SHOPCODE\":\"dna\",\"PRODUCTNAME\":\"630 (프레스캇)\",\"PRODUCTMAKER\":\"인텔\",\"MAKERKEYWORD\":\"\",\"PRODUCTBRAND\":\"펜티엄4\",\"BRANDKEYWORD\":\"펜티엄4,  펜티엄\",\"PRODUCTMODEL\":\"\",\"MODELWEIGHT\":\"\",\"SIMPLEDESCRIPTION\":\"인텔(소켓775)|64(32)비트|싱글 코어|쓰레드 2개|3.0GHz|2MB\",\"ADDDESCRIPTION\":\"\",\"CMDESCRIPTION\":\"\",\"LOWESTPRICE\":\"703\",\"MOBILEPRICE\":\"2850\",\"AVERAGEPRICE\":\"1352\",\"SHOPQUANTITY\":\"2\",\"DISCONTINUED\":\"N\",\"CATEGORYCODE1\":\"861\",\"CATEGORYCODE2\":\"873\",\"CATEGORYCODE3\":\"959\",\"CATEGORYCODE4\":\"0\",\"CATEGORYKEYWORD\":\"PC 주요부품,CPU\",\"CATEGORYWEIGHT\":\"CPU\",\"REGISTERDATE\":\"20050811\",\"MANUFACTUREDATE\":\"20050601\",\"MODIFYDATE\":\"20151217\",\"MANAGERKEYWORD\":\"\",\"PROMOTIONPRICE\":\"0\",\"BUNDLENAME\":\"벌크\",\"BUNDLEDISPLAYSEQUENCE\":\"0\",\"SELECTYN\":\"Y\",\"PRICECOMPARESERVICEYN\":\"Y\",\"OPTIONCODEDATAS\":\"소켓 구분^인텔(소켓775), 연산 체계^64(32), 코어 형태^싱글 코어, 동작 속도^3.0 ~ 3.49, L2 캐시 메모리^2MB, 쓰레드 형태^쓰레드 2개\",\"MAKERCODE\":\"3156\",\"BRANDCODE\":\"534\",\"MOVIEYN\":\"N\",\"PRICELOCKYN\":\"N\",\"STVIEWBIT\":\"c2,c3\",\"NATTRIBUTEVALUESEQ\":\"224,245,256,270,283,285,293,300,308,324,346,355,359,89512,90401,90408,91815,146726\"}";
        System.out.println(JsonUtil.json2ObjectWithLowercaseKey(json));
        long st = System.currentTimeMillis();
        for(int i =0;i < 25000; i++) {
            Map<String, Object> map = JsonUtil.json2ObjectWithLowercaseKey(json);
        }

        System.out.println((System.currentTimeMillis() - st) + "ms");
    }

    @Test
    public void testSpeedInline() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(JsonUtil.module);
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() { };
        String json = "{\"ID\":\"64269\",\"PRODUCTCODE\":\"64269\",\"SHOPCODE\":\"dna\",\"PRODUCTNAME\":\"630 (프레스캇)\",\"PRODUCTMAKER\":\"인텔\",\"MAKERKEYWORD\":\"\",\"PRODUCTBRAND\":\"펜티엄4\",\"BRANDKEYWORD\":\"펜티엄4,  펜티엄\",\"PRODUCTMODEL\":\"\",\"MODELWEIGHT\":\"\",\"SIMPLEDESCRIPTION\":\"인텔(소켓775)|64(32)비트|싱글 코어|쓰레드 2개|3.0GHz|2MB\",\"ADDDESCRIPTION\":\"\",\"CMDESCRIPTION\":\"\",\"LOWESTPRICE\":\"703\",\"MOBILEPRICE\":\"2850\",\"AVERAGEPRICE\":\"1352\",\"SHOPQUANTITY\":\"2\",\"DISCONTINUED\":\"N\",\"CATEGORYCODE1\":\"861\",\"CATEGORYCODE2\":\"873\",\"CATEGORYCODE3\":\"959\",\"CATEGORYCODE4\":\"0\",\"CATEGORYKEYWORD\":\"PC 주요부품,CPU\",\"CATEGORYWEIGHT\":\"CPU\",\"REGISTERDATE\":\"20050811\",\"MANUFACTUREDATE\":\"20050601\",\"MODIFYDATE\":\"20151217\",\"MANAGERKEYWORD\":\"\",\"PROMOTIONPRICE\":\"0\",\"BUNDLENAME\":\"벌크\",\"BUNDLEDISPLAYSEQUENCE\":\"0\",\"SELECTYN\":\"Y\",\"PRICECOMPARESERVICEYN\":\"Y\",\"OPTIONCODEDATAS\":\"소켓 구분^인텔(소켓775), 연산 체계^64(32), 코어 형태^싱글 코어, 동작 속도^3.0 ~ 3.49, L2 캐시 메모리^2MB, 쓰레드 형태^쓰레드 2개\",\"MAKERCODE\":\"3156\",\"BRANDCODE\":\"534\",\"MOVIEYN\":\"N\",\"PRICELOCKYN\":\"N\",\"STVIEWBIT\":\"c2,c3\",\"NATTRIBUTEVALUESEQ\":\"224,245,256,270,283,285,293,300,308,324,346,355,359,89512,90401,90408,91815,146726\"}";
        System.out.println(objectMapper.readValue(json, typeReference));
        long st = System.currentTimeMillis();
        for(int i =0;i < 25000; i++) {
            Map<String, Object> map = objectMapper.readValue(json, typeReference);
        }

        System.out.println((System.currentTimeMillis() - st) + "ms");
    }

    @Test
    public void testJsonSmart() throws ParseException {
        String json = "{\"ID\":\"64269\",\"PRODUCTCODE\":\"64269\",\"SHOPCODE\":\"dna\",\"PRODUCTNAME\":\"630 (프레스캇)\",\"PRODUCTMAKER\":\"인텔\",\"MAKERKEYWORD\":\"\",\"PRODUCTBRAND\":\"펜티엄4\",\"BRANDKEYWORD\":\"펜티엄4,  펜티엄\",\"PRODUCTMODEL\":\"\",\"MODELWEIGHT\":\"\",\"SIMPLEDESCRIPTION\":\"인텔(소켓775)|64(32)비트|싱글 코어|쓰레드 2개|3.0GHz|2MB\",\"ADDDESCRIPTION\":\"\",\"CMDESCRIPTION\":\"\",\"LOWESTPRICE\":\"703\",\"MOBILEPRICE\":\"2850\",\"AVERAGEPRICE\":\"1352\",\"SHOPQUANTITY\":\"2\",\"DISCONTINUED\":\"N\",\"CATEGORYCODE1\":\"861\",\"CATEGORYCODE2\":\"873\",\"CATEGORYCODE3\":\"959\",\"CATEGORYCODE4\":\"0\",\"CATEGORYKEYWORD\":\"PC 주요부품,CPU\",\"CATEGORYWEIGHT\":\"CPU\",\"REGISTERDATE\":\"20050811\",\"MANUFACTUREDATE\":\"20050601\",\"MODIFYDATE\":\"20151217\",\"MANAGERKEYWORD\":\"\",\"PROMOTIONPRICE\":\"0\",\"BUNDLENAME\":\"벌크\",\"BUNDLEDISPLAYSEQUENCE\":\"0\",\"SELECTYN\":\"Y\",\"PRICECOMPARESERVICEYN\":\"Y\",\"OPTIONCODEDATAS\":\"소켓 구분^인텔(소켓775), 연산 체계^64(32), 코어 형태^싱글 코어, 동작 속도^3.0 ~ 3.49, L2 캐시 메모리^2MB, 쓰레드 형태^쓰레드 2개\",\"MAKERCODE\":\"3156\",\"BRANDCODE\":\"534\",\"MOVIEYN\":\"N\",\"PRICELOCKYN\":\"N\",\"STVIEWBIT\":\"c2,c3\",\"NATTRIBUTEVALUESEQ\":\"224,245,256,270,283,285,293,300,308,324,346,355,359,89512,90401,90408,91815,146726\"}";
        JSONParser parser = new JSONParser(MODE_JSON_SIMPLE);
        System.out.println(parser.parse(json));
        long st = System.currentTimeMillis();
        for(int i =0;i < 25000; i++) {
//            Map<String, Object> o = parser.parse(json);
            String document = json.substring(2);
        }
        System.out.println((System.currentTimeMillis() - st) + "ms");
    }
}

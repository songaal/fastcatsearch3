package org.fastcatsearch.sample;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Random;
import java.util.UUID;

public class SampleDataGenerator implements Runnable {

    private String endpoint;
    private int limit;
    private int sleep;
    private boolean update;
    private Random r = new Random(System.nanoTime());

    public SampleDataGenerator(String endpoint, String range, String interval, String usingUpdate) {
        this.endpoint = endpoint;
        this.limit = Integer.parseInt(range);
        this.sleep = Integer.parseInt(interval);
        this.update = Boolean.parseBoolean(usingUpdate);
    }

    @Override
    public void run() {
        HttpClient client = HttpClientBuilder.create().build();

        for (int k = 0; k < 100000000; k++) {
            int i = r.nextInt(limit);

            String key = String.format("%032d", i);
            String title = UUID.randomUUID().toString();
            Writer writer = new StringWriter();
            new JSONWriter(writer).object().key("FID").value(key).key("TITLE").value(title).endObject();
            String JSON_STRING = writer.toString();
            StringEntity requestEntity = new StringEntity(
                    JSON_STRING,
                    ContentType.APPLICATION_JSON);

            HttpRequestBase request = null;
            if (!update) {
                HttpPost r = new HttpPost(endpoint);
                r.setEntity(requestEntity);
                request = r;
            } else {
                int type = r.nextInt(10);
                if (type > 16) {
                    //insert
                    HttpPost r = new HttpPost(endpoint);
                    r.setEntity(requestEntity);
                    request = r;
                } else if (type > 13) {
                    //update
                    HttpPut r = new HttpPut(endpoint);
                    r.setEntity(requestEntity);
                    request = r;
                } else {
                    //delete
                    HttpDeleteWithBody r = new HttpDeleteWithBody(endpoint);
                    r.setEntity(requestEntity);
                    request = r;
                }
            }
//            System.out.println(JSON_STRING);
            HttpResponse response = null;
            try {
                response = client.execute(request);
                response.getEntity().getContent().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            System.out.println(response);
            if (k % 100 == 0) {
                System.out.println("Insert " + k +"...");
                System.out.println(JSON_STRING);
                System.out.println(response);
            }
            int delay = r.nextInt(sleep) + 5;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    * POST http://localhost:8090/service/index?collectionId=film
    * {"FID":"00000000000000000000000000066312","TITLE":"4617d4ac-9f09-451a-ad60-25bcc6f6c384"}
    * */
    public static void main(String[] args) {
        new Thread(new SampleDataGenerator(args[0], args[1], args[2], args[3])).start();
    }

    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }
    }


}

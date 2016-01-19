package org.fastcatsearch.tools;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.mortbay.util.ajax.JSON;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * Created by swsong on 2016. 1. 19..
 */
public class IndexRestAPIGen {

    private final static String INDEX_API = "/service/index";

    private final String host;

    public static void main(String... args) {
        IndexRestAPIGen apigen = new IndexRestAPIGen("http://localhost:8090");
        String collection = "film";
        int LIMIT = 1000 * 1000;
        Random r = new Random(System.currentTimeMillis());
        try {

            for (int i = 0; i < LIMIT; i++) {

                String data = makeJson(r);
                System.out.println(data);
                apigen.requestAPI(INDEX_API, collection, data);

                int pause = r.nextInt(500) + 50;
                Thread.sleep(pause);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public IndexRestAPIGen(String host) {
        this.host = host;
    }

    public boolean requestAPI(String path, String collection, String resource) throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(host).path(path).queryParam("collectionId", collection);
        Response response = webTarget.request(MediaType.APPLICATION_JSON).post(Entity.json(resource));
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            return true;
        } else {
            String entity = response.readEntity(String.class);
            throw new Exception(entity);
        }
    }

    private static String[] ratingType = new String[]{"5 Star", "4 Star", "3 Star", "2 Star", "1 Star"};

    private static String makeJson(Random r) {
        int id = r.nextInt(Integer.MAX_VALUE - 1);
        String title = "title-" + id;
        String desc = "desc-" + id;
        String category = String.valueOf(r.nextInt(99) + 100);
        int price = (r.nextInt(300) + 50) * 100;
        String rating = ratingType[r.nextInt(5)];
        String actors = "Actor-" + r.nextInt(20);
        JSONStringer o = new JSONStringer();
        try {
            o.object().key("FID").value(id)
                    .key("TITLE").value(title)
                    .key("DESCRIPTION").value(desc)
                    .key("CATEGORY").value(category)
                    .key("PRICE").value(price)
                    .key("RATING").value(rating)
                    .key("ACTORS").value(actors)
                    .endObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return o.toString();
    }


}

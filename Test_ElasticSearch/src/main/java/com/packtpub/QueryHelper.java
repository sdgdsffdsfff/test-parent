package com.packtpub;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.sohu.tv.index.data.engine.es.ElasticSearchCenter;
import com.sohu.tv.index.data.engine.es.ElasticSearchClientFactory;
import com.sohu.tv.index.data.engine.es.impl.ElasticSearchCenterImpl;

import java.io.IOException;
import java.util.Random;

/**
 * Created by alberto on 9/21/13.
 */
public class QueryHelper {
    
    private static ElasticSearchCenter elasticSearchCenter = new ElasticSearchCenterImpl();

    private static Client client;
    
    
    private String[] tags=new String[]{"nice", "cool", "bad", "amazing"};

    private String getTag(){
        return tags[new Random().nextInt(tags.length)];
    }

    public void populateData(String index, String type) {
        client = elasticSearchCenter.getClient();
        try {
            client.admin().indices().prepareCreate(index)
                    .addMapping(type, XContentFactory.jsonBuilder()
                            .startObject()
                            .startObject(type)
                            .startObject("_timestamp").field("enabled", true).field("store", "yes").endObject()
                            .startObject("_ttl").field("enabled", true).field("store", "yes").endObject()
                            .startObject("properties")
                            .startObject("name")
                            .field("type", "string")
                            .field("term_vector", "with_positions_offsets")
                            .field("store", "yes")
                            .endObject()
                            .endObject()
                            .endObject()
                            .endObject())
                    .execute().actionGet();
        } catch (IOException e) {
            System.out.println("Unable to create mapping");
        }
        client.admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        BulkRequestBuilder bulker = client.prepareBulk();
        for (Integer i = 1; i <= 1000; i++) {
            bulker.add(client.prepareIndex(index, type, i.toString()).setSource("text", i.toString(), "number1", i + 1, "number2", i % 2, "tag", getTag()));
        }
        bulker.execute().actionGet();

        client.admin().indices().prepareRefresh(index).execute().actionGet();

    }

    public Client getClient() {
        return client;
    }
}

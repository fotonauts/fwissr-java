package com.fotonauts.fwissr.source;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fotonauts.fwissr.FwissrRuntimeException;
import com.fotonauts.fwissr.SmarterMap;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class MongodbSource extends Source {

    public final static Set<String> TOP_LEVEL_COLLECTIONS = Collections.unmodifiableSet(Collections.singleton("fwissr"));

    private static Pattern pattern = Pattern.compile("mongodb://(?<hostname>[^:/]+)(:(?<port>[0-9]+))?/(?<dbname>[^?]*)(?:\\?.*)?");

    private static Map<String, MongoClient> mongoClientCache = new HashMap<>();

    private synchronized static Mongo getMongo(String shortUrl) throws UnknownHostException {
        if (!mongoClientCache.containsKey(shortUrl)) {
            String tokens[] = shortUrl.split(":");
            MongoClient mongo = new MongoClient(tokens[0], Integer.parseInt(tokens[1]));
            mongoClientCache.put(shortUrl, mongo);
        }
        return mongoClientCache.get(shortUrl);
    }

    private static DB getMongodbDatabase(String url) throws UnknownHostException {
        Matcher tokens = pattern.matcher(url);
        if (tokens.matches()) {
            String shortUrl = tokens.group("hostname") + ":" + (tokens.group("port") != null ? tokens.group("port") : "27017");
            return getMongo(shortUrl).getDB(tokens.group("dbname"));
        }
        throw new FwissrRuntimeException("Can not parse mongodb url: " + url);
    }

    // package visibility for testing
    DBCollection collection;

    public MongodbSource(DBCollection collection, SmarterMap options) {
        super(options);
        this.collection = collection;
    }

    public static MongodbSource fromSettings(SmarterMap settings) {
        SmarterMap options = settings.clone();
        options.remove("mongodb");
        options.remove("collection");
        options.freeze();

        DBCollection collection;
        try {
            collection = MongodbSource.getMongodbDatabase(settings.get("mongodb").toString()).getCollection(
                    settings.get("collection").toString());
        } catch (UnknownHostException e) {
            throw new FwissrRuntimeException("Can not open mongodb connection", e);
        }
        return new MongodbSource(collection, options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SmarterMap fetchConf() {
        SmarterMap result = new SmarterMap();

        SmarterMap conf = new SmarterMap();
        for (DBObject doc : collection.find()) {
            Object key = doc.get("_id");
            Serializable value;
            if (doc.containsField("value")) {
                value = (Serializable) doc.get("value");
            } else {
                SmarterMap map = new SmarterMap(doc.toMap()).clone();
                map.remove("_id");
                map.freeze();
                value = map;
            }
            conf.put(key.toString(), value);
        }

        String[] path = collection.getName().split("\\.");
        mergeConf(result, conf, path, TOP_LEVEL_COLLECTIONS.contains(collection.getName()));

        return result;

    }

}

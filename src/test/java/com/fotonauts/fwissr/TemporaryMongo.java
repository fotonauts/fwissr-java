package com.fotonauts.fwissr;

import org.junit.rules.ExternalResource;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class TemporaryMongo extends ExternalResource {

    private MongodExecutable _mongodExe;
    private MongodProcess _mongod;
    private MongoClient mongo;

    @Override
    public void before() throws Exception {

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        int port = Network.getFreeServerPort();
        _mongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(port, false)).build());
        _mongod = _mongodExe.start();

        mongo = new MongoClient("127.0.0.1", port);
    }

    @Override
    public void after() {
        try {
            _mongod.stop();
            _mongodExe.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MongoClient getClient() {
        return mongo;
    }
}

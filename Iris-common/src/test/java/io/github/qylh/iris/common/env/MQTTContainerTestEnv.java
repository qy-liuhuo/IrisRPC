package io.github.qylh.iris.common.env;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.stream.Stream;

public class MQTTContainerTestEnv {

    static final Logger logger = LogManager.getLogger(MQTTContainerTestEnv.class);

    public static final GenericContainer mqttBrokerContainer = new GenericContainer("emqx/emqx:5.8.6")
            .withExposedPorts(1883, 18083);

    @BeforeClass
    public static void before(){
        logger.info("Starting containers...");
        Startables.deepStart(Stream.of(mqttBrokerContainer)).join();
        logger.info("Containers are started.");
    }

}

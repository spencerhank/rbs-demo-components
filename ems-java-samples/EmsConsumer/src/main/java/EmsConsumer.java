// Copyright 2017-2023 Cloud Software Group, Inc. All Rights Reserved.

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.naming.TibjmsContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class EmsConsumer implements ExceptionListener {
    /*-----------------------------------------------------------------------
     * Parameters
     *----------------------------------------------------------------------*/

    String serverUrl = null;
    String userName = null;
    String password = null;
    String destName = "demo_tcm";
    boolean useTopic = true;
    int ackMode = Session.AUTO_ACKNOWLEDGE;

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection connection = null;
    Session session = null;
    MessageConsumer msgConsumer = null;
    Destination destination = null;
    Logger logger;
    int received = 0;

    /*---------------------------------------------------------------------
     * onException
     *---------------------------------------------------------------------*/
    public void onException(JMSException e) {
        /* print the connection exception status */
        logger.warn("ExceptionListener: " + e.getErrorCode() + " : " + e.getMessage());
    }

    public static Map<String, String> parseAppConfig(String filename) throws Exception {
        Map<String, String> options = new HashMap<String, String>();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
            // Parse the tcm-config.yaml file as name: value options. This can be
            // replaced with a YAML parsing library for full features.
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String[] nameValue = line.split(":", 2);
                    if (nameValue.length != 2) {
                        throw new Exception(line + " did not split into a name/value pair");
                    }
                    String name = nameValue[0].trim();
                    String value = nameValue[1].trim();
                    options.put(name, value);
                }
            }
        }

        if (options.get("tcm_authentication_key") == null
                || options.get("tcm_authentication_key").equals("")) {
            throw new Exception("The tcm_authentication_key option is required to connect to TCM");
        }
        if (options.get("ems_username") == null || options.get("ems_username").equals("")) {
            throw new Exception("The ems_username option is required to connect to TCM");
        }
        if (options.get("ems_server") == null || options.get("ems_server").equals("")) {
            throw new Exception("The ems_server option is required to connect to TCM");
        }
        return options;
    }

    public EmsConsumer(CommandLine cmd) throws Exception {

        logger = LogManager.getRootLogger();
        logger.info("#");
        logger.info("# EmsConsumer");
        logger.info("#");

        Map<String, String> options =
                EmsConsumer.parseAppConfig(cmd.getOptionValue("f", "tcm-config.yaml"));
        serverUrl = options.get("ems_server");
        userName = options.get("ems_username");
        password = options.get("tcm_authentication_key");
        if (cmd.hasOption("queue")) {
            useTopic = false;
            destName = cmd.getOptionValue("queue", destName);
        } else {
            destName = cmd.getOptionValue("topic", destName);
        }
        if (cmd.hasOption("ackmode")) {
            String mode = cmd.getOptionValue("ackmode");
            if (mode.compareTo("AUTO") == 0)
                ackMode = Session.AUTO_ACKNOWLEDGE;
            else if (mode.compareTo("CLIENT") == 0)
                ackMode = Session.CLIENT_ACKNOWLEDGE;
            else if (mode.compareTo("DUPS_OK") == 0)
                ackMode = Session.DUPS_OK_ACKNOWLEDGE;
            else if (mode.compareTo("EXPLICIT_CLIENT") == 0)
                ackMode = Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE;
            else if (mode.compareTo("EXPLICIT_CLIENT_DUPS_OK") == 0)
                ackMode = Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE;
            else if (mode.compareTo("NO") == 0)
                ackMode = Tibjms.NO_ACKNOWLEDGE;
            else {
                throw new Exception("Unrecognized -ackmode: " + mode);
            }
        }
        run(cmd);
    }

    /*-----------------------------------------------------------------------
     * run
     *----------------------------------------------------------------------*/
    void run(CommandLine cmd) throws Exception {

        Tibjms.setExceptionOnFTEvents(true);

        ConnectionFactory factory = null;
        Hashtable<String, Object> env = new Hashtable<String, Object>();

        if (cmd.hasOption("jndi")) {
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
            env.put(Context.PROVIDER_URL, serverUrl);
            env.put(Context.URL_PKG_PREFIXES, "com.tibco.tibjms.naming");
            if (System.getenv("TCM_NO_TLS") == null)
                env.put(TibjmsContext.SECURITY_PROTOCOL, "ssl");
            env.put(Context.SECURITY_PRINCIPAL, userName);
            env.put(Context.SECURITY_CREDENTIALS, password);
            Context context = new InitialContext(env);
            // use SSL to lookup the ConnectionFactory
            Object factory1 = context.lookup("SSLFTGenericConnectionFactory");
            if (!(factory1 instanceof ConnectionFactory))
                throw new NamingException(
                        "Expected ConnectionFactory but found: " + factory1.getClass().getName());
            factory = (ConnectionFactory) factory1;
        } else {
            TibjmsConnectionFactory factory1 = new com.tibco.tibjms.TibjmsConnectionFactory(
                    serverUrl, cmd.getOptionValue("id", "EmsConsumer"), env);
            factory1.setConnAttemptCount(200);
            factory1.setConnAttemptDelay(850);
            factory1.setConnAttemptTimeout(20000);
            factory1.setReconnAttemptCount(200);
            factory1.setReconnAttemptDelay(850);
            factory1.setReconnAttemptTimeout(20000);
            factory = (ConnectionFactory) factory1;
        }

        /* create the connection */
        connection = factory.createConnection(userName, password);
        if (cmd.hasOption("jndi")) {
            connection.setClientID(cmd.getOptionValue("id", "EmsConsumer"));
        }

        /* create the session */
        session = connection.createSession(ackMode);

        /* set the exception listener */
        connection.setExceptionListener(this);

        /* create the destination */
        if (useTopic)
            destination = session.createTopic(destName);
        else
            destination = session.createQueue(destName);

        /* create the consumer */
        msgConsumer = session.createConsumer(destination);

        /* start the connection */
        connection.start();

        logger.info("Waiting for messages ...");

        int count = Integer.parseInt(cmd.getOptionValue("c", "-1"));
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        Runnable task = () -> {
            try {
                while (true) {
                    if (latch.getCount() <= 0)
                        return;
                    MapMessage msg = (MapMessage) msgConsumer.receive();
                    if (msg == null) {
                        break;
                    }
                    if (ackMode == Session.CLIENT_ACKNOWLEDGE
                            || ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE
                            || ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE) {
                        msg.acknowledge();
                    }
                    Instant now = Instant.now();
                    int sqn = msg.getInt("Sqn");
                    long time = msg.getLong("Time");
                    long cur = (now.getEpochSecond() * 1000000000L) + now.getNano();
                    long delta = cur - time;
                    logger.info("Received Sqn " + sqn + " after " + delta + "ns");
                    ++received;
                    if (count > 0 && received >= count) {
                        break;
                    }
                }
            } catch (JMSException e) {
                logger.error("Delivery failed " + e.toString());
            }
            executor.shutdown();
            latch.countDown();
        };

        executor.schedule(task, 0, TimeUnit.SECONDS);

        // schedule timeout if needed
        long timeout = Long.parseLong(cmd.getOptionValue("t", "-1"));
        if (timeout > 0) {
            task = () -> {
                logger.info("Received " + received + " messages before timeout");
                executor.shutdown();
                latch.countDown();
            };
            executor.schedule(task, timeout, TimeUnit.SECONDS);
        }

        latch.await();

        if (count > 0 && received < count) {
            logger.info("Received only " + received + " of " + count + " messages");
        } else {
            logger.info("Received " + received + " messages");
        }

        /* close the connection */
        connection.close();
    }

    public static void configureLogger() {
        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();
        AppenderComponentBuilder console = builder.newAppender("console", "Console");
        console.add(builder.newLayout("PatternLayout").addAttribute("pattern",
                "%d{yyyy/MM/dd HH:mm:ss} [%t] %-5level: %msg%n%throwable"));
        builder.add(console);
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.INFO);
        rootLogger.add(builder.newAppenderRef("console"));
        builder.add(rootLogger);
        Configurator.initialize(builder.build());
    }

    public static void main(String[] args) {

        configureLogger();

        Options options = new Options();
        options.addOption(Option.builder("f").longOpt("config").argName("config-file").hasArg()
                .desc("the path to a configuration yaml").build());
        options.addOption(Option.builder("c").longOpt("count").argName("count").hasArg()
                .desc("the number of messages received before exiting").build());
        options.addOption(Option.builder("t").longOpt("timeout").argName("timeout").hasArg()
                .desc("the duration before exiting (seconds)").build());
        options.addOption(Option.builder("topic").argName("topic").hasArg()
                .desc("the topic to consume").build());
        options.addOption(Option.builder("queue").argName("queue").hasArg()
                .desc("the queue to consume").build());
        options.addOption(Option.builder("id").argName("client-id").hasArg()
                .desc("client identifier").build());
        options.addOption(Option.builder("ackmode").hasArg().desc(
                "acknowledge mode, default is AUTO (other options: CLIENT, DUPS_OK, NO,EXPLICIT_CLIENT and EXPLICIT_CLIENT_DUPS_OK)")
                .build());
        options.addOption(Option.builder("jndi").argName("jndi")
                .desc("use JNDI to lookup connection factory").build());
        options.addOption("h", "help", false, "this usage");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp(120, "EmsConsumer", "", options, "", true);
                System.exit(0);
            }
            new EmsConsumer(cmd);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

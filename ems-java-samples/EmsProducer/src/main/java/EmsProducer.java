// Copyright 2017-2023 Cloud Software Group, Inc. All Rights Reserved.

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
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

public class EmsProducer implements ExceptionListener {
    /*-----------------------------------------------------------------------
     * Parameters
     *----------------------------------------------------------------------*/

    String serverUrl = null;
    String userName = null;
    String password = null;
    String destName = "demo_tcm";
    Vector<String> data = new Vector<String>();
    boolean useTopic = true;

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection connection = null;
    Session session = null;
    MessageProducer msgProducer = null;
    Destination destination = null;
    Logger logger;
    int sent = 0;

    /*---------------------------------------------------------------------
     * onException
     *---------------------------------------------------------------------*/
    public void onException(JMSException e) {
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

    public EmsProducer(CommandLine cmd) throws Exception {

        logger = LogManager.getRootLogger();
        logger.info("#");
        logger.info("# EmsProducer");
        logger.info("#");

        Tibjms.setExceptionOnFTEvents(true);

        Map<String, String> options =
                EmsProducer.parseAppConfig(cmd.getOptionValue("f", "tcm-config.yaml"));
        serverUrl = options.get("ems_server");
        userName = options.get("ems_username");
        password = options.get("tcm_authentication_key");
        if (cmd.hasOption("queue")) {
            useTopic = false;
            destName = cmd.getOptionValue("queue", destName);
        } else {
            destName = cmd.getOptionValue("topic", destName);
        }

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
//            TibjmsConnectionFactory factory1 = new com.tibco.tibjms.TibjmsConnectionFactory(
//                    serverUrl, cmd.getOptionValue("id", "EmsProducer"), env);
            SolConnectionFactory factory1 = SolJmsUtility.createConnectionFactory();
            factory1.setHost(serverUrl);
            factory1.setUsername(userName);
            factory1.setPassword(password);
            factory1.setVPN("default");
//            factory1.setConnAttemptCount(200);
//            factory1.setConnAttemptDelay(850);
//            factory1.setConnAttemptTimeout(20000);
//            factory1.setReconnAttemptCount(200);
//            factory1.setReconnAttemptDelay(850);
//            factory1.setReconnAttemptTimeout(20000);
            factory = (ConnectionFactory) factory1;
        }

        connection = factory.createConnection(userName, password);
        if (cmd.hasOption("jndi")) {
            connection.setClientID(cmd.getOptionValue("id", "EmsProducer"));
        }
        connection.setExceptionListener(this);


        /* create the session */
//        session = connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        /* create the destination */
        if (useTopic)
            destination = session.createTopic(destName);
        else
            destination = session.createQueue(destName);

        /* create the producer */
        msgProducer = session.createProducer(destination);

        int count = Integer.parseInt(cmd.getOptionValue("c", "-1"));
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        Runnable task = () -> {
            if (latch.getCount() <= 0)
                return;
            try {
                MapMessage msg = session.createMapMessage();
                Instant now = Instant.now();
                long time = (now.getEpochSecond() * 1000000000L) + now.getNano();
                msg.setInt("Sqn", sent);
                msg.setLong("Time", time);
                msgProducer.send(msg);
                logger.info("Delivered " + msg.toString());
                ++sent;
                if (count > 0 && sent >= count) {
                    executor.shutdown();
                    latch.countDown();
                }
            } catch (JMSException e) {
                logger.error("Delivery failed " + e.toString());
                executor.shutdown();
                latch.countDown();
            }
        };

        // schedule periodic sending
        int ivl = Integer.parseInt(cmd.getOptionValue("i", "1000"));
        executor.scheduleAtFixedRate(task, 0, ivl, TimeUnit.MILLISECONDS);

        // schedule timeout if needed
        long timeout = Long.parseLong(cmd.getOptionValue("t", "-1"));
        ScheduledFuture<?> timeoutFuture = null;
        if (timeout > 0) {
            task = () -> {
                logger.info("Sent " + sent + " messages before timeout");
                executor.shutdown();
                latch.countDown();
            };
            timeoutFuture = executor.schedule(task, timeout, TimeUnit.SECONDS);
        }

        latch.await();
        if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
        }
        connection.close();
        logger.info("Delivered " + sent + " messages");
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
                .desc("the number of messages to send before exiting").build());
        options.addOption(Option.builder("i").longOpt("interval").argName("interval").hasArg()
                .desc("the interval between sends (milliseconds)").build());
        options.addOption(Option.builder("t").longOpt("timeout").argName("timeout").hasArg()
                .desc("the duration before exiting (seconds)").build());
        options.addOption(
                Option.builder("topic").argName("topic").hasArg().desc("topic to send on").build());
        options.addOption(
                Option.builder("queue").argName("queue").hasArg().desc("queue to send on").build());
        options.addOption(Option.builder("id").argName("client-id").hasArg()
                .desc("client identifier").build());
        options.addOption(Option.builder("jndi").argName("jndi")
                .desc("use JNDI to lookup connection factory").build());
        options.addOption("h", "help", false, "this usage");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp(120, "EmsProducer", "", options, "", true);
                System.exit(0);
            }
            new EmsProducer(cmd);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

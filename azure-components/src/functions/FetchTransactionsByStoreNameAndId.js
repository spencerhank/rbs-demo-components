const { app } = require('@azure/functions');
const solace = require('solclientjs');
const azure = require("azure-storage");

const tableSvc = azure.createTableService(process.env["AzureWebJobsStorage"]);

var TopicPublisher = function (solaceModule, topicName, payload) {
    'use strict';
    var solace = solaceModule;
    var publisher = {};
    publisher.session = null;
    publisher.topicName = topicName;

    // Logger
    publisher.log = function (line) {
        var now = new Date();
        var time = [('0' + now.getHours()).slice(-2), ('0' + now.getMinutes()).slice(-2),
        ('0' + now.getSeconds()).slice(-2)];
        var timestamp = '[' + time.join(':') + '] ';
        console.log(timestamp + line);
    };

    publisher.log('\n*** Publisher to topic "' + publisher.topicName + '" is ready to connect ***');


    // Establishes connection to Solace PubSub+ Event Broker
    publisher.connect = function (argv) {
        // if (publisher.session !== null) {
        //     publisher.log('Already connected and ready to publish.');
        //     return;
        // }
        // extract params
        // if (argv.length < (2 + 3)) { // expecting 3 real arguments
        //     publisher.log('Cannot connect: expecting all arguments' +
        //         ' <protocol://host[:port]> <client-username>@<message-vpn> <client-password>.\n' +
        //         'Available protocols are ws://, wss://, http://, https://, tcp://, tcps://');
        //     process.exit();
        // }
        // var hosturl = argv.slice(2)[0];
        // publisher.log('Connecting to Solace PubSub+ Event Broker using url: ' + hosturl);
        // var usernamevpn = argv.slice(3)[0];
        // var username = usernamevpn.split('@')[0];
        // publisher.log('Client username: ' + username);
        // var vpn = usernamevpn.split('@')[1];
        // publisher.log('Solace PubSub+ Event Broker VPN name: ' + vpn);
        // var pass = argv.slice(4)[0];
        // create session
        try {
            publisher.session = solace.SolclientFactory.createSession({
                // solace.SessionProperties
                url: process.env["SOLACE_URL"],
                vpnName: process.env["SOLACE_VPN"],
                userName: process.env["SOLACE_USERNAME"],
                password: process.env["SOLACE_PW"],
            });
        } catch (error) {
            publisher.log(error.toString());
        }
        // define session event listeners
        publisher.session.on(solace.SessionEventCode.UP_NOTICE, function (sessionEvent) {
            publisher.log('=== Successfully connected and ready to publish messages. ===');
            publisher.publish();
            publisher.exit();
        });
        publisher.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, function (sessionEvent) {
            publisher.log('Connection failed to the message router: ' + sessionEvent.infoStr +
                ' - check correct parameter values and connectivity!');
        });
        publisher.session.on(solace.SessionEventCode.DISCONNECTED, function (sessionEvent) {
            publisher.log('Disconnected.');
            if (publisher.session !== null) {
                publisher.session.dispose();
                publisher.session = null;
            }
        });
        // connect the session
        try {
            publisher.session.connect();
        } catch (error) {
            publisher.log(error.toString());
        }
    };

    // Publishes one message
    publisher.publish = function () {
        if (publisher.session !== null) {
            var messageText = 'Sample Message';
            var message = solace.SolclientFactory.createMessage();
            message.setDestination(solace.SolclientFactory.createTopicDestination(publisher.topicName));
            message.setBinaryAttachment(messageText);
            message.setDeliveryMode(solace.MessageDeliveryModeType.PERSISTENT);
            publisher.log('Publishing message "' + messageText + '" to topic "' + publisher.topicName + '"...');
            try {
                publisher.session.send(message);
                publisher.log('Message published.');
            } catch (error) {
                publisher.log(error.toString());
            }
        } else {
            publisher.log('Cannot publish because not connected to Solace PubSub+ Event Broker.');
        }
    };

    publisher.exit = function () {
        publisher.disconnect();
        setTimeout(function () {
            console.log("Last Disconnect")
            return {
                status: 200,
                body: "Success"
            }
        }, 1000); // wait for 1 second to finish
    };

    // Gracefully disconnects from Solace PubSub+ Event Broker
    publisher.disconnect = function () {
        publisher.log('Disconnecting from Solace PubSub+ Event Broker...');
        if (publisher.session !== null) {
            try {
                publisher.session.disconnect();
            } catch (error) {
                publisher.log(error.toString());
            }
        } else {
            publisher.log('Not connected to Solace PubSub+ Event Broker.');
        }
    };

    return publisher;
};

app.http('FetchTransactionsByStoreNameAndId', {
    methods: ['GET', 'POST'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        // TODO: get store and store id from payload, add reply to header for response

        const tableQuery = new azure.TableQuery().top(10);
        var options = {
            payloadFormat: "application/json;odata=nometadata"
        }

        const result = await new Promise((resolve, reject) => {
            tableSvc.queryEntities("Transactions", tableQuery, null, this.options,
                (error, result) => {
                    if (error) {
                        reject(error);
                    } else {
                        resolve(result);
                    }
                });
        }, this);
        context.log(result.entries);



        context.log(`Http function processed request for url "${request.url}"`);
        try {
            var factoryProps = new solace.SolclientFactoryProperties();
            factoryProps.profile = solace.SolclientFactoryProfiles.version10;
            solace.SolclientFactory.init(factoryProps);
            var publisher = {};

            var publisher = new TopicPublisher(solace, 'tutorial/topic', 'testPayload');


            publisher.connect();

            process.stdin.resume();


            // const name = request.query.get('name') || await request.text() || 'world';

            // return {
            //     statu: 204,
            //     body: 'Request Received'
            // }
        } catch (error) {
            context.error(error);

        }

    }
});
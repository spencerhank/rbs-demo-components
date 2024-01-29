const { app } = require('@azure/functions');
const { TableClient, AzureSASCredential } = require("@azure/data-tables");
const solace = require('solclientjs');

var factoryProps = new solace.SolclientFactoryProperties();
factoryProps.profile = solace.SolclientFactoryProfiles.version10;
solace.SolclientFactory.init(factoryProps);

var TopicPublisher = function (solaceModule, topicName, payload) {
    'use strict';
    var solace = solaceModule;
    var publisher = {};
    publisher.session = null;
    publisher.topicName = topicName;
    publisher.payload = payload;

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
            var message = solace.SolclientFactory.createMessage();
            message.setDestination(solace.SolclientFactory.createTopicDestination(publisher.topicName));
            message.setSdtContainer(solace.SDTField.create(solace.SDTFieldType.STRING, JSON.stringify(publisher.payload)));
            message.setDeliveryMode(solace.MessageDeliveryModeType.DIRECT);
            publisher.log('Publishing message "' + publisher.payload + '" to topic "' + publisher.topicName + '"...');
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


app.http('UpsertTransactions', {
    methods: ['POST'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        try {
            const stringPayload = await request.text();
            const transaction = JSON.parse(stringPayload).transaction;
            context.log(transaction);


            let entityToUpsert = {
                partitionKey: 'PickupOrders',
                rowKey: transaction.transactionId,
                action: transaction.transactionAction,
                paymentInformation: JSON.stringify(transaction.paymentInformation),
                products: JSON.stringify(transaction.products),
                purchaseChannel: transaction.purchaseChannel.channel,
                storeName: transaction.purchaseChannel.storeName,
                storeId: transaction.purchaseChannel.storeId,
                rewardsInfo: JSON.stringify(transaction.rewardsInfo),
                timestamp: transaction.timeStamp,
                ID: transaction.transactionId,
            }

            // context.log(entityToUpsert);
            const client = new TableClient(`https://hspencerrbsdemostorage.table.core.windows.net`,
                'PickupOrders',
                new AzureSASCredential(process.env["pickUpTableSasTokenForUpdates"]));

            await client.upsertEntity(
                entityToUpsert,
                "Merge"
            );

            const upserttedEntity = await client.getEntity(entityToUpsert.partitionKey, entityToUpsert.rowKey);

            // TODO publish back to fulfillment app
            var publisher = new TopicPublisher(solace, `pickup/task/${entityToUpsert.action}/${entityToUpsert.storeId}/${entityToUpsert.rowKey}/SYSTEM`, upserttedEntity);

            publisher.connect();

            process.stdin.resume();

            context.log("Upsertted Entity: ", upserttedEntity);

            // return {
            //     statu: 200,
            //     body: 'OK'
            // }
        } catch (error) {
            context.error(error);
            return {
                status: 500,
                body: `${error}`
            }

        }

    }
});
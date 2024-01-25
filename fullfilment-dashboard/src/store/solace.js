import { ref } from 'vue';
import { defineStore } from 'pinia'
import solace, { MessageOutcome } from 'solclientjs'

export const useSolaceStore = defineStore('solaceStore', () => {
    const eventHandlers = ref({});
    const readyToSend = ref(false);


    let factorProps = new solace.SolclientFactoryProperties();
    factorProps.profile = solace.SolclientFactoryProfiles.version10;
    solace.SolclientFactory.init(factorProps);

    const solaceClient = {}
    solaceClient.session = null;

    function connect() {
        if (solaceClient.session !== null) {
            console.log('Already connected and ready to subscribe')
        }

        try {
            solaceClient.session = solace.SolclientFactory.createSession({
                url: 'wss://mr-connection-rzux89z11fn.messaging.solace.cloud:443',
                vpnName: 'dr-test',
                userName: 'solace-cloud-client',
                password: 'prqf38pq8jetg1pfqdoof1t2l5',
                noLocal: true
            });
        } catch (error) {
            console.log(error)
        }

        solaceClient.session.on(solace.SessionEventCode.UP_NOTICE, function (sessionEvent) {
            console.log('=== Successfully connected and ready to subscribe. ===')
            consume();
        });

        solaceClient.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, function (sessionEvent) {
            console.log('Connection failed to the message router: ' + sessionEvent.infoStr +
                ' - check correct parameter values and connectivity!');
        });
        solaceClient.session.on(solace.SessionEventCode.DISCONNECTED, function (sessionEvent) {
            console.log('Disconnected.');
            solaceClient.subscribed = false;
            if (solaceClient.session !== null) {
                solaceClient.session.dispose();
                solaceClient.session = null;
            }
        });

        solaceClient.session.on(solace.SessionEventCode.SUBSCRIPTION_ERROR, function (sessionEvent) {
            console.log('Cannot subscribe to topic: ' + sessionEvent.correlationKey);
        });
        //    For direct subscriptions
        //    solaceClient.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, function (sessionEvent) {
        //      if (solaceClient.subscribed) {
        //        solaceClient.subscribed = false;
        //        console.log('Successfully unsubscribed from topic: ' + sessionEvent.correlationKey);
        //      } else {
        //        solaceClient.subscribed = true;
        //        console.log('Successfully subscribed to topic: ' + sessionEvent.correlationKey);
        //        console.log('=== Ready to receive messages. ===');
        //      }
        //    });
        // define message event listener
        solaceClient.session.on(solace.SessionEventCode.MESSAGE, function (message) {
            console.log('Received message via event consumer on topic: "' + message.getDestination().getName());
            try {
                console.log('User property map: ', message.getUserPropertyMap());
                //  For Request/Reply
                for (const [callbackName, callback] of Object.entries(eventHandlers.value)) {
                    if (callbackName == message.getUserPropertyMap().getField('response-handler').getValue()) {
                        callback(message)
                    }
                }
            } catch (error) {
                console.log('No usermap');
                for (const [topic, callback] of Object.entries(eventHandlers.value)) {
                    const regex = topic.replace('*', '.*').replace('>', '.*')
                    let destinationName = message.getDestination().getName();
                    let found = destinationName.match(regex);
                    if (found) {
                        callback(message)
                    }
                }
            }



        });
        // connect the session
        try {
            solaceClient.session.connect();
        } catch (error) {
            console.log(error.toString());
        }

    }

    function consume(queueName) {
        if (solaceClient.session == null) {
            connect();
        } else {
            console.log('Start Consuming');
            try {

                let messageConsumer = solaceClient.session.createMessageConsumer({
                    queueDescriptor: { name: queueName, type: solace.QueueType.QUEUE, durable: false },
                    acknowledgeMode: solace.MessageConsumerAcknowledgeMode.CLIENT,
                    createIfMissing: true

                })
                messageConsumer.on(solace.MessageConsumerEventName.UP, function () {
                    // TODO: determine what next
                    messageConsumer.consuming = true;
                    readyToSend.value = true;
                    // messageConsumer.addSubscription(solaceClient.session.getSessionProperties()['_p2pInboxInUse']);
                    console.log('=== Ready to receive messages. ===');
                });
                messageConsumer.on(solace.MessageConsumerEventName.CONNECT_FAILED_ERROR, function () {
                    messageConsumer.consuming = false;
                    console.log('=== Error: the message consumer could not bind to queue "' + queueName +
                        '" ===\n   Ensure this queue exists on the message router vpn');
                    messageConsumer.exit();
                });
                messageConsumer.on(solace.MessageConsumerEventName.DOWN, function () {
                    messageConsumer.consuming = false;
                    console.log('=== The message consumer is now down ===');
                });
                messageConsumer.on(solace.MessageConsumerEventName.DOWN_ERROR, function () {
                    messageConsumer.consuming = false;
                    console.log('=== An error happened, the message consumer is down ===');
                });
                messageConsumer.on(solace.MessageConsumerEventName.MESSAGE, function (message) {
                    console.log('Received message via event consumer on queue: "' + message.getDestination().getName());
                    //  For subscriptions only
                    // for (const [topic, callback] of Object.entries(eventHandlers.value)) {
                    //     const regex = topic.replace('*', '.*').replace('>', '.*')
                    //     let destinationName = message.getDestination().getName();
                    //     let found = destinationName.match(regex);
                    //     if (found) {
                    //         callback(message)
                    //     }
                    // }
                });
                messageConsumer.connect();
                solaceClient.eventConsumer = messageConsumer;
            } catch (error) {
                console.log(error.toString());
            }
        }

    }

    function addSubscriptionHandler(subscriptionTopic, responseHandler) {
        eventHandlers.value[subscriptionTopic] = responseHandler;
        solaceClient.session.subscribe(
            solace.SolclientFactory.createTopicDestination(subscriptionTopic),
            true,
            subscriptionTopic,
            10000
        );
    }

    function removeSubscriptionHandler(subscriptionTopic) {
        delete eventHandlers.value[subscriptionTopic];
        solaceClient.eventConsumer.unsubscribe(
            solace.SolclientFactory.createTopicDestination(subscriptionTopic),
            true,
            subscriptionTopic,
            10000
        );
    }

    function publishMessage(topic, payload) {
        let message = solace.SolclientFactory.createMessage();
        message.setDestination(solace.SolclientFactory.createTopicDestination(topic));
        message.setSdtContainer(solace.SDTField.create(solace.SDTFieldType.STRING, JSON.stringify(payload)));
        message.setDMQEligible(true);
        message.setTimeToLive(15000);
        console.log('Publishing message {} on topic {}', message, topic);
        solaceClient.session.send(message);

    }

    function sendRequest(payload, topic, responsehandler) {
        let message = solace.SolclientFactory.createMessage();
        let userPropertyMap = new solace.SDTMapContainer();
        userPropertyMap.addField('response-handler', solace.SDTFieldType.STRING, responsehandler.name);
        message.setDestination(solace.SolclientFactory.createTopicDestination(topic));
        message.setUserPropertyMap(userPropertyMap);
        payload['replyTo'] = solaceClient.session.getSessionProperties()['_p2pInboxInUse'];
        message.setSdtContainer(solace.SDTField.create(solace.SDTFieldType.STRING, JSON.stringify(payload)));
        message.setDMQEligible(true);
        message.setTimeToLive(15000);

        eventHandlers.value[responsehandler.name] = responsehandler;
        solaceClient.session.send(message);
    }

    function disconnect(queueName) {
        if (solaceClient.session != null) {
            if (solaceClient.eventConsumer != null && solaceClient.eventConsumer.consuming) {
                solaceClient.eventConsumer.consuming = false;
                readyToSend.value = false;
                console.log('Disconnecting consumption from queue: ' + queueName);
                try {
                    solaceClient.eventConsumer.disconnect();
                    solaceClient.eventConsumer.dispose();
                    solaceClient.session.disconnect();
                    solaceClient.session.dispose();
                    solaceClient.eventConsumer = null;
                } catch (error) {
                    console.log(error.toString());
                }
            } else {
                console.log('Cannot disconnect the consumer because it is not connected to queue "' +
                    queueName + '"');
            }
        } else {
            console.log('Cannot disconnect the consumer because not connected to Solace PubSub+ Event Broker.');
        }

    }

    return {
        readyToSend,
        connect,
        disconnect,
        addSubscriptionHandler,
        removeSubscriptionHandler,
        sendRequest,
        publishMessage
    }
})
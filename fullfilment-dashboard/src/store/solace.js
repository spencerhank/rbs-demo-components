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
            // TODO: update connection details
            solaceClient.session = solace.SolclientFactory.createSession({
                url: 'wss://mr-connection-rzux89z11fn.messaging.solace.cloud:443',
                vpnName: 'dr-test',
                userName: 'solace-cloud-client',
                password: 'prqf38pq8jetg1pfqdoof1t2l5'
            });
        } catch (error) {
            console.log(error)
        }

        solaceClient.session.on(solace.SessionEventCode.UP_NOTICE, function (sessionEvent) {
            console.log('=== Successfully connected and ready to subscribe. ===')
            readyToSend.value = true;
        });

        solaceClient.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, function (sessionEvent) {
            console.log('Connection failed to the message router: ' + sessionEvent.infoStr +
                ' - check correct parameter values and connectivity!');
        });
        solaceClient.session.on(solace.SessionEventCode.DISCONNECTED, function (sessionEvent) {
            console.log('Disconnected.');
            solaceClient.subscribed = false;
            readyToSend.value = false;
            if (solaceClient.session !== null) {
                solaceClient.session.dispose();
                solaceClient.session = null;
            }
        });

        solaceClient.session.on(solace.SessionEventCode.SUBSCRIPTION_ERROR, function (sessionEvent) {
            console.log('Cannot subscribe to topic: ' + sessionEvent.correlationKey);
        });
        //    For direct subscriptions
        solaceClient.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, function (sessionEvent) {
            if (solaceClient.subscribed) {
                solaceClient.subscribed = false;
                console.log('Successfully unsubscribed from topic: ' + sessionEvent.correlationKey);
            } else {
                solaceClient.subscribed = true;
                console.log('Successfully subscribed to topic: ' + sessionEvent.correlationKey);
                console.log('=== Ready to receive messages. ===');
            }
        });
        // define message event listener
        solaceClient.session.on(solace.SessionEventCode.MESSAGE, function (message) {
            console.log('Received message on topic: "' + message.getDestination().getName());
            console.log('User property map: ', message.getUserPropertyMap());
            //  For subscriptions only
            for (const [callbackName, callback] of Object.entries(eventHandlers.value)) {
                if (callbackName == message.getUserPropertyMap().getField('response-handler').getValue()) {
                    callback(message)
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

    function addSubscriptionHandler(subscriptionTopic, responseHandler) {
        this.evantHandlers.value[subscriptionTopic] = responseHandler;
        solaceClient.session.subscribe(
            solace.SolclientFactory.createTopicDestination(subscriptionTopic),
            true,
            subscriptionTopic,
            10000
        );
    }

    function removeSubscriptionHandler(subscriptionTopic) {
        delete eventHandlers.value[subscriptionTopic];
        solaceClient.session.unsubscribe(
            solace.SolclientFactory.createTopicDestination(subscriptionTopic),
            true,
            subscriptionTopic,
            10000
        );
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
        if (solaceClient.session !== null) {
            if (eventHandlers.value != null) {
                console.log("Unsubscribing from topics");
                for (var key in eventHandlers.value) {
                    removeSubscriptionHandler(key);
                }
                eventHandlers.value = {};

            } else {
                console.log('No topic to unsubscribe From');
            }
            console.log('Disconnecting session: ' + queueName);
            try {
                solaceClient.session.disconnect();
            } catch (error) {
                console.log(error.toString());
            }
        } else {
            console.log('Not connected from the Solace PubSub+ Broker');
        }

    }

    return {
        readyToSend,
        connect,
        disconnect,
        addSubscriptionHandler,
        removeSubscriptionHandler,
        sendRequest,
    }
})
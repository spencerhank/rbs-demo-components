Send whole xml file as single kafka message
tr -d '\n' < kafka-samples/resources/order1.xml | /opt/homebrew/opt/kafka/bin/kafka-console-producer --broker-list localhost:29092 --topic ORDERS

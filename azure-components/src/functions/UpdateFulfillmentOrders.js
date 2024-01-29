const { app } = require('@azure/functions');
const { TableClient, AzureSASCredential } = require("@azure/data-tables");


app.http('UpdateFulfillmentOrders', {
    methods: ['POST'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        try {
            const stringPayload = await request.text();
            const updateRequest = JSON.parse(stringPayload);
            const order = updateRequest.order;
            // context.log(order);

            let entityToUpdate = {
                partitionKey: 'PickupOrders',
                rowKey: order.RowKey,
            }

            if (updateRequest.action == 'ASSIGNED') {
                entityToUpdate['assignedTo'] = updateRequest.order.assignedTo
                entityToUpdate['fulfillmentStatus'] = 'IN_PROGRESS'
            } else if (updateRequest.action == 'RELEASED') {
                entityToUpdate['assignedTo'] = ''
                entityToUpdate['fulfillmentStatus'] = 'NOT_STARTED'
            } else if (updateRequest.action == 'COMPLETED') {
                entityToUpdate['assignedTo'] = updateRequest.order.assignedTo
                entityToUpdate['fulfillmentStatus'] = 'COMPLETED'
            }

            context.log(entityToUpdate);
            const client = new TableClient(`https://hspencerrbsdemostorage.table.core.windows.net`,
                'PickupOrders',
                new AzureSASCredential(process.env["pickUpTableSasTokenForUpdates"]));

            await client.upsertEntity(
                entityToUpdate,
                "Merge"
            );
            context.log(`Http function processed request for url "${request.url}"`);



            return {
                statu: 200,
                body: 'OK'
            }
        } catch (error) {
            context.error(error);

        }

    }
});
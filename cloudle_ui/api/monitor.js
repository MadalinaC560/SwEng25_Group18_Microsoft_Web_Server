const fetch = require("node-fetch");
const { DefaultAzureCredential } = require("@azure/identity");

module.exports = async function (context, req) {
    try {
        const credential = new DefaultAzureCredential();
        const tokenResponse = await credential.getToken("https://management.azure.com/");

        const response = await fetch("https://management.azure.com/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/microsoft.insights/components/{resourceName}/metrics?api-version=2018-01-01", {
            headers: {
                "Authorization": `Bearer ${tokenResponse.token}`,
                "Content-Type": "application/json"
            }
        });

        const data = await response.json();
        context.res = { status: 200, body: data };
    } catch (error) {
        context.res = { status: 500, body: "Error fetching Azure Monitor data" };
    }
};

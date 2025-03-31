import { ApplicationInsights } from "@microsoft/applicationinsights-web";

const appInsights = new ApplicationInsights({
    config: {
        connectionString: process.env.NEXT_PUBLIC_AZURE_CONNECTION_STRING,
        enableAutoRouteTracking: true,
    },
});

appInsights.loadAppInsights();

export default appInsights;
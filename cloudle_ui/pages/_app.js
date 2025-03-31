import { useEffect } from "react";
import appInsights from "../utils/appInsights";

function App({ Component, pageProps }) {
    useEffect(() => {
        appInsights.trackPageView();
    }, []);

    return <Component {...pageProps} />;
}

export default App;
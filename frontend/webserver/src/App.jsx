import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Landing from "./pages/landing.jsx";
import AnalyticsPage from "./pages/analytics.jsx";
import AboutUs from "./pages/aboutUs.jsx";
import Navigation from "./components/navigationComponents/Navigation.jsx";
import NotFound from "./pages/notfoundpage.jsx";
import "./App.css";

function App() {
  return (
    <Router>
      <Navigation />
      <Routes>
        <Route path="/" element={<Landing />} /> {/* Default Page */}
        <Route path="/analytics" element={<AnalyticsPage />} />
        <Route path="/aboutUs" element={<AboutUs />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}

export default App;

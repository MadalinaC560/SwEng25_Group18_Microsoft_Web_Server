import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Landing from "./pages/landing.jsx";
import AnalyticsPage from "./pages/analytics.jsx";
import "./App.css";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Landing />} /> {/* Default Page */}
        <Route path="/analytics" element={<AnalyticsPage />} />{" "}
        {/* Post Log In Page */}
        <Route path="*" element={<Navigate to="/" />} />{" "}
        {/* Redirect unknown routes */}
      </Routes>
    </Router>
  );
}

export default App;

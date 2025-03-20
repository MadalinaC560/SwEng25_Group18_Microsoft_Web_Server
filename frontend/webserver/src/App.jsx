import { BrowserRouter as Router } from "react-router-dom";
import AnimatedRoutes from "./AnimatedRoutes.jsx";
import Navigation from "./components/navigationComponents/Navigation.jsx";

import "./App.css";
function App() {
  return (
    <Router>
      <Navigation />
      <AnimatedRoutes />
    </Router>
  );
}

export default App;

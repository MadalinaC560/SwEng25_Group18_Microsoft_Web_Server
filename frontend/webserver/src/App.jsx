import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Landing from './pages/landing.jsx';

function App() {

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Landing />} /> {/* Default Page */}
      </Routes>
    </Router>
  )
}

/* App.jsx needed to configure routing to other pages */

export default App

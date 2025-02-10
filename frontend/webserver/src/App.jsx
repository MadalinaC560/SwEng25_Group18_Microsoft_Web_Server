import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from './pages/login.jsx';

function App() {

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} /> {/* Default Page */}
      </Routes>
    </Router>
  )
}

/* App.jsx needed to configure routing to other pages */

export default App

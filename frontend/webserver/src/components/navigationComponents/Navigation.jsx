import { NavLink, Link, useLocation } from "react-router-dom";
import "../../css/navigation.css";
import cloudl from "../../images/cloudl.png";

function Navigation() {
  const location = useLocation(); // Get the current location object

  return (
    <div className="header">
      {/* Logo link outside the nav */}
      <Link to="/home" className="logo-link">
        <img src={cloudl} alt="Logo" className="logo" />
      </Link>

      {/* Conditionally render the navigation bar */}

      <nav>
        <NavLink to="/" end>
          Landing Page
        </NavLink>
        {location.pathname !== "/" && (
          <NavLink to="/analytics">Analytics</NavLink>
        )}
        <NavLink to="/aboutUs">About Us</NavLink>
      </nav>
    </div>
  );
}

export default Navigation;

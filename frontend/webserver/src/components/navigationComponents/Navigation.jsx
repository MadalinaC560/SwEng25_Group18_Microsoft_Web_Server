import { NavLink, Link } from "react-router-dom";
import "../../css/navigation.css";
import cloudl from "../../images/cloudl.png";

function Navigation() {
  return (
    <div className="header">
      {/* Logo link outside the nav */}
      <Link to="/" className="logo-link">
        <img src={cloudl} alt="Logo" className="logo" />
      </Link>

      {/* Navigation links inside the nav */}
      <nav>
        <NavLink to="/" end>
          Landing Page
        </NavLink>
        <NavLink to="/analytics">Analytics</NavLink>
        <NavLink to="/aboutUs">About Us</NavLink>
      </nav>
    </div>
  );
}

export default Navigation;

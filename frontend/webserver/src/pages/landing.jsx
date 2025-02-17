import React, { useEffect } from "react";
import cloudl from "../images/cloudl.png";
import "../css/landing.css";
import { useNavigate } from "react-router-dom";

function Landing() {
  const handleLogIn = () => {
    alert("Logged In");
  };

  const handleSignUp = () => {
    alert("Signed Up");
  };

  console.log("Landing component rendered");

  return (
    <div className="landing">
      <img src={cloudl} width={200} />
      <h1 className="landing-title">Welcome to Cloudle</h1>
      <h2>For all your web hosting needs!</h2>
      <button className="land-button" type="button" onClick={handleLogIn}>
        Log In
      </button>
      <button className="land-button" type="button" onClick={handleSignUp}>
        Sign Up
      </button>
    </div>
  );
}

export default Landing;

import { useState } from 'react';
import { Link } from "react-router-dom";

function Login () {

    return (
        <> 
        <div classNameName='header'>
        <nav className="nav-links-left">
            <a href="#Microsoft">
                <img src="https://mailmeteor.com/logos/assets/PNG/Microsoft_Logo_512px.png" alt="Microsoft"/>
            </a>
            <a href="#Trinity">
                <img src="tcd.png" alt="TCD"/>
            </a>
        </nav>

        <h1>Azure Cloud Hosting</h1>
    
        <nav classNameName="nav-links-right">
            <a href="#about">About Us</a>
            <a href="#contact">Contact</a>
        </nav>
        </div>
        
        <div classNameName='login-container'>
            
            <input type="text" classNameName="input-field" id="input-field" placeholder="Username / Email"/>
            <input type="password" className="input-field" id="pass-field" placeholder="Password"/>
            
            <div className="actions">
                <a href="#forgot">Forgot Password?</a>
                <a href="#signup">Sign Up</a>
            </div>
            
            <button className="login-btn" id="login-btn">Log In</button>
        </div>
        </>
    )
}

export default Login
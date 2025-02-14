import { useState } from 'react';
import { Link, useNavigate } from "react-router-dom";
import { doCreateUserWithEmailAndPassword, doSignInWithGoogle } from '../firebase/auth';

const Register = () => {
    const navigate = useNavigate();
    
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isSigningUp, setIsSigningUp] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const onSubmit = async (e) => {
        e.preventDefault();
        if (!isSigningUp) {
            setIsSigningUp(true);
            try {
                await doCreateUserWithEmailAndPassword(email, password);
                console.log("User registered successfully!");
                navigate("/login"); // Redirect to login after signup
            } catch (err) {
                setErrorMessage(err.message);
                setIsSigningUp(false);
            }
        }
    };

    const onGoogleSignIn = async (e) => {
        e.preventDefault();
        if (!isSigningUp) {
            setIsSigningUp(true);
            try {
                await doSignInWithGoogle();
                console.log("Google Sign-In successful!");
                navigate("/"); // Redirect to homepage after signup
            } catch (err) {
                setErrorMessage(err.message);
                setIsSigningUp(false);
            }
        }
    };

    return (
        <>
            {/* Header */}
            <div className='header'>
                <nav className="nav-links-left">
                    <a href="#Microsoft">
                        <img src="https://mailmeteor.com/logos/assets/PNG/Microsoft_Logo_512px.png" alt="Microsoft" />
                    </a>
                    <a href="#Trinity">
                        <img src="tcd.png" alt="TCD" />
                    </a>
                </nav>

                <h1>Azure Cloud Hosting</h1>

                <nav className="nav-links-right">
                    <a href="#about">About Us</a>
                    <a href="#contact">Contact</a>
                </nav>
            </div>

            {/* Register Form */}
            <div className='login-container'>
                <form onSubmit={onSubmit}>
                    <input 
                        type="text"
                        className="input-field"
                        placeholder="Sign up with your Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <input 
                        type="password"
                        className="input-field"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    {errorMessage && <p className="error-message">{errorMessage}</p>}

                    <button className="login-btn" type="submit" disabled={isSigningUp}>
                        {isSigningUp ? "Signing up..." : "Sign Up"}
                    </button>
                </form>

                <button className="google-btn" onClick={onGoogleSignIn} disabled={isSigningUp}>
                    {isSigningUp ? "Signing up..." : "Sign up with Google"}
                </button>

                <div className="actions">
                    <p>Already have an account? <Link to="/login">Log in</Link></p>
                </div>
            </div>
        </>
    );
};

export default Register;

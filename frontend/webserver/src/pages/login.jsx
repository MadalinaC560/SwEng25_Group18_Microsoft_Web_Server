import { useState } from 'react';
import { Link } from "react-router-dom";
import { doSignInWithEmailAndPassword, doSignInWithGoogle } from '../firebase/auth';
import { useAuth } from '../contexts/authContexts';

const Login = () => {
    const { userLoggedIn } = useAuth();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isSigningIn, setIsSigningIn] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const onSubmit = async (e) => {
        e.preventDefault();
        if (!isSigningIn) {
            setIsSigningIn(true);
            try {
                await doSignInWithEmailAndPassword(email, password);
            } catch (err) {
                setErrorMessage(err.message);
                setIsSigningIn(false);
            }
        }
    };

    const onGoogleSignIn = async (e) => {
        e.preventDefault();
        if (!isSigningIn) {
            setIsSigningIn(true);
            try {
                await doSignInWithGoogle();
            } catch (err) {
                setErrorMessage(err.message);
                setIsSigningIn(false);
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

            {/* Login Form */}
            <div className='login-container'>
                <form onSubmit={onSubmit}>
                    <input 
                        type="text"
                        className="input-field"
                        placeholder="Username / Email"
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

                    <div className="actions">
                        <a href="#forgot">Forgot Password?</a>
                        <Link to="/register">Sign Up</Link> {/* Updated this line */}
                    </div>


                    {errorMessage && <p className="error-message">{errorMessage}</p>}

                    <button className="login-btn" type="submit" disabled={isSigningIn}>
                        {isSigningIn ? "Signing in..." : "Log In"}
                    </button>
                </form>

 

            </div>
        </>
    );
};

export default Login;

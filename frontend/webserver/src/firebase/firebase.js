
import { initializeApp } from "firebase/app";
import {getAuth} from "firebase/auth";

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyBV3MAFVO8fbqgueZGRbp3r5IXqIsm3dv0",
  authDomain: "webserver-login-page.firebaseapp.com",
  projectId: "webserver-login-page",
  storageBucket: "webserver-login-page.firebasestorage.app",
  messagingSenderId: "957982488569",
  appId: "1:957982488569:web:d033634c0790fde3b22f28"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

export {app, auth};
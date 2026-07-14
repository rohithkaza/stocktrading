// LoginPage.js
// This file will handle the login and sign-up functionality using Firebase Authentication and a backend server for user management.
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {auth, signInWithEmailAndPassword, googleProvider, signInWithPopup, createUserWithEmailAndPassword} from '../firebaseConfig';
import Cookies from 'js-cookie';
import './login.css';
import logo from '../assets/logo.svg';
import google from '../assets/google.svg';
import stockmarket from '../assets/stockmarket.jpg';

const LoginPage = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSignUp, setIsSignUp] = useState(false); // whether the user is signing up/ logging in
  const [confirmPassword, setConfirmPassword] = useState(''); // for sign up confirmation
  const navigate = useNavigate();
  const backendUrl = 'https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/auth'; // backend URL for login/signup


  // clear error message every 5 seconds
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => {
        setError('');
      }, 5000); // clear error after 5 seconds
      return () => clearTimeout(timer); // cleanup the timer on unmount or when error changes
    }
  }, [error]); // This will clear the error message after 5 seconds


  const handleLogin = async(e) => {
    e.preventDefault();
    setError('');

    try {
      // firebase login
      const userCredential = await signInWithEmailAndPassword(auth, email, password);

      // process the user object
      const user = userCredential.user;
      const idToken = await user.getIdToken(); // get the ID token

      // store the ID token in a cookie
      Cookies.set('token', idToken, {
        expires: 1 / 24, // 1 hour
        secure: true,
        sameSite: 'None', // 'Strict' can cause issues with cross-site cookies in some browsers
      });
      // console.log('ID token:', idToken); // user should not see this message, currently for debugging

      const token = Cookies.get('token');

      // send the token to the backend
      const response = await fetch(backendUrl + '/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        credentials: 'include', // this will make sure the cookie containing id token is sent
      });
      if (!response.ok) {
        throw new Error('Failed to send token to the server');
      }

      // Extract username from email (everything before @)
      const username = email.split('@')[0] || 'username';

      // send the user object to the parent component
      onLogin(username); // send the username to the parent component
      navigate('/dashboard');
    } catch (error) {
      const errorMessage = handleFirebaseAuthError(error, setError);
      setError(errorMessage); // Set the error message in state
      console.error('Login error:', error); // Log the error for debugging
    }
  };

  const handleSignUp = async(e) => {
    e.preventDefault();
    setError('');

    // Check if passwords match
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    try {
      console.log('Sign up with email:', email, 'and password:', password);
      // firebase sign up
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);

      // process the user object
      const user = userCredential.user;
      const idToken = await user.getIdToken(); // get the ID token

      // store the ID token in a cookie
      Cookies.set('token', idToken, {
        expires: 1 / 24, // 1 hour
        secure: true,
        sameSite: 'None', // 'Strict' can cause issues with cross-site cookies in some browsers
      });
      // console.log('ID token:', idToken);

      const token = Cookies.get('token');

      // send the token to the backend
      const response = await fetch(backendUrl + '/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        credentials: 'include', // this will make sure the cookie is sent
      });
      if (!response.ok) {
        throw new Error('Failed to send token to the server');
      }

      // Extract username from email (everything before @)
      const username = email.split('@')[0] || 'username';

      // send the user object to the parent component
      onLogin(username);
      navigate('/dashboard');
    } catch (error) {
      const errorMessage = handleFirebaseAuthError(error, setError);
      setError(errorMessage); // Set the error message in state
      console.error('Sign up error:', error); // Log the error for debugging
    };
  };

  const handleGoogleSignIn = async() => {
    // Mock Google sign-in
    try {
      const result = await signInWithPopup(auth, googleProvider);

      // process the user object
      const user = result.user;
      const idToken = await user.getIdToken(); // get the ID token

      // store the ID token in a cookie
      Cookies.set('token', idToken, {
        expires: 1 / 24, // 1 hour
        secure: true,
        sameSite: 'None', // 'Strict' can cause issues with cross-site cookies in some browsers
      });
      // console.log('ID token:', idToken);

      const token = Cookies.get('token');

      // send the token to the backend
      const response = await fetch(backendUrl + '/google/handler', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        credentials: 'include',
      });
      if (!response.ok) {
        throw new Error('Failed to send token to the server');
      }

      const username = user.email.split('@')[0] || 'Google User';
      onLogin(username); // send the username to the parent component
      navigate('/dashboard');
    } catch (error) {
      const errorMessage = handleFirebaseAuthError(error, setError);
      setError(errorMessage); // Set the error message in state
      console.error('Google sign-in error:', error); // Log the error for debugging
    }
  };

  // Handle Firebase authentication errors
  // This function will handle Firebase authentication errors and set the error message in state
  // The error message is more readable for the user and can be customized based on the error code
  const handleFirebaseAuthError = (error, setError) => {
    let errorMessage = error.message; // default to the error message from Firebase

    switch (error.code) {
      case 'auth/invalid-email':
        errorMessage = 'Invalid email address.';
        break;
      case 'auth/user-not-found':
        errorMessage = 'User not found.';
        break;
      case 'auth/wrong-password':
        errorMessage = 'Incorrect password. Please try again.';
        break;
      case 'auth/weak-password':
        errorMessage = 'Password should be at least 6 characters long.';
        break;
      case 'auth/email-already-in-use':
        errorMessage = 'Email already registered. Please try logging in instead.';
        break;
      case 'auth/too-many-requests':
        errorMessage = 'Too many requests. Please try again later.';
        break;
      case 'auth/popup-closed-by-user':
        errorMessage = 'Google sign-in popup was closed before completion';
        break;
      case 'auth/network-request-failed':
        errorMessage = 'Network error. Please check your internet connection and try again.';
        break;
      case 'auth/invalid-credential':
        errorMessage = 'Invalid credential provided. Please check your email and password.';
        break;
      default:
        // For any other error, keep the default message
        errorMessage = 'An unknown error occurred. Please try again.';
        break;
    }
    setError(errorMessage); // Set the error message in state
    return errorMessage; // Return the error message for further handling if needed
  }


  // Handle form submission
  // This function will determine whether to log in or sign up based on the state of `isSignUp`
  // and call the appropriate handler function.
  const handleSubmit = (e) => {
    e.preventDefault();
    if (isSignUp) {
      // If it's sign up, call the sign up handler
      handleSignUp(e);
    }
    else {
      // If it's login, call the login handler
      handleLogin(e);
    }
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-content">
          <img src={logo} alt="logo" style={{ cursor: 'pointer' }} onClick={() => navigate('/')} />
          <div className="welcome-content">
            <h1>
              {isSignUp ? 'Create Account' : 'Welcome!'}
            </h1>
            <div className="subheading-1">
              {isSignUp ? 'Sign up by creating an account below' : 'Log in to your account here'}
            </div>
          </div>
          <form onSubmit={handleSubmit} className="fields-content">
            {error && <div className="error-message">{error}</div>}
            {/* Display error message if any */}

            <div className="form-group">
              <p>Email</p>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email"
                required
              />
            </div>

            <div className="form-group">
              <p>Password</p>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                required
              />
            </div>

            {isSignUp && (
            <div className="form-group">
              <p>Confirm Password</p>
              <input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm Password"
                required
              />
            </div>
            )}

            <button type="submit" className="login-button">
              {isSignUp ? 'Sign Up' : 'Log In'}
              {/* Change button text based on sign up state */}
            </button>

            <p>
              {isSignUp ? "Already have an account?" : "Don't have an account?"}{" "}
              <button type="button" className="toggle-button" onClick={() => setIsSignUp(!isSignUp)}>
                {isSignUp ? 'Log In' : 'Sign Up'}
              </button>
            </p>

            <div className="divider-row">
              <div className="divider-frame">
                <div className="divider"></div>
                <p>Or</p>
                <div className="divider"></div>
              </div>
            </div>
            <div className="sign-in-content">
              <button type="button" className="sign-in-button" onClick={handleGoogleSignIn}>
                <img src={google} alt="google" />
                <div className="button-text-content">
                Sign in with Google
                </div>
              </button>
            </div>
          </form>
        </div>
      </div>
      <div className="image">
        <img src={stockmarket} alt="stockmarket" className="stock-image" />
      </div>
    </div>
  );
};

export default LoginPage;
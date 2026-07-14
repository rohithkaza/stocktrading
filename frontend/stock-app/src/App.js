// App.js
// This file contains the main application component that sets up routing and session management for the app.
import React, { useState, useEffect } from 'react';
import './styles.css';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import Landing from './Pages/landing';
import LoginPage from './Pages/login';
import Accountdashboard from './Pages/accountdashboard';
import Search from './Pages/search';
import Indivstock from './Pages/indivstock';
import Documentation from './Pages/documentation';
import { getAuth, getIdToken } from 'firebase/auth';
import Cookies from 'js-cookie';

// simple modal for maintaining a session state
const SessionModal = ({ onContinue, onLogout }) => {
  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Session Expiring Soon!</h2>
        <p>Your session is about to expire. Do you want to continue?</p>
        <button onClick={onContinue}>Continue Session</button>
        <button onClick={onLogout}>Logout</button>
      </div>
    </div>
  );
};

// helper component to trigger redirect only after mount
const SessionRedirector = ({ path }) => {
  const navigate = useNavigate();

  useEffect(() => {
    if (path) {
      navigate(path, { replace: true });
    }
  }, [path]);

  return null;
};

const App = () => {
  // User authentication state
  const auth = getAuth();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userName, setUserName] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [redirectPath, setRedirectPath] = useState(null); // redirect to last path on refresh if needed
  const [sessionReady, setSessionReady] = useState(false); // wait until session check is done
  const SESSION_DURATION = 60 * 60 * 1000; // 1 hour
  const WARNING_DURATION = 1 * 60 * 1000; // 1 minute before session expires

  // Check if there's a stored login state on app load
  useEffect(() => {
    // Check local storage for login state and user name
    const storedLoginState = localStorage.getItem('isLoggedIn');
    const storedUserName = localStorage.getItem('userName');
    const expireTime = localStorage.getItem('expireTime');
    const lastPath = localStorage.getItem('lastPath');

    if (storedLoginState === 'true' && storedUserName && expireTime) {
      // if the user is logged in and the session has not expired
      const timeLeft = parseInt(expireTime) - Date.now(); // compute time left for current session
      if (timeLeft > 0) {
        // if there's time left in the session
        setIsLoggedIn(true);
        setUserName(storedUserName);
        startSessionTimeout(timeLeft);

        // if currently at landing, redirect to last visited page
        if (window.location.pathname === '/' && lastPath && lastPath !== '/') {
          setRedirectPath(lastPath);
        }
      } else {
        // if there's no time left, force logout
        // Note that the 1-min warning will not be shown if the session has already expired
        handleLogout();
      }
    }

    setSessionReady(true); // allow app to render now
  }, []);

  // Save current route as last visited path
  useEffect(() => {
    localStorage.setItem('lastPath', window.location.pathname);
  }, []);

  // Effect to control body scroll based on modal visibility
  useEffect(() => {
    if (showModal) {
      // Prevent scrolling when modal is open
      document.body.style.overflow = 'hidden';
    } else {
      // Allow scrolling when modal is closed
      document.body.style.overflow = 'unset'; // Or 'auto' or ''
    }

    // Cleanup function to restore scroll when component unmounts
    // or before the effect runs again if showModal changes
    return () => {
      document.body.style.overflow = 'unset'; // Or 'auto' or ''
    };
  }, [showModal]); // Dependency array ensures this runs only when showModal changes
  

  // Start session timer
  const startSessionTimeout = (timeLeft = SESSION_DURATION) => {
    // Clear any existing timers to avoid multiple timeouts
    clearTimeout(window.logoutTimer);
    clearTimeout(window.warningTimer);

    // logout after session duration
    window.logoutTimer = setTimeout(() => {
      handleLogout();
    }, timeLeft);

    // show warning modal before session expires
    if (timeLeft > WARNING_DURATION) {
      window.warningTimer = setTimeout(() => {
        console.log('Session is about to expire!'); // Debugging log
        setShowModal(true);
      }, timeLeft - WARNING_DURATION);
    }

    localStorage.setItem('expireTime', Date.now() + timeLeft);
  };

  // Handle login
  const handleLogin = (username) => {
    // Set user state and local storage
    setIsLoggedIn(true);
    setUserName(username);
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('userName', username);

    // Compute session expiration time and start session timeout
    const expireAt = Date.now() + SESSION_DURATION;
    localStorage.setItem('expireTime', expireAt.toString());
    startSessionTimeout(SESSION_DURATION);
  };

  // Handle logout
  const handleLogout = () => {
    // Clear user state and local storage
    setIsLoggedIn(false);
    setUserName('');
    setShowModal(false);

    // Clear session timers
    clearTimeout(window.logoutTimer);
    clearTimeout(window.warningTimer);

    // Clear cookies
    Cookies.remove('token', { secure: true, sameSite: 'None' });

    // Clear local storage
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userName');
    localStorage.removeItem('expireTime');
    localStorage.removeItem('lastPath');
  };

  // handle continue session
  const handleContinueSession = async () => {
    // Get the current user
    const currentUser = auth.currentUser;
    const username = currentUser ? currentUser.displayName : '';
    console.log('Current User:', username); // Debugging log to check current user
    if (currentUser) {
      try {
        // refresh the token
        const newToken = await getIdToken(currentUser, true);

        // Update the cookie with the new token
        Cookies.set('token', newToken, {
          expires: 1 / 24, // 1 hour
          secure: true, // Ensure these match your login settings
          sameSite: 'None', // Ensure these match your login settings
        });

        // update the local storage expire time
        const newExpire = Date.now() + SESSION_DURATION;
        localStorage.setItem('expireTime', newExpire.toString());
        setShowModal(false);
        startSessionTimeout(SESSION_DURATION);
      } catch (error) {
        console.error('Error refreshing token:', error);
        handleLogout(); // force logout on error
      }
    } else {
      console.error('No current user found for token refresh.');
      handleLogout(); // force logout if no user found
    }
  };

  // Handle search term changes
  const handleSearchTermChange = (term) => {
    setSearchTerm(term);
  };

  return (
    <Router>
      {/* Block all rendering until session state is checked */}
      {!sessionReady ? null : (
        <>
          {/* Only redirect AFTER mount is complete to avoid DOM crashes */}
          {redirectPath && <SessionRedirector path={redirectPath} />}

          {/* Render the session modal if showModal is true above all pages */}
          {showModal && (
            <SessionModal onContinue={handleContinueSession} onLogout={handleLogout} />
          )}

          <Routes>
            {/* Public route - Landing page */}
            <Route path="/" element={<Landing />} />

            {/* Public route - Documentation page */}
            <Route path="/documentation" element={<Documentation />} />

            {/* Login page */}
            <Route
              path="/login"
              element={isLoggedIn ? <Navigate to="/dashboard" /> : <LoginPage onLogin={handleLogin} />}
            />

            {/* Protected routes - Only accessible when logged in */}
            <Route
              path="/dashboard"
              element={isLoggedIn ?
                <Accountdashboard
                  userName={userName}
                  onLogout={handleLogout}
                  onSearchTermChange={handleSearchTermChange}
                  searchTerm={searchTerm}
                /> :
                <Navigate to="/" />
              }
            />

            <Route
              path="/search"
              element={isLoggedIn ?
                <Search
                  userName={userName}
                  onLogout={handleLogout}
                  initialSearchTerm={searchTerm}
                  onSearchTermChange={handleSearchTermChange}
                /> :
                <Navigate to="/" />
              }
            />

            <Route
              path="/stock/:symbol/:name"
              element={isLoggedIn ?
                <Indivstock
                  userName={userName}
                  onLogout={handleLogout}
                  onSearchTermChange={handleSearchTermChange}
                  searchTerm={searchTerm}
                /> :
                <Navigate to="/" />
              }
            />

            {/* Fallback route */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </>
      )}
    </Router>
  );
};

export default App;

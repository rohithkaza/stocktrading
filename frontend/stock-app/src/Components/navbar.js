import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './navbar.css';
import '../styles.css';
import logo from '../assets/logo.svg';
import closeIcon from '../assets/close.svg';
import account from '../assets/account.svg';

const Navbar = ({ userName, onLogout, onSearchTermChange, initialSearchTerm = '' }) => { 
    const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
    const [isHovered, setIsHovered] = useState(false);
    const [previousPath, setPreviousPath] = useState('');
    const searchInputRef = useRef(null);
    const navigate = useNavigate();
    const location = useLocation();
    const isSearchPage = location.pathname === '/search';

    // Update searchTerm if initialSearchTerm changes (e.g., when navigating back to Search page)
    useEffect(() => {
        setSearchTerm(initialSearchTerm);
    }, [initialSearchTerm]);

    // Store the previous path when navigating to search
    useEffect(() => {
        if (location.pathname !== '/search') {
            setPreviousPath(location.pathname);
        }
    }, [location.pathname]);

    // Focus input when navigating to search page
    useEffect(() => {
        if (isSearchPage && searchInputRef.current) {
            // Small delay to ensure the transition is complete
            setTimeout(() => {
                searchInputRef.current.focus();
            }, 50);
        }
    }, [isSearchPage]);

    const handleSearchChange = (e) => {
        const newSearchTerm = e.target.value;
        setSearchTerm(newSearchTerm);
        
        if (onSearchTermChange) {
            onSearchTermChange(newSearchTerm);
        }
    };

    // Handle click on the search input (even when empty)
    const handleSearchFocus = () => {
        // Only navigate if we're not already on the search page
        if (location.pathname !== '/search') {
            // Preserve the search term during navigation
            navigate('/search', { 
                state: { 
                    searchTerm: searchTerm,
                    previousPath: location.pathname
                } 
            });
        }
    };

    // Clear search and return to previous page
    const handleClearSearch = () => {
        setSearchTerm('');
        // Get the previous path from location state or use the stored one
        const returnPath = location.state?.previousPath || previousPath || '/dashboard';
        navigate(returnPath);
    };

    const handleLogoClick = () => {
        navigate('/dashboard');
    };

    const handleLogout = () => {
        if (onLogout) {
            onLogout();
            navigate('/');
        }
    };

    return (
        <div className="navbar-container">
            <nav className="navbar-content">
                <div className="nav-logo-content" onClick={handleLogoClick} style={{ cursor: 'pointer' }}>
                    <img src={logo} alt="logo" />
                </div>
                <div className="search-input-container">
                    <input 
                        ref={searchInputRef}
                        type="text" 
                        value={searchTerm} 
                        onChange={handleSearchChange}
                        onFocus={handleSearchFocus}
                        placeholder="Search for stocks by symbol or name" 
                        className="search-input"
                    />
                    {searchTerm && (
                        <div className="search-clear-button" onClick={handleClearSearch}>
                            <img src={closeIcon} alt="Clear search" />
                        </div>
                    )}
                </div>
                <div className="user-information">
                    <div className="username-container">
                        <img src={account} alt="account" />
                        <p className="username">{userName}</p>
                    </div>
                    <button 
                    onClick={handleLogout} 
                    className={`button ${isHovered ? 'hovered' : ''}`} 
                    onMouseEnter={() => setIsHovered(true)} 
                    onMouseLeave={() => setIsHovered(false)}
                    >Log Out
                    </button>
                </div>
            </nav>
        </div>
    );
};

export default Navbar;
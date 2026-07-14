import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './header.css';
import '../styles.css';
import logo from '../assets/logo.svg';
import info from '../assets/info.svg';

const Header = ({ onLogin }) => {
    const [isHovered, setIsHovered] = useState(false);
    const navigate = useNavigate();

    const handleLoginClick = () => {
        if (onLogin) {
            onLogin();
        } else {
            navigate('/login');
        }
    };

    const handleLogoClick = () => {
        navigate('/');
    };

    const handleInfoClick = () => {
        navigate('/documentation');
    };

    return (
        <div className="header-container">
            <nav className="header-content">
                <div className="logo-content" onClick={handleLogoClick} style={{ cursor: 'pointer' }}>
                    <img src={logo} alt="logo" />
                </div>
                <div className="info-group">
                    <div className="documentation">
                        <div className="info-item" onClick={handleInfoClick} style={{ cursor: 'pointer' }}>
                            <img src={info} alt="info" />
                            <span className="tooltip-text">User Guide</span>
                        </div>
                    </div>
                    <button 
                        onClick={handleLoginClick} 
                        className={`button ${isHovered ? 'hovered' : ''}`}
                        onMouseEnter={() => setIsHovered(true)}
                        onMouseLeave={() => setIsHovered(false)}
                    >
                        Log In
                    </button>
                </div>
            </nav>
        </div>
    );
};

export default Header;

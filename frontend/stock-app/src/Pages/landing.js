import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import "./landing.css";
import "../styles.css";
import stocklanding from '../assets/stocklanding.jpg';
import papertrading from '../assets/papertrading.png';
import portfolio from '../assets/portfolio.png';
import marketdata from '../assets/marketdata.png';
import Header from '../Components/header';
import Footer from '../Components/footer';

const Landing = () => {
    const navigate = useNavigate();
    const [isHovered, setIsHovered] = useState(false);
    
    const handleLogin = () => {
        navigate('/login');
    };

    return (
        <div className="landing-page">
            <Header onLogin={handleLogin} />
            <div className="first-landing-container">
                <div className="first-landing-content">
                    <div className="first-component-section">
                        <div className="first-text-section">
                            <div className="subheading-2 subheading">Your Very Own Stock Trading Environment</div>
                            <h1 className="heading">Experience Real Stock Trading, Risk-Free</h1>
                            <p className="description">
                                InvestExpress offers a hands-on way to learn stock trading. Simulate real-time market conditions and test your strategies using virtual money. Perfect for beginners and experienced investors alike, you'll gain practical experience in managing a portfolio without any financial risk.
                            </p>
                        </div>
                        <button 
                            onClick={handleLogin} 
                            className={`button ${isHovered ? 'hovered' : ''}`}
                            onMouseEnter={() => setIsHovered(true)}
                            onMouseLeave={() => setIsHovered(false)}
                        >Log In
                        </button>
                    </div>
                    <div className="first-image-section">
                        <img src={stocklanding} alt="stocklanding" className="stock-image" />
                    </div>
                </div>
            </div>
            <div className="second-landing-container">
                <div className="second-landing-content">
                    <div className="second-text-section">
                        <div className="subheading-1 subheading">Practice Real Stock Trading</div>
                        <h2 className="heading">Manage Your Virtual Stock Portfolio Following Latest Trends</h2>
                    </div>
                    <div className="second-component-section">
                        <div className="third-component-section">
                            <img src={papertrading} alt="papertrading" className="stock-image" />
                            <div className="third-text-section">
                                <div className="subheading-1">Paper Trading</div>
                                <p className="description">
                                    With InvestExpress, you can trade stocks using virtual money, providing a safe space to practice and learn. Buy and sell real stocks at real-time prices, and experiment with strategies.
                                </p>
                            </div>
                        </div>
                        <div className="third-component-section">
                            <img src={portfolio} alt="portfolio" className="stock-image" />
                            <div className="third-text-section">
                                <div className="subheading-1">Track Your Portfolio</div>
                                <p className="description">
                                    Keep track of your investments, view your account balance, and see how your stocks are performing over time. With InvestExpress, portfolio management has never been easier.
                                </p>
                            </div>
                        </div>
                        <div className="third-component-section">
                            <img src={marketdata} alt="marketdata" className="stock-image" />
                            <div className="third-text-section">
                                <div className="subheading-1">Real-Time Market Data</div>
                                <p className="description">
                                    InvestExpress updates stock prices every 15 minutes, ensuring that your paper trades reflect the real stock market dynamics. Stay up-to-date with the latest trends.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Landing;
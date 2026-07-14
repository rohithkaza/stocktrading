import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import "../styles.css";
import "./accountdashboard.css";
import refresh from '../assets/refresh.svg';
import piechart from '../assets/piechart.svg';
import Navbar from '../Components/navbar';
import Footer from '../Components/footer';
import Positionstocktable from '../Components/positionstocktable';
import Orderhistorytable from '../Components/orderhistorytable';
import Cookies from 'js-cookie';
import Watchlisttable from '../Components/watchlisttable';
import BalanceChart from '../Components/balancechart';
import ResetDialog from '../Components/resetdialog';
import ConfirmationMessage from '../Components/confirmationmsg';

const Accountdashboard = ({ userName, onLogout }) => {
    const navigate = useNavigate();
    const [portfolioBalance, setPortfolioBalance] = useState(0)
    const [availableFunds, setAvailableFunds] = useState(0)
    const [dailyChange, setDailyChange] = useState(0);
    const [percentChange, setPercentChange] = useState(0);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [showResetDialog, setShowResetDialog] = useState(false);
    const [showConfirmation, setShowConfirmation] = useState(false);
    const [refreshTrigger, setRefreshTrigger] = useState(0); // Add a refresh trigger state

    const [balanceChartData, setBalanceChartData] = useState([]);

    const fetchDashboardData = () => {
        setIsRefreshing(true);

        const token = Cookies.get('token');
        
        // Fetch portfolio balance
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/portfolioval', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`portfolio value request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("user portfolio value successful")
                console.log(data);
                setPortfolioBalance(data.balance)
            })
            .catch(error => {
                console.error("Error fetching portfolio value:", error);
            });
            
        // Fetch uninvested funds
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/uninvested', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`uninvested value request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("user uninvested value successful")
                console.log(data);
                setAvailableFunds(data.balance)
            })
            .catch(error => {
                console.error("Error fetching uninvested value:", error);
            });

        // Fetch portfolio changes
        fetch("https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/portfolio/change", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'Authorization': `Bearer ${token}`,
            },
            credentials: "include",
        })
            .then(res => {
                if (!res.ok) {
                    console.warn(`Portfolio change request failed: ${res.status}`);
                    setDailyChange(0);  // Use default values
                    setPercentChange(0);
                    return null;
                }
                return res.json();
            })
            .then(data => {
                if (data) {
                    console.log("get portfolio changes successful");
                    console.log(data);
                    setDailyChange(data.change);
                    setPercentChange(data.percentChange);
                }
                setIsRefreshing(false);
            })
            .catch(error => {
                console.error("Error fetching portfolio changes:", error);
                setDailyChange(0);  // Use default values
                setPercentChange(0);
                setIsRefreshing(false);
            });

            // fetch balance chart data
            // fetch('http://localhost:8080/user/industries', {
            fetch ('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/industries', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                credentials: 'include',
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`balance chart request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("got balance chart data successfully");
                console.log(data);
    
                if (data) {
                    setBalanceChartData(data);
                    console.log("balance chart data", data);
                } else {
                    console.error("Invalid or empty balancechart data received");
                    setBalanceChartData([]);
                }
            })
            .catch(error => {
                console.error("Error fetching balance chart data:", error);
                setBalanceChartData([]);
            });
    };

    useEffect(() => {
        fetchDashboardData();
    }, []);
    
    const handleRefreshClick = () => {
        fetchDashboardData();
    };
    
    const handleSearchTermChanged = (newSearchTerm) => {
        console.log("SEARCH TERM CHANGED:", newSearchTerm);
        if (newSearchTerm.trim()) {
            // Navigate to search page with the search term in state
            navigate('/search', { state: { searchTerm: newSearchTerm } });
        }
    };

    const handleStockSelect = (stockSymbol) => {
        navigate(`/stock/${stockSymbol}`, { state: { referrer: '/dashboard' } });
    };
    
    const handleResetClick = () => {
        setShowResetDialog(true);
    };
    
    const handleDialogCancel = () => {
        setShowResetDialog(false);
    };

    const resetFunds = () => {
        setShowResetDialog(false);
        setIsRefreshing(true);
        
        const token = Cookies.get('token');

        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`reset account request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("user reset successful");
                setShowConfirmation(true);
                
                // Fetch all data again
                fetchDashboardData();
                
                // Force refresh of child components
                setRefreshTrigger(prev => prev + 1);
                
                // Hide the confirmation message after 3 seconds
                setTimeout(() => {
                    setShowConfirmation(false);
                }, 3000);
                
                setIsRefreshing(false);
            })
            .catch(error => {
                console.error("Error resetting user:", error);
                setIsRefreshing(false);
            });
    };

    return (
        <div className="account-page">
            <Navbar userName={userName} onLogout={onLogout} onSearchTermChange={handleSearchTermChanged}/>
            <div className="account-container-outside">
                <div className="account-container-inside">
                    <div className="account-content">
                        <div className="first-dashboard-content">
                            <div className="money-content">
                                <div className="big-header">
                                    <h2>Dashboard</h2>
                                    <div className="refresh-container" onClick={handleRefreshClick} style={{ cursor: 'pointer' }}>
                                        <img 
                                            src={refresh} 
                                            alt="refresh" 
                                            style={isRefreshing ? { animation: 'spin 1s linear infinite' } : {}} 
                                        />
                                        <span className="tooltip-text-refresh">Refresh</span>
                                    </div>
                                </div>
                                <div className="funds-content">
                                    <div className="funds-gap">
                                        <div className="balance">
                                            <div className="subheading-1">Portfolio Balance</div>
                                            <div className="percentage">
                                            <h1>{portfolioBalance !== undefined ? `$${portfolioBalance.toFixed(2)}` : "Loading..."}</h1>
                                                    <p className="percentage-text" style={{ color: dailyChange >= 0 ? 'green' : 'red' }}>
                                                        {dailyChange !== undefined && percentChange !== undefined
                                                        ? `${dailyChange >= 0 ? "+" : "-"}$${Math.abs(dailyChange).toFixed(2)} (${percentChange.toFixed(2)}%) Today`
                                                        : "N/A"}
                                                </p>
                                            </div>
                                        </div>
                                        <div className="balance">
                                            <div className="subheading-1">Available Funds</div>
                                            <h1>{availableFunds !== undefined ? `$${availableFunds.toFixed(2)}` : "N/A"}</h1>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <p className="description">
                                        Your dashboard is your personal stock management center on <b>InvestExpress</b>, giving you a live snapshot of your portfolio and trading activity. 
                                        Here, you can track your total balance and available funds, view your portfolio invetments and a visual breakdown of these investments by sector, 
                                        monitor stocks you're watching, and review your order history over time. You can explore details on every stock you own and gain insights into your 
                                        trading performance over time.
                                    </p>
                                    <br></br>
                                    <p className="description">
                                        Want to try a new strategy? Use the <b>Reset Account</b> button at the bottom of the page to clear your 
                                        portfolio and start fresh. Everything you need to manage your virtual investments is here.
                                    </p>
                                </div>
                            </div>
                            <div className="balance-container">
                                {balanceChartData && Object.values(balanceChartData).some(value => value > 0) ? (
                                    <BalanceChart balanceChartData={balanceChartData} />
                                ) : (
                                    <div className="empty-balance-container">
                                        <div className="chart-subheading-2"><img src={piechart} alt="piechart"/>You currently have no stocks in your portfolio.</div>
                                    </div>
                                )}
                            </div>
                        </div>
                        <div className="subheading-1" style={{marginTop: "0px"}}> Your Positions </div>
                        <Positionstocktable key={`positions-${refreshTrigger}`} />
                        <div className="subheading-1">Your Watchlist</div>
                        <Watchlisttable key={`watchlist-${refreshTrigger}`} />
                        <div className="subheading-1">Order History</div>
                        <Orderhistorytable key={`history-${refreshTrigger}`} />
                        <div className="reset">
                            <button className="reset-button" onClick={handleResetClick}>Reset Account</button>
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
            
            {/* Reset Account Dialog */}
            {showResetDialog && (
                <ResetDialog 
                    onCancel={handleDialogCancel} 
                    onReset={resetFunds} 
                />
            )}
            
            {/* Confirmation Message */}
            {showConfirmation && <ConfirmationMessage />}
        </div>
    );
};

export default Accountdashboard;
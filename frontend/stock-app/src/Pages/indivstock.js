import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import "../styles.css";
import "./indivstock.css";
import arrow from '../assets/arrow.svg';
import refresh from '../assets/refresh.svg';
import addwatchlist from '../assets/addwatchlist.svg';
import removewatchlist from '../assets/removewatchlist.svg';
import Navbar from '../Components/navbar';
import Footer from '../Components/footer';
import Buytrade from '../Components/buytrade';
import Selltrade from '../Components/selltrade';
import Buydialog from '../Components/buydialog';
import Selldialog from '../Components/selldialog';
import StockChart from '../Components/stockchart';
import ConfirmationMessage from '../Components/confirmationmsg';
import FailureMessage from '../Components/failuremsg';
import News from '../Components/news';
import Cookies from 'js-cookie';
import TooltipLink from '../Components/tooltip';

function format_date(date) {
    const formatted = date.getFullYear() + '-'
        + String(date.getMonth() + 1).padStart(2, '0') + '-'
        + String(date.getDate()).padStart(2, '0');
    return formatted
}


const Indivstock = ({ userName, onLogout }) => {
    const { symbol, name } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [activeTab, setActiveTab] = useState('buy'); // 'buy' or 'sell'
    const [showBuyDialog, setShowBuyDialog] = useState(false);
    const [showSellDialog, setShowSellDialog] = useState(false);
    const [OHLCData, setOHLCData] = useState([]);
    const [balance, setBalance] = useState(0);
    const [availableShares, setAvailableShares] = useState(0);
    const [showConfirmation, setShowConfirmation] = useState(false);
    const [showFailure, setShowFailure] = useState(false);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [currentChartIncrement, setCurrentChartIncrement] = useState('D');

    const [sellSuccess, setSellSuccess] = useState(false);
    const [buySuccess, setBuySuccess] = useState(false);

    const [newsData, setNewsData] = useState([]);
    const [inWatchlist, setInWatchlist] = useState(false);
    const [chartIncrement, setChartIncrement] = useState('D');
    const [stockInfo, setStockInfo] = useState({
        symbol: symbol || 'ABCD',
        name: name,
        price: 123.45,
        change: -4.56,
        changePercent: -7.89,
        description: "Loading...",
        sourceType: "",
        articleTitle: "",
        url: "",
        citationNums: [],
        citationTexts: []
    });
    const [sourceText, setSourceText] = useState(<p class="source"></p>);
    const [descriptionText, setDescriptionText] = useState(<p className="description"></p>);

    useEffect(() => {
        if (stockInfo.sourceType==="Wikipedia") {
            setSourceText(<p class="source">
            Source: Wikipedia (<a href={stockInfo.url}>{stockInfo.articleTitle}</a>). Some information may be inaccurate. Original article released under the <a href="\wikipediaCopyright.txt">Creative Commons Attribution-Share-Alike License 4.0</a>.
            </p>);
        } else if (stockInfo.sourceType==="AlphaVantage" && stockInfo.description!=="No Company Information Available. Please try a Different Stock.") {
            setSourceText(<p class="source">Source: Alpha Vantage</p>);
        } else { // "" (should only trigger for the initial sourceType assignment)
            setSourceText(<p class="source"></p>);
        }
    }, [stockInfo.sourceType, stockInfo.description, stockInfo.url, stockInfo.articleTitle]);

    useEffect(() => {
        if (stockInfo.sourceType==="Wikipedia") {
            let pieces = [];
            let pieceOn = "";
            for(let i = 0; i < stockInfo.description.length; i++) {
                if (stockInfo.description[i]==="[") {
                    pieces.push(pieceOn);
                    pieceOn = "";
                    if (stockInfo.description.indexOf(']', i)===-1) {
                        pieceOn += "[";
                        continue;
                    }
                    let citationNum = stockInfo.description.substring(i, stockInfo.description.indexOf(']', i)+1); //has format "[number]"
                    let citationInd = stockInfo.citationNums.indexOf(citationNum);
                    if(citationInd===-1) {
                        pieceOn += "[";
                        continue;
                    }
                    let citationText = stockInfo.citationTexts[citationInd];
                    //console.log(i);
                    //console.log(stockInfo.description);
                    //console.log("PRINTING CITATION DETAILS");
                    //console.log(citationNum);
                    //console.log(citationText);
                    pieces.push(<TooltipLink tooltipText={citationText} triggerText={citationNum}/>);
                    i = stockInfo.description.indexOf(']', i);
                } else {
                    pieceOn += stockInfo.description[i];
                }
            }
            //console.log(stockInfo.description);
            pieces.push(pieceOn);
            setDescriptionText(<p className="description">{pieces.map((piece) => <>{piece}</>)}</p>);
        } else {
            setDescriptionText(<p className="description">{stockInfo.description}</p>);
        }
    }, [stockInfo.description]);

    const fetchStockData = () => {
        setIsRefreshing(true);
        
        // Fetch OHLC chart data
        handleChartIncrementClick(currentChartIncrement);

        const token = Cookies.get('token');

        // Get the stock price
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/price', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`stock price request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("got price successfully")
            console.log(data)
            setStockInfo(prev => ({
                ...prev,
                price: parseFloat(data.price),
                symbol: symbol
            }));
        })
        .catch(error => {
            console.error("Error fetching stock price:", error);
        });

        // Get the user balance
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
                throw new Error(`user balance request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("user balance successful")
            console.log(data);
            setBalance(data.balance);
        })
        .catch(error => {
            console.error("Error fetching user balance:", error);
        });

        // Get the number of shares owned
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/stockshares', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`user stockshares request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("stock shares successful")
            console.log(data);
            setAvailableShares(data.shares);
        })
        .catch(error => {
            console.error("Error fetching stock shares:", error);
        });

        // Get the stock change
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/change', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`stock change request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("stock change successful")
            console.log(data);
            setStockInfo(prev => ({
                ...prev,
                change: parseFloat(data.change),
            }));
        })
        .catch(error => {
            console.error("Error fetching stock change:", error);
        });

        // Get the stock change percent
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/changepercent', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`stock change percent request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("stock change percent successful")
            console.log(data);
            setStockInfo(prev => ({
                ...prev,
                changePercent: parseFloat(data.changePercent),
            }));
            setIsRefreshing(false);
        })
        .catch(error => {
            console.error("Error fetching stock change percent:", error);
            setIsRefreshing(false);
        });

        // Get the company description
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/description', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
        .then(response => response.json())
        .then(data => { //response will always have "ok" status
            setStockInfo(prev => ({
                ...prev,
                description: data.description,
                articleTitle: data.articleTitle,
                sourceType: data.sourceType,
                url: data.url,
                citationNums: data.citationNums,
                citationTexts: data.citationTexts
            }));
        });

        const now = new Date();
        const start_date = new Date(now.setDate(now.getDate()-1));
        const end_date = new Date();

        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/news', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: stockInfo.symbol,
                startDate: start_date,
                endDate: end_date,
                limit: 3,
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`stock news request failed: ${response.status}`);
                }
                // First check the content type of the response
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    return response.json();
                } else {
                    return response.text(); // Handle as text if not JSON
                }
            })
            .then(data => {
                console.log("news successful");
                const dataArr = JSON.parse(data);
                console.log(dataArr)
                setNewsData(dataArr);
            })
            .catch(error => {
                console.error("Error during news operation:", error);
            });

        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/watchlist/in', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`in watchlist request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("in watchlist successful")
                console.log(data);
                setInWatchlist(data)
            })
            .catch(error => {
                console.error("Error fetching in watchlist change:", error);
            });
    };

    useEffect(() => {
        if (symbol) {
            fetchStockData();
        }
    }, [symbol]);
    
    const handleSearchTermChanged = (newSearchTerm) => {
        console.log("SEARCH TERM CHANGED:", newSearchTerm);
        if (newSearchTerm.trim()) {
            navigate('/search', { state: { searchTerm: newSearchTerm } });
        }
    };

    const handleRefreshClick = () => {
        fetchStockData();
    };

    const handleBuyClick = () => {
        setShowBuyDialog(true);
    };

    const handleSellClick = () => {
        setShowSellDialog(true);
    };

    const handleDialogCancel = () => {
        setSellSuccess(false);
        setBuySuccess(false);
        setShowBuyDialog(false);
        setShowSellDialog(false);
    };

    const handleChartIncrementClick = (increment) => {
        setCurrentChartIncrement(increment);
        let timeFrame = "";
        const today = new Date();
        const end_date = new Date(today.setDate(today.getDate()-1));
        let start_date = new Date(today.setMonth(today.getMonth() - 1));
        
        if (increment === "D") {
            timeFrame = "1Day";
            setChartIncrement('D')
        }
        else if (increment === "W") {
            timeFrame = "1Week";
            start_date = new Date(today.setMonth(today.getMonth() - 4));
            setChartIncrement('W')
        }
        else if (increment === "M") {
            timeFrame = "1Month";
            start_date = new Date(today.setMonth(today.getMonth() - 30));
            setChartIncrement('M')
        }
        else {
            console.error("invalid increment");
            return;
        }

        const token = Cookies.get('token');
        
        console.log(`Fetching data for symbol: ${symbol}`);
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/histdata', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: symbol,
                timeFrame: timeFrame,
                startDate: format_date(start_date),
                endDate: format_date(end_date),
            })
            
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`histdata request failed: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("got hist data successfully");
            console.log(data);

            if (data[symbol] && Array.isArray(data[symbol])) {
                const formattedData = data[symbol].map(item => {
                    return {
                        date: item.t.split('T')[0],
                        open: item.o,
                        high: item.h,
                        low: item.l,
                        close: item.c,
                        volume: item.v
                    };
                });
                setOHLCData(formattedData);
                console.log(formattedData);
            } else {
                console.error("Invalid or empty OHLC data received");
                setOHLCData([]);
            }
        })
        .catch(error => {
            console.error("Error fetching chart data:", error);
            setOHLCData([]);
        });
    };

    const handleBuyConfirm = () => {
        setBuySuccess(false)
        console.log(`Buying ${stockInfo.symbol}`);
        const shareElement = document.querySelector('.share-input');
        const quantity = parseInt(shareElement.value);
        
        if (isNaN(quantity) || quantity <= 0) {
            setShowFailure(true);
            setShowBuyDialog(false);
            return;
        }
        
        setShowBuyDialog(false);

        const token = Cookies.get('token');
        
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/buy', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: stockInfo.symbol,
                quantity: quantity
            })
        })
        .then(response => {
            if (!response.ok) {
                setBuySuccess(false);
                throw new Error(`user buy request failed: ${response.status}`);
            }
            // First check the content type of the response
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return response.json();
            } else {
                return response.text(); // Handle as text if not JSON
            }
        })
        .then(data => {
            console.log("buy successful");
            console.log(data);
            setShowConfirmation(true);
            setBuySuccess(true);
            
            // Refresh data after successful purchase
            fetchStockData();
        })
        .catch(error => {
            setBuySuccess(false);
            console.error("Error during buy operation:", error);
            setShowFailure(true);
        });
    };

    const handleSellConfirm = () => {
        setSellSuccess(false)
        console.log(`Selling ${stockInfo.symbol}`);
        const shareElement = document.querySelector('.share-input');
        const quantity = parseInt(shareElement.value);
        
        if (isNaN(quantity) || quantity <= 0 || quantity > availableShares) {
            setShowFailure(true);
            setShowSellDialog(false);
            return;
        }
        
        setShowSellDialog(false);

        const token = Cookies.get('token');
        
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/sell', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
            body: JSON.stringify({
                stockSymbol: stockInfo.symbol,
                quantity: quantity
            })
        })
        .then(response => {
            if (!response.ok) {
                setSellSuccess(false)
                throw new Error(`user sell request failed: ${response.status}`);
            }
            // First check the content type of the response
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return response.json();
            } else {
                return response.text(); // Handle as text if not JSON
            }
        })
        .then(data => {
            console.log("sell successful");
            console.log(data);
            setShowConfirmation(true);
            setSellSuccess(true);
            
            // Refresh data after successful sale
            fetchStockData();
        })
        .catch(error => {
            setSellSuccess(false);
            console.error("Error during sell operation:", error);
            setShowFailure(true);
        });
    };

    // Handle back button navigation
    const handleBackClick = () => {
        // If we have a referrer in the state, navigate to that
        if (location.state && location.state.referrer) {
            navigate(location.state.referrer);
        } else {
            // Default fallback to dashboard if no referrer is available
            navigate('/dashboard');
        }
    };

    const handleWatchlistClick = () => {
        const token = Cookies.get('token');

        if (inWatchlist) {
            fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/watchlist/remove', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                credentials: 'include',
                body: JSON.stringify({
                    stockSymbol: symbol
                })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`remove watchlist request failed: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("remove watchlist successful")
                    fetchStockData();
                })
                .catch(error => {
                    console.error("Error removing stock from watchlist:", error);
                });
        } else {
            fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/watchlist/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                credentials: 'include',
                body: JSON.stringify({
                    stockSymbol: symbol
                })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`add to watchlist request failed: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("add to watchlist successful")
                    fetchStockData();
                })
                .catch(error => {
                    console.error("Error adding stock to watchlist:", error);
                });
        }
    }

    const buttonText = inWatchlist ? "Remove from Watchlist" : "Add to Watchlist";
    const buttonImage = inWatchlist ? removewatchlist : addwatchlist;

    return (
        <div className="indiv-page">
            <Navbar userName={userName} onLogout={onLogout} onSearchTermChange={handleSearchTermChanged}/>
            <div className="indiv-container-outside">
                <div className="indiv-container-inside">
                    <div className="indiv-content">
                        <div className="stock-header">
                            <div className="arrow-container" onClick={handleBackClick} style={{ cursor: 'pointer' }}>
                                <img src={arrow} alt="back-arrow" />
                            </div>
                            <div className="header-text">
                                <div className="big-header">
                                    <h2>{stockInfo.symbol} ({stockInfo.name})</h2>
                                    <div className="refresh-container" onClick={handleRefreshClick} style={{ cursor: 'pointer' }}>
                                        <img 
                                            src={refresh} 
                                            alt="refresh" 
                                            style={isRefreshing ? { animation: 'spin 1s linear infinite' } : {}} 
                                        />
                                        <span className="tooltip-text-refresh">Refresh</span>
                                    </div>
                                </div>
                                <div className="small-header">
                                    <div className="subheading-1">${stockInfo.price.toFixed(2)}</div>
                                    <p className="percentage-text" style={{ color: stockInfo.change >= 0 ? 'green' : 'red' }}>
                                        {stockInfo.change >= 0 ? "+" : "-"}${Math.abs(stockInfo.change).toFixed(2)} ({stockInfo.changePercent.toFixed(2)}%) Today
                                    </p>
                                </div>
                                <div className="small-header">
                                    <button className="watchlist-button" onClick={handleWatchlistClick}>{buttonText}<img src={buttonImage} alt="watchlist button icon" /></button>
                                </div>
                            </div>
                        </div>
                        <div className="components">
                            <div className="chart-container">
                                <StockChart ohlcData={OHLCData} handleChartIncrementClick={handleChartIncrementClick} activeIncrement={chartIncrement}/>
                            </div>
                            <div className="trade-wrapper">
                                {activeTab === 'buy' ? (
                                    <Buytrade onBuyClick={handleBuyClick} onTabChange={() => setActiveTab('sell')} sharePrice={stockInfo.price} balance={balance.toFixed(2)} buySuccess={buySuccess}/>
                                ) : (
                                    <Selltrade onSellClick={handleSellClick} onTabChange={() => setActiveTab('buy')} sharePrice={stockInfo.price} availableShares={availableShares} sellSuccess={sellSuccess} />
                                )}
                            </div>
                        </div>
                        <div className="description-content">
                            <div className="subheading-1">About {stockInfo.symbol} ({stockInfo.name})</div>
                            {descriptionText}
                            {sourceText}
                        </div>
                        <div className="about-content">
                            <div className="subheading-1">News</div>
                            <div className="news-articles">
                                {newsData.length > 0 ? (
                                    newsData.slice(0, 3).map((article, i) => (
                                        <News key={i} article={article} />
                                    ))
                                ) : (
                                    <p className="description">No current news to show.</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <Footer />

            {/* Confirmation Message */}
            {showConfirmation && <ConfirmationMessage />}

            {/* Failure Message */}
            {showFailure && <FailureMessage />}
            
            {/* Buy Dialog */}
            {showBuyDialog && (
                <div onClick={handleDialogCancel} style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%' }}>
                    <div onClick={e => e.stopPropagation()}>
                        <Buydialog 
                            stockSymbol={stockInfo.symbol} 
                            onCancel={handleDialogCancel} 
                            onBuy={handleBuyConfirm} 
                        />
                    </div>
                </div>
            )}
            
            {/* Sell Dialog */}
            {showSellDialog && (
                <div onClick={handleDialogCancel} style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%' }}>
                    <div onClick={e => e.stopPropagation()}>
                        <Selldialog 
                            stockSymbol={stockInfo.symbol} 
                            onCancel={handleDialogCancel} 
                            onSell={handleSellConfirm} 
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default Indivstock;
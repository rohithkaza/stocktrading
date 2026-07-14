import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import './stocktable.css';
import '../styles.css';
import Cookies from 'js-cookie';

const Positionstocktable = () => {
    const navigate = useNavigate();
    const [investments, setInvestments] = useState({});
    const [percentChanges, setPercentChanges] = useState({});
    const [currentPrices, setCurrentPrices] = useState({});
    const [stockNames, setStockNames] = useState({});

    // Fetch current price for a single stock symbol
    const fetchCurrentPrice = (symbol) => {
        return fetch("https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/price", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
            body: JSON.stringify({stockSymbol: symbol}),
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("fail to fetch the stock price...");
                }
                return res.json();
            })
            .then((data) => data.price);
    };

    // Fetch current prices for all symbols and return them as a map
    const fetchAllCurrentPrices = (symbols) => {
        const requests = symbols.map((symbol) =>
            fetchCurrentPrice(symbol).then((price) => ({symbol, price}))
        );
        return Promise.all(requests).then((results) => {
            const priceMap = {};
            results.forEach(({symbol, price}) => {
                priceMap[symbol] = price;
            });
            return priceMap;
        });
    };

    const token = Cookies.get('token');

    // Fetch percent change for a single stock symbol
    const fetchChangePercent = (symbol) => {
        return fetch("https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/changepercent", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'Authorization': `Bearer ${token}`,
            },
            credentials: "include",
            body: JSON.stringify({stockSymbol: symbol}),
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("fail to fetch the stock percent change...");
                }
                return res.json();
            })
            .then((data) => data.changePercent);
    };

    // Fetch percent change for all symbols and return them as a map
    const fetchAllChangePercents = (symbols) => {
        const requests = symbols.map((symbol) =>
            fetchChangePercent(symbol).then((percent) => ({symbol, percent}))
        );
        return Promise.all(requests).then((results) => {
            const percentMap = {};
            results.forEach(({symbol, percent}) => {
                percentMap[symbol] = percent;
            });
            return percentMap;
        });
    };

    // Fetch full name for a single stock symbol
    const fetchName = (symbol) => {
        return fetch("https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/stockname", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'Authorization': `Bearer ${token}`,
            },
            credentials: "include",
            body: JSON.stringify({stockSymbol: symbol}),
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("fail to fetch the stock name change...");
                }
                return res.json();
            })
            .then((data) => data.name);
    };

    // Fetch full names for each symbol and return them as a map
    const fetchAllStockNames= (symbols) => {
        const requests = symbols.map((symbol) =>
            fetchName(symbol).then((name) => ({symbol, name}))
        );
        return Promise.all(requests).then((results) => {
            const nameMap = {};
            results.forEach(({symbol, name}) => {
                nameMap[symbol] = name;
            });
            return nameMap;
        });
    };

    useEffect(() => {
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/investments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`investments request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("user investment list successful")
                console.log(data);
                setInvestments(data);
                const symbols = Object.keys(data);
                return Promise.all([
                    fetchAllChangePercents(symbols),
                    fetchAllCurrentPrices(symbols),
                    fetchAllStockNames(symbols)
                ]);
            })
            .then(([percentMap, priceMap, nameMap]) => {
                setPercentChanges(percentMap);
                setCurrentPrices(priceMap);
                setStockNames(nameMap);
            })
            .catch(error => {
                console.error("Error fetching investments data:", error);
            });
    }, []);

    const handleRowClick = (symbol, name) => {
        // Include referrer information when navigating
        console.log("NAME HERE", name)
        navigate(`/stock/${symbol}/${name}`, {state: {referrer: '/dashboard'}});
    };

    return (
        <div className="table-container">
            <div className="header-row">
                <div className="name-cell">
                    <div className="name-text">
                        <div className="subheading-2 description">Name</div>
                    </div>
                    <div className="divider"></div>
                </div>
                <div className="shares-cell">
                    <div className="shares-text">
                        <div className="subheading-2 description">Price</div>
                    </div>
                    <div className="divider"></div>
                </div>
                <div className="value-cell">
                    <div className="value-text">
                        <div className="subheading-2 description">Shares</div>
                    </div>
                    <div className="divider"></div>
                </div>
                <div className="value-cell">
                    <div className="value-text">
                        <div className="subheading-2 description">Change</div>
                    </div>
                    <div className="divider"></div>
                </div>
            </div>

            {/* Make rows clickable */}
            <div className="scrollable-body">
                {Object.values(investments).map(({stockSymbol, shares, avgPrice}) =>
                    <div key={stockSymbol} className="row" onClick={() => handleRowClick(stockSymbol, stockNames[stockSymbol])}
                        style={{cursor: 'pointer'}}>
                        <div className="name-cell">
                            <div className="name-text">
                                <div className="name-text-inside">
                                    <div className="subheading-2">{stockSymbol}</div>
                                    <p className="description">{stockNames[stockSymbol]}</p>
                                </div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        
                        <div className="shares-cell">
                            <div className="shares-text">
                                <div className="subheading-2">${currentPrices[stockSymbol] ? currentPrices[stockSymbol].toFixed(2) : 'Loading...'}</div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text">
                                <div className="subheading-2">{shares}</div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text">
                                {(() => {
                                    const percent = percentChanges[stockSymbol];
                                    const isPositive = percent >= 0;

                                    return (
                                        <div className={`subheading-2 ${isPositive ? 'positive' : 'negative'}`}>
                                            {isPositive ? '+' : '-'}{Math.abs(percent).toFixed(2)}%
                                        </div>
                                    );
                                })()}
                            </div>
                            <div className="divider"></div>
                        </div>
                    </div>
                )}
                {/* Show a message if no transactions */}
                {Object.keys(investments).length === 0 && (
                    <div className="row">
                        <div className="empty-table" style={{ textAlign: 'center', width: '100%' }}>
                            <div className="subheading-2-table">You have no current positions.</div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Positionstocktable;
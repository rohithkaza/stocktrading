import React from 'react';
import { useNavigate } from 'react-router-dom';
import './stocktable.css';
import '../styles.css';

const Searchstocktable = ({ onStockSelect, stocks}) => { 
    const navigate = useNavigate();

    const handleRowClick = (symbol, name) => {
        if (onStockSelect) {
            onStockSelect(symbol, name);
        } else {
            // Use a default navigation with the current page as referrer
            navigate(`/stock/${symbol}/${name}`, { state: { referrer: window.location.pathname } });
        }
    };

    // Default stocks to show if no search results are provided
    const defaultStocks = [
        // { symbol: 'TLKM', name: 'PT Telkom Indonesia Tbk.', price: 120.07, change: 54.28, isPositive: true },
        // { symbol: 'NVDA', name: 'NVIDIA Corporation', price: 120.07, change: 54.28, isPositive: true },
        // { symbol: 'AAPL', name: 'Apple Inc.', price: 190.50, change: 2.13, isPositive: true }
        { symbol: 'AAPL', name: 'Apple Inc.', price: 178.72, change: 0.95, isPositive: true },
        { symbol: 'MSFT', name: 'Microsoft Corporation', price: 410.34, change: -3.45, isPositive: false },
        { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 152.51, change: 1.23, isPositive: true },
        { symbol: 'AMZN', name: 'Amazon.com, Inc.', price: 177.23, change: 0.67, isPositive: true },
        { symbol: 'NVDA', name: 'NVIDIA Corporation', price: 120.07, change: -4.58, isPositive: false }
    ];

    // Use provided stocks or default stocks
    const displayStocks = stocks || defaultStocks;

    return (
        <div className="searchtable-container">
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
                        <div className="subheading-2 description">Change</div>
                    </div>
                    <div className="divider"></div>
                </div>
            </div>
            
            {/* Map through stocks and create rows */}
            <div className="scrollable-body">
                {displayStocks.map((stock, index) => (
                    <div 
                        key={index} 
                        className="row" 
                        onClick={() => {
                            handleRowClick(stock.symbol, (stock.name.length > 30 ? stock.name.substring(0,27)+'...' : stock.name));
                            }
                        }
                        style={{ cursor: 'pointer' }}
                    >
                        <div className="name-cell">
                            <div className="name-text">
                                <div className="name-text-inside">
                                    <div className="subheading-2">{stock.symbol}</div>
                                    <p className="description">{stock.name}</p>
                                </div>
                            </div>
                            {index < displayStocks.length - 1 && <div className="divider"></div>}
                        </div>
                        <div className="shares-cell">
                            <div className="shares-text">
                                <div className="subheading-2">${typeof stock.price === 'number' ? stock.price.toFixed(2) : stock.price}</div>
                            </div>
                            {index < displayStocks.length - 1 && <div className="divider"></div>}
                        </div>
                        <div className="value-cell">
                            <div className="value-text">
                                <div className={`subheading-2 ${stock.isPositive !== false ? "positive" : "percentage-text"}`}>
                                    {stock.change > 0 ? '+' : ''}{typeof stock.change === 'number' ? stock.change.toFixed(2) : stock.change}%
                                </div>
                            </div>
                            {index < displayStocks.length - 1 && <div className="divider"></div>}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Searchstocktable;
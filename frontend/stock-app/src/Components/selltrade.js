import React, { useState, useEffect } from 'react';
import './trade.css';
import '../styles.css';

const Selltrade = ({ onSellClick, onTabChange, sharePrice, availableShares, sellSuccess}) => { 
    const [shares, setShares] = useState('');
    const [total, setTotal] = useState(0);
    // const sharePrice = 120.07;

    useEffect(() => {
        if (sellSuccess) {
            setShares('');
            setTotal(0);
        }
    }, [sellSuccess, setShares, setTotal]);

    const handleSharesChange = (e) => {
        const value = e.target.value.replace(/[^\d]/g, '');
        setShares(value);
        if (value && !isNaN(value)) {
            setTotal((parseFloat(value) * sharePrice).toFixed(2));
        } else {
            setTotal(0);
        }
    };

    return (
        <div className="trade-container">
            <div className="header">
                <div className="buy-sell-header" onClick={onTabChange} style={{ cursor: 'pointer' }}>
                    <p className="text-action">Buy</p>
                    <div className="inactive-highlight">
                    </div>
                </div>
                <div className="buy-sell-header">
                    <p className="text-action">Sell</p>
                    <div className="active-highlight">
                    </div>
                </div>
            </div>
            <div className="trade-content">
                <div className="share-content">
                    <p className="text-action">Shares</p>
                    <input 
                        className="share-input"
                        type="text"
                        placeholder="Enter a share amount to sell" 
                        value={shares}
                        onChange={handleSharesChange}
                        min="0"
                        max="4"
                    />
                </div>
                <div className="value">
                    <p className="text-stretch">Share Value</p>
                    <p className="text-action">${sharePrice.toFixed(2)}</p>
                </div>
                <div className="value">
                    <p className="text-stretch">Total</p>
                    <p className="text-action">${total}</p>
                </div>
                <div className="transaction">
                    <p className="text-avail">{availableShares} shares available</p>
                    <button className="button-stretch" onClick={onSellClick}>Sell</button>
                </div>
            </div>
        </div>
    );
};

export default Selltrade;
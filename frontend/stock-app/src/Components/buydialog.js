import React from 'react';
import './dialog.css';
import '../styles.css';

const Buydialog = ({ stockSymbol = 'NVDA', onCancel, onBuy }) => { 
    return (
        <div className="dialog-overlay">
            <div className="dialog-container">
                <div className="dialog-content">
                    <div className="subheading-1">Are you sure you want to buy {stockSymbol}?</div>
                    <p className="description">This action cannot be undone.</p>
                </div>
                <div className="dialog-buttons">
                    <button className="cancel-button" onClick={onCancel}>
                        Cancel
                    </button>
                    <button onClick={onBuy}>Buy</button>
                </div>
            </div>
        </div>
    );
};

export default Buydialog;
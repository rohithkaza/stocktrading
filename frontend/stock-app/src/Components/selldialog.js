import React from 'react';
import './dialog.css';
import '../styles.css';

const Selldialog = ({ stockSymbol = 'NVDA', onCancel, onSell }) => { 
    return (
        <div className="dialog-overlay">
            <div className="dialog-container">
                <div className="dialog-content">
                    <div className="subheading-1">Are you sure you want to sell {stockSymbol}?</div>
                    <p className="description">This action cannot be undone.</p>
                </div>
                <div className="dialog-buttons">
                    <button className="cancel-button" onClick={onCancel}>
                        Cancel
                    </button>
                    <button onClick={onSell}>Sell</button>
                </div>
            </div>
        </div>
    );
};

export default Selldialog;
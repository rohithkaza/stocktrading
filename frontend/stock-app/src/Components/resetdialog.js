import React from 'react';
import './dialog.css';
import '../styles.css';

const ResetDialog = ({ onCancel, onReset }) => { 
    return (
        <div className="dialog-overlay" style={{ 
            backgroundColor: 'rgba(0, 0, 0, 0.6)', // Darker overlay for better contrast
            backdropFilter: 'blur(2px)',  // Add blur effect
            zIndex: 1000  // Ensure it's above other elements
        }}>
            <div className="dialog-container" style={{
                background: 'white',  // Ensure solid white background
                boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.25)'  // Stronger shadow
            }}>
                <div className="dialog-content">
                    <div className="subheading-1">Are you sure you want to reset your account?</div>
                    <p className="description">This will clear your portfolio and reset your funds to the initial amount. This action cannot be undone.</p>
                </div>
                <div className="dialog-buttons">
                    <button className="cancel-button" onClick={onCancel}>
                        Cancel
                    </button>
                    <button 
                        onClick={onReset} 
                        style={{ backgroundColor: '#f44336' }}  // Red button for destructive action
                    >
                        Reset Account
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ResetDialog;
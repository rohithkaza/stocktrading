import { useState, useEffect } from 'react';
import "./notification.css";
import '../styles.css';
import confirmation from '../assets/confirmation.svg';

const ConfirmationMessage = () => {
    const [visible, setVisible] = useState(true);
    const [slideOut, setSlideOut] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            setSlideOut(true); 
            setTimeout(() => {
                setVisible(false); 
            }, 500);
        }, 3000);

        return () => clearTimeout(timer);
    }, []);

    return (
        visible && (
            <div
                className={`message-container ${slideOut ? 'slide-out' : ''}`}
                style={{ background: "#78C778" }}
            >
                <div className="message-box">
                    <div className="message-content">
                        <img src={confirmation} alt="confirmation" />
                        <p>Your request was successfully processed!</p>
                    </div>
                </div>
            </div>
        )
    );
};

export default ConfirmationMessage;

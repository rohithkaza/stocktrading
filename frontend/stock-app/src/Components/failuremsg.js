import { useState, useEffect } from 'react';
import "./notification.css";
import '../styles.css';
import failure from '../assets/failure.svg';

const FailureMessage = () => {
    const [visible, setVisible] = useState(true);
    const [slideOut, setSlideOut] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            setSlideOut(true); // start slide out
            setTimeout(() => {
                setVisible(false); // unmount after animation
            }, 500); // match the CSS animation duration
        }, 3000); // time before sliding out

        return () => clearTimeout(timer);
    }, []);

    return (
        visible && (
            <div
                className={`message-container ${slideOut ? 'slide-out' : ''}`}
                style={{ background: "#FFB5B5" }}
            >
                <div className="message-box">
                    <div className="message-content">
                        <img src={failure} alt="failure" />
                        <p>There was an error trying to process your request. Please try again.</p>
                    </div>
                </div>
            </div>
        )
    );
};

export default FailureMessage;

import React, { useRef, useState } from 'react';

const TooltipLink = ({ tooltipText, triggerText }) => {
  const [showTooltip, setShowTooltip] = useState(false);
  const hideTimeout = useRef(null);

  const handleMouseEnter = () => {
    clearTimeout(hideTimeout.current); // Cancel any scheduled hide
    setShowTooltip(true);
  };

  const handleMouseLeave = () => {
    hideTimeout.current = setTimeout(() => {
      setShowTooltip(false);
    }, 200); // Only hide after delay
  };

  return (
    <span
      style={{ position: 'relative', display: 'inline-block', color: 'blue' }}
      onMouseEnter={() => handleMouseEnter()}
      onMouseLeave={() => handleMouseLeave()}
    >
    {triggerText}
      {showTooltip && (
        <div
          onMouseEnter={() => handleMouseEnter()}
          onMouseLeave={() => handleMouseLeave()}
          style={{
            position: 'absolute',
            bottom: '120%',
            left: '0',
            backgroundColor: '#333',
            color: '#fff',
            padding: '6px 8px',
            borderRadius: '4px',
            fontSize: '14px',
            whiteSpace: 'nowrap',
            zIndex: 1000,
            userSelect: 'text'
          }}
        >
          {tooltipText}
        </div>
      )}
    </span>
  );
};

export default TooltipLink;
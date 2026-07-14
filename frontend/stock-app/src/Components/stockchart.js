import React from 'react';
import Plot from 'react-plotly.js';
import '../styles.css';

const StockChart = ({ ohlcData, handleChartIncrementClick, activeIncrement }) => {
    const barColors = ohlcData.map(d => d.close > d.open ? 'rgba(0, 200, 0, 0.4)' : 'rgba(200, 0, 0, 0.4)') // 40% opacity;

    // trace for OHLC candlestick chart
    const candlestickTrace = {
        x: ohlcData.map(d => d.date),
        open: ohlcData.map(d => d.open),
        high: ohlcData.map(d => d.high),
        low: ohlcData.map(d => d.low),
        close: ohlcData.map(d => d.close),
        increasing: { line: { color: 'green' } },
        decreasing: { line: { color: 'red' } },
        type: 'candlestick',
        xaxis: 'x',
        yaxis: 'y1',
        name: 'OHLC Data',
        showlegend: true,
        legendgroup: 'ohlc'
    };

    // trace for trading volume histogram
    const volumeTrace = {
        x: ohlcData.map(d => d.date),
        y: ohlcData.map(d => d.volume),
        type: 'bar',
        marker: {
            color: barColors,
        },
        xaxis: 'x',
        yaxis: 'y2',
        hovertemplate: '%{y}',
        name: 'Trading Volume',
        showlegend: true,
        legendgroup: 'volume'
    };

    return (
        <div>
            <div className="chart-increment-container">
                {['D', 'W', 'M'].map((interval) => (
                    <button
                        key={interval}
                        className={`chart-increment-btn ${activeIncrement === interval ? 'active' : ''}`}
                        onClick={() => handleChartIncrementClick(interval)}
                    >
                        {interval === 'D' ? 'Day' : interval === 'W' ? 'Week' : 'Month'}
                    </button>
                ))}
            </div>

        <Plot
            data={[candlestickTrace, volumeTrace]}
            layout={{
                margin: {
                    t: 0,
                },
                height: 390,
                xaxis: {
                    title: 'Date',
                    type: 'category',
                    domain: [0, 1], // puts x axis @ bottom (below vol)
                    tickmode: 'auto',
                    nticks: 5,
                    anchor: 'y2',
                    rangeslider: { visible: false },
                },
                yaxis: {
                    title: 'Price',
                    domain: [0.2, 1], // puts ohlc at top
                    tickmode: 'auto',
                    autorange: true,
                },
                yaxis2: {
                    title: 'Volume',
                    domain: [0, 0.15], // put vol at bottom
                    showgrid: false,
                    autorange: true,
                    tickmode: 'auto',
                    nticks: 3
                },
                width: 800,
                legend: {
                    orientation: 'v', 
                    y: 0,          
                    x: 4,            
                    xanchor: 'right',
                    // yanchor: 'top',   
                    bgcolor: 'rgba(255, 255, 255, 0.5)',
                    bordercolor: '#E2E2E2',
                    borderwidth: 1
                }
            }}
        />
        </div>
        
    );
};

export default StockChart;

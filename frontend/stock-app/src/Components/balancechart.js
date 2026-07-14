import React from 'react';
import Plot from 'react-plotly.js';
import '../styles.css';

const BalanceChart = ({ balanceChartData }) => {

    // balanceChartData = { // dummy data for now, bc fetch errors :')
    //     "industry 1": 1,
    //     "industry 2": 2,
    //     "industry 3": 3,
    //     "industry 4": 4,
    //     "industry 5": 5,
    // };

    const labels = Object.keys(balanceChartData);
    const values = Object.values(balanceChartData);

    return (
        <div className="balance-chart-container">
            <Plot
                data={[
                    {
                        type: 'pie',
                        labels: labels,
                        values: values,
                        hole: 0.3,
                        textinfo: 'label',
                        hoverinfo: 'label+value+percent',
                    },
                ]}
                layout={{
                    height: 425,
                    width: 350,
                    showlegend: false,
                    paper_bgcolor: 'rgba(0, 0, 0, 0)',
                    plot_bgcolor: 'rgba(0, 0, 0, 0)',
                    margin: {
                        t: 0,
                        b: 0,
                        l: 0,
                        r: 0,
                    },
                }}
            />
        </div>
    );
};

export default BalanceChart;

import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import './stocktable.css';
import '../styles.css';
import Cookies from 'js-cookie';
import searchIcon from '../assets/search.svg';
import closeIcon from '../assets/close.svg';
import filterDateIcon from '../assets/calendar-range.svg'

const Orderhistorytable = () => {
    const navigate = useNavigate();
    const [transactions, setTransactions] = useState([]);
    const [stockNames, setStockNames] = useState({});
    const [showFilter, setShowFilter] = useState(false);
    const [filterText, setFilterText] = useState('');
    const [filteredTransactions, setFilteredTransactions] = useState([]);
    const [filterModalOpen, setFilterModalOpen] = useState(false);
    const [filterMode, setFilterMode] = useState('exact'); // or 'range'
    const [dateLabel, setDateLabel] = useState(''); // label text (e.g. "Exact: 2025-04-17" or "Range: 2025-04-17 -- 2025-04-18")
    const [filterDate, setFilterDate] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [pendingFilter, setPendingFilter] = useState({
        filterDate: '',
        startDate: '',
        endDate: '',
    }); // store user inputs before they click "apply"
    const token = Cookies.get('token');

    /**
     * Converts a time string to CST
     * @param {string} dateStr - The date string (e.g., "04/17/2025")
     * @param {string} timeStr - The time string (e.g., "15:16")
     * @returns {string} The time formatted in CST timezone
     */
    const convertToCST = (dateStr, timeStr) => {
        // Create a date object from the date and time strings
        // Format from backend is MM/DD/YYYY for date and HH:MM for time
        const [month, day, year] = dateStr.split('/');
        const [hours, minutes] = timeStr.split(':');

        // Create date object (assuming UTC timezone for the original date)
        const date = new Date(Date.UTC(parseInt(year), parseInt(month) - 1, parseInt(day), parseInt(hours), parseInt(minutes)));

        // Convert to CST (America/Chicago)
        return new Intl.DateTimeFormat('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            timeZone: 'America/Chicago', // CST timezone
            hour12: true // Use 12-hour format with AM/PM
        }).format(date);
    };

    // Fetch full name for a single stock symbol
    const fetchName = (symbol) => {
        // return fetch("http://localhost:8080/stock/stockname", {
        return fetch("https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/stock/stockname", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'Authorization': `Bearer ${token}`,
            },
            credentials: "include",
            body: JSON.stringify({stockSymbol: symbol}),
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("fail to fetch the stock name change...");
                }
                return res.json();
            })
            .then((data) => data.name);
    };

    // Fetch full names for each symbol and return them as a map
    const fetchAllStockNames = (symbols) => {
        const requests = symbols.map((symbol) =>
            fetchName(symbol).then((name) => ({symbol, name}))
        );
        return Promise.all(requests).then((results) => {
            const nameMap = {};
            results.forEach(({symbol, name}) => {
                nameMap[symbol] = name;
            });
            return nameMap;
        });
    };

    // Fetch all transaction history
    useEffect(() => {
        fetch('https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/user/history', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Transaction history request failed: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("Transaction history successful");
                console.log(data);
                setTransactions(data); // ← Already parsed from .json()
                setFilteredTransactions(data);
                const uniqueSymbols = [...new Set(data.map(item => item.symbol))];
                console.log("this is uniquesymbols", uniqueSymbols)
                return Promise.all([
                    fetchAllStockNames(uniqueSymbols)
                ]);
            })
            .then(([nameMap]) => {
                setStockNames(nameMap);
            })
            .catch(error => {
                console.error("Error fetching transaction history:", error);
            });
    }, []);

    // Filter transactions based on search text and dates
    useEffect(() => {
        const searchTermLower = filterText.toLowerCase();
        const filtered = transactions.filter(transaction => {
            const symbol = transaction.symbol.toLowerCase();
            const stockName = stockNames[transaction.symbol]?.toLowerCase() || '';
            const matchesText = symbol.includes(searchTermLower) || stockName.includes(searchTermLower);

            // Convert MM/DD/YYYY to Date object
            const [month, day, year] = transaction.date.split('/');
            const transactionDate = new Date(`${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`);
            let matchesDate = true;

            if (filterMode === 'exact' && filterDate) {
                const targetDate = new Date(filterDate);
                matchesDate = transactionDate.getTime() === targetDate.getTime(); // use timestamp to avoid timezone mismatch
            }

            if (filterMode === 'range') {
                const start = startDate ? new Date(startDate) : null;
                // console.log("startDate:", start)
                const end = endDate ? new Date(endDate) : null;
                // console.log("endDate:", end)
                const afterStart = start ? transactionDate >= start : true;
                // console.log("afterStart:", afterStart)
                const beforeEnd = end ? transactionDate <= end : true;
                // console.log("beforeEnd:", beforeEnd)
                matchesDate = afterStart && beforeEnd;
            }

            return matchesText && matchesDate;
        });

        setFilteredTransactions(filtered);
    }, [filterText, filterMode, filterDate, startDate, endDate, transactions, stockNames]);


    // Preload current filter values into pendingFilter when filter modal opens
    useEffect(() => {
        if (filterModalOpen) {
            setPendingFilter({
                filterDate,
                startDate,
                endDate,
            });
        }
    }, [filterModalOpen]);

    /**
     * Navigates to the stock detail page
     *
     * @param {string} symbol - The stock symbol (e.g., "AAPL").
     * @param {string} name - The full name of the stock (e.g., "Apple Inc.").
     */
    const handleRowClick = (symbol, name) => {
        navigate(`/stock/${symbol}/${name}`, {state: {referrer: '/dashboard'}});
    };


    /**
     * Toggles the visibility of the filter input section
     */
    const toggleFilter = () => {
        setShowFilter(!showFilter);
        if (showFilter) {
            setFilterText('');
            setFilterDate('');
        }
    };

    return (
        <div className="table-container">
            <div className="filter-container">
                <div className="header-row">
                    <div className="filter-button-cell">
                        <button
                            className="filter-button"
                            onClick={toggleFilter}
                            title={showFilter ? "Close filter" : "Filter by stock"}
                        >
                            {showFilter ? 'Close' : 'Filter'}
                        </button>
                    </div>
                    <div className="name-cell">
                        <div className="name-text">
                            <div className="subheading-2 description">Name</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                    <div className="shares-cell">
                        <div className="shares-text">
                            <div className="subheading-2 description">Date</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                    <div className="value-cell">
                        <div className="value-text">
                            <div className="subheading-2 description">Action</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                    <div className="value-cell">
                        <div className="value-text">
                            <div className="subheading-2 description">Price</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                    <div className="value-cell">
                        <div className="value-text">
                            <div className="subheading-2 description">Shares</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                    <div className="value-cell">
                        <div className="value-text">
                            <div className="subheading-2 description">Total</div>
                        </div>
                        <div className="divider"></div>
                    </div>
                </div>

                {showFilter && (
                    <div className="search-filter-container">
                        {/* Filter by stock name/symbol */}
                        <div className="search-input-wrapper full-width">
                            <img src={searchIcon} alt="Search" className="search-icon"/>
                            <input
                                type="text"
                                value={filterText}
                                onChange={(e) => setFilterText(e.target.value)}
                                placeholder="Filter by stock name or symbol"
                                className="search-filter-input with-filter-icon"
                            />
                            <img
                                src={filterDateIcon}
                                alt="Filter"
                                className="filter-inline-icon calendar-large"
                                onClick={() => setFilterModalOpen(true)}
                            />
                            {filterText && (
                                <img
                                    src={closeIcon}
                                    alt="Clear search"
                                    className="clear-search-icon"
                                    onClick={() => setFilterText('')}
                                />
                            )}
                        </div>

                        {/* Filter-by-date Labels */}
                        {dateLabel && (
                            <div className="active-filter-label">
                                {dateLabel}
                                <img
                                    src={closeIcon}
                                    alt="Remove date filter"
                                    className="remove-label-icon"
                                    onClick={() => {
                                        setDateLabel('');
                                        setFilterDate('');
                                        setStartDate('');
                                        setEndDate('');
                                    }}
                                />
                            </div>
                        )}
                    </div>
                )}

                {filterModalOpen && (
                    <div className="filter-modal">
                        <div className="filter-modal-content">
                            <h3 className="filter-modal-title">Filter Transactions by Date</h3>
                            <div className="filter-section">
                                <label className="filter-label">Exact Date</label>
                                {/* Exact Date Filter*/}
                                <input
                                    type="date"
                                    value={pendingFilter.filterDate}
                                    onChange={(e) =>
                                        setPendingFilter({
                                            filterDate: e.target.value,
                                            startDate: '', // clear range if exact is selected
                                            endDate: ''
                                        })
                                    }
                                    disabled={!!pendingFilter.startDate || !!pendingFilter.endDate} // disable if range selected
                                    className="filter-input-field"
                                />
                            </div>

                            <div className="filter-section">
                                <label className="filter-label">Date Range</label>
                                <div className="range-fields">
                                    {/* Date Range Filter*/}
                                    <input
                                        type="date"
                                        value={pendingFilter.startDate}
                                        onChange={(e) => {
                                            const newStart = e.target.value;
                                            const newEnd = pendingFilter.endDate && pendingFilter.endDate < newStart
                                                ? ''  // if new start is after old end, reset end
                                                : pendingFilter.endDate;
                                            setPendingFilter({
                                                filterDate: '',  // clear exact date if using range
                                                startDate: newStart,
                                                endDate: newEnd
                                            });
                                        }}
                                        disabled={!!pendingFilter.filterDate} // disable if exact selected
                                        className="filter-input-field"
                                    />
                                    <span className="to-label">to</span>
                                    <input
                                        type="date"
                                        value={pendingFilter.endDate}
                                        min={
                                            pendingFilter.startDate
                                                ? new Date(new Date(pendingFilter.startDate).getTime() + 86400000) // +1 day in ms
                                                    .toISOString()
                                                    .split('T')[0]
                                                : ''
                                        } // Set min to startDate + 1 to prevent end <= start
                                        onChange={(e) =>
                                            setPendingFilter({
                                                ...pendingFilter,
                                                endDate: e.target.value
                                            })
                                        }
                                        disabled={!!pendingFilter.filterDate}
                                        className="filter-input-field"
                                    />
                                </div>
                            </div>

                            {/* Action & Close Buttons */}
                            <div className="modal-button-row">
                                <button
                                    className="apply-button"
                                    // Transfer pending states to actual filter state
                                    onClick={() => {
                                        setFilterDate(pendingFilter.filterDate);
                                        setStartDate(pendingFilter.startDate);
                                        setEndDate(pendingFilter.endDate);

                                        if (pendingFilter.filterDate) {
                                            setFilterMode('exact');
                                            setDateLabel(`Exact: ${pendingFilter.filterDate}`);
                                        } else if (pendingFilter.startDate && pendingFilter.endDate) {
                                            setFilterMode('range');
                                            setDateLabel(`Range: ${pendingFilter.startDate} – ${pendingFilter.endDate}`);
                                        }

                                        setFilterModalOpen(false);
                                    }}
                                    disabled={
                                        !pendingFilter.filterDate &&
                                        !(pendingFilter.startDate && pendingFilter.endDate)
                                    }
                                >
                                    Apply
                                </button>

                                <button
                                    className="close-button"
                                    onClick={() => setFilterModalOpen(false)}
                                >
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* Dynamically render transaction history */}
            <div className="scrollable-body">
                {filteredTransactions.map((transaction, index) => (
                    <div
                        key={index}
                        className="row"
                        onClick={() => handleRowClick(transaction.symbol, stockNames[transaction.symbol])}
                        style={{cursor: 'pointer'}}
                    >
                        <div className="padding-cell"></div>
                        <div className="name-cell">
                            <div className="name-text">
                                <div className="name-text-inside">
                                    <div className="subheading-2">{transaction.symbol}</div>
                                    <p className="description">{stockNames[transaction.symbol]}</p>
                                </div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="shares-cell">
                            <div className="shares-text" style={{textAlign: 'right'}}>
                                <div className="subheading-2" style={{textAlign: 'right', width: '100%'}}>
                                    {transaction.date}, {convertToCST(transaction.date, transaction.time)}
                                </div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text" style={{textAlign: 'right'}}>
                                <div
                                    className={`subheading-2 ${transaction.action === 'Buy' ? 'positive' : 'negative'}`}>
                                    {/* <div className={`subheading-2 ${transaction.action === 'Buy' ? 'positive' : 'negative'}`} style={{ textAlign: 'right', width: '100%' }}> */}
                                    {transaction.action}
                                </div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text" style={{textAlign: 'right'}}>
                                <div className="subheading-2"
                                     style={{textAlign: 'right', width: '100%'}}>{transaction.price}</div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text" style={{textAlign: 'right'}}>
                                <div className="subheading-2"
                                     style={{textAlign: 'right', width: '100%'}}>{transaction.shares}</div>
                            </div>
                            <div className="divider"></div>
                        </div>
                        <div className="value-cell">
                            <div className="value-text" style={{textAlign: 'right'}}>
                                <div className="subheading-2"
                                     style={{textAlign: 'right', width: '100%'}}>{transaction.total}</div>
                            </div>
                            <div className="divider"></div>
                        </div>
                    </div>
                ))}

                {/* Show a message if no transactions */}
                {filteredTransactions.length === 0 && (
                    <div className="row">
                        <div className="empty-table" style={{textAlign: 'center', width: '100%'}}>
                            <div className="subheading-2-table">
                                {filterText || filterDate ? 'No matching orders found for searched criteria.' : 'You have no order history.'}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>

    );

};

export default Orderhistorytable;
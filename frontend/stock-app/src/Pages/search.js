import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import "../styles.css";
import "./search.css";
import nosearch from '../assets/nosearch.svg';
import Navbar from '../Components/navbar';
import Footer from '../Components/footer';
import Searchstocktable from '../Components/searchstocktable';
import Cookies from "js-cookie";

const Search = ({ userName, onLogout }) => {
    const navigate = useNavigate();
    const location = useLocation();

    // Initialize searchTerm from location state if available
    const initialSearchTerm = location.state?.searchTerm || '';
    const [isLoading, setIsLoading] = useState(false);
    const [noResults, setNoResults] = useState(false);
    const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
    const [displayedSearchResults, setDisplayedSearchResults] = useState([]);
    const [fetchInfo, setFetchInfo] = useState({fetchSearchTerm: null, fetchedSearchResults: null, error: null});
    const [displayResultsHeader, setDisplayResultsHeader] = useState('');
    const fetchTimeout = useRef(null);
    const [displayError, setDisplayError] = useState(null);

    // Popular stocks to show when no search is performed
    const popularStocks = [
        { symbol: 'AAPL', name: 'Apple Inc.', price: 234.56, change: 2.34, isPositive: true },
        { symbol: 'MSFT', name: 'Microsoft Corporation', price: 417.88, change: 1.23, isPositive: true },
        { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 163.45, change: -0.67, isPositive: false },
        { symbol: 'AMZN', name: 'Amazon.com, Inc.', price: 187.23, change: 3.45, isPositive: true },
        { symbol: 'TSLA', name: 'Tesla, Inc.', price: 190.30, change: -2.10, isPositive: false },
    ];

    //Run a search if we have an initial search term on component mount
    // useEffect(() => {
    //     if (initialSearchTerm.trim() !== '') {
    //         fetchStockResults(initialSearchTerm);
    //     }
    // }, [initialSearchTerm]);

    // Fetch search results whenever the search term changes
    useEffect(() => {
        console.log("effect triggered");
        clearTimeout(fetchTimeout.current);
        // Clear any previous errors
        setDisplayError(null);

        // Set loading state
        setIsLoading(true);
        setNoResults(false);
        setDisplayResultsHeader('');

        // Don't search if the search term is empty - show most active instead
        // Else, search for the search term.
        if (!searchTerm || searchTerm.trim() === '') {
            console.log("active stock fetch with searchTerm: "+searchTerm);
            fetchTimeout.current = setTimeout(() => {
                fetchMostActiveStockResults();
            }, 600);
        } else {
            fetchTimeout.current = setTimeout(() => {
                fetchStockResults(searchTerm);
            }, 600);
        }
    }, [searchTerm]);

    // When search results are fetched, check if original search term matches current, then display.
    // Note since fetchSearchTerm is initialzied to null, this won't do anything on initialization.
    useEffect(() => {
        if(fetchInfo.fetchSearchTerm===null)
            return;
        if(fetchInfo.fetchSearchTerm.trim()==='' && searchTerm.trim()==='') {
            // Display most active stock results
            setDisplayedSearchResults(fetchInfo.fetchedSearchResults);
            setDisplayResultsHeader("Most Active Stocks: ");
        }
        else if(fetchInfo.fetchSearchTerm==searchTerm) { //fetchSearchTerm will corr. to fetchSearchResults since they are updated in the same synchronous block
            // Display results
            setDisplayedSearchResults(fetchInfo.fetchedSearchResults);
            setDisplayResultsHeader(`Search Results for \"${searchTerm}\"`)
        } else {
            return;
        }
        // This will only execute if the if or else if are met, i.e. if the displayed search term matches the fetched one.
        setDisplayError(fetchInfo.error);
        setNoResults(fetchInfo.fetchedSearchResults.length === 0);
        setIsLoading(false);
    }, [fetchInfo]);

    // Function to fetch stock results from the backend
    const fetchStockResults = async (term) => {
        console.log("fetchStockResults start");
        try {
            const token = Cookies.get('token');
            // Make API call to the backend endpoint for stock search
            // Keep the localhost URL as requested
            const response = await fetch(`https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/api/stock/search?prefix=${encodeURIComponent(term)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                credentials: 'include',  // Important for including auth cookies
            });

            console.log("fetch response gotten");

            if (!response.ok) {
                throw new Error(`Failed to fetch stock data: ${response.status}`);
            }

            const data = await response.json();
            console.log("Search results:", data);

            // Process the data into the format expected by the SearchStockTable
            const formattedResults = data.map(stock => {
                return {
                    symbol: stock.symbol,
                    name: stock.name || `${stock.symbol} Inc.`, // Fallback if name is missing
                    price: parseFloat(stock.price) || 0,
                    change: parseFloat(stock.change) || 0,
                    isPositive: stock.isPositive !== undefined ? stock.isPositive : (stock.change >= 0)
                };
            });

            setFetchInfo(_ => ({fetchedSearchResults: formattedResults, fetchSearchTerm: term, error: null}));
        } catch (error) {
            console.error('Error fetching stock data:', error);

            // If API fails, use a fallback search for known stocks (client-side filtering)
            const term = searchTerm.toUpperCase();
            const fallbackResults = popularStocks.filter(stock =>
                stock.symbol.includes(term) || stock.name.toUpperCase().includes(term)
            );

            if (fallbackResults.length > 0) {
                setFetchInfo(_ => ({fetchedSearchResults: fallbackResults, fetchSearchTerm: term, error: "Using offline results - couldn't connect to stock API"}));
            } else {
                setFetchInfo(_ => ({fetchedSearchResults: [], fetchSearchTerm: term, error: error.message}));
            }
        }
    };

    // Function to fetch most active stock results from the backend
    const fetchMostActiveStockResults = async () => {
        try {
            // Make API call to the backend endpoint for stock search
            // Keep the localhost URL as requested
            const response = await fetch(`https://stock-backend-dot-rice-comp-539-spring-2022.uk.r.appspot.com/api/stock/mostactive`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',  // Important for including auth cookies
            });

            console.log("fetch response gotten");

            if (!response.ok) {
                throw new Error(`Failed to fetch stock data: ${response.status}`);
            }

            const data = await response.json();
            console.log("Search results:", data);

            // Process the data into the format expected by the SearchStockTable
            const formattedResults = data.map(stock => {
                return {
                    symbol: stock.symbol,
                    name: stock.name || `${stock.symbol} Inc.`, // Fallback if name is missing
                    price: parseFloat(stock.price) || 0,
                    change: parseFloat(stock.change) || 0,
                    isPositive: stock.isPositive !== undefined ? stock.isPositive : (stock.change >= 0),
                    volume: parseFloat(stock.volume) || 0
                };
            });

            setFetchInfo(_ => ({fetchedSearchResults: formattedResults, fetchSearchTerm: '', error: null}));
        } catch (error) {
            console.error('Error fetching stock data:', error);

            // If API fails, use a fallback search for known stocks (client-side filtering)
            const term = searchTerm.toUpperCase();
            const fallbackResults = popularStocks.filter(stock =>
                stock.symbol.includes(term) || stock.name.toUpperCase().includes(term)
            );

            if (fallbackResults.length > 0) {
                setFetchInfo(_ => ({fetchedSearchResults: fallbackResults, fetchSearchTerm: '', error: "Using offline results - couldn't connect to stock API"}));
            } else {
                setFetchInfo(_ => ({fetchedSearchResults: [], fetchSearchTerm: '', error: error.message}));
            }
        }
    };

    return (
        <div className="search-page">
            <Navbar
                userName={userName}
                onLogout={onLogout}
                onSearchTermChange={setSearchTerm}
                initialSearchTerm={searchTerm} // Pass the current search term to the navbar
            />
            <div className="search-container-outside">
                <div className="search-container-inside">
                    <div className="search-content">
                        {displayError && (
                            <div className="error-message">
                                <p>{displayError}</p>
                            </div>
                        )}

                        {searchTerm ? (
                            <>
                                <div className="subheading-1">{displayResultsHeader}</div>
                                {isLoading ? (
                                    <div className="loading-indicator">Loading...</div>
                                ) : noResults ? (
                                    <div className="no-results">
                                        <img src={nosearch} alt="nosearch" />
                                        <p>No results found. Try again with a different search term.</p>
                                        <p className="search-tip">Try searching for common tickers like AAPL, MSFT, GOOGL, etc.</p>
                                    </div>
                                ) : (
                                    <Searchstocktable
                                        onStockSelect={(symbol, name) => {console.log("sjvkdfvhdbvdfvhbfjdfbvfdjvhbf: "+`/stock/${symbol}/${name}`); navigate(`/stock/${symbol}/${name}`, { state: { referrer: '/search' } })}}
                                        stocks={displayedSearchResults}
                                        displayVolume={false}
                                    />
                                )}
                            </>
                        ) : (
                            <>
                                <div className="subheading-1">{displayResultsHeader}</div>
                                {isLoading ? (
                                    <div className="loading-indicator">Loading...</div>
                                ) : noResults ? (
                                    <div className="no-results">
                                        <img src={nosearch} alt="nosearch" />
                                        <p>No stocks found. Try using a search term.</p>
                                    </div>
                                ) : (
                                    <Searchstocktable
                                        onStockSelect={(symbol, name) => navigate(`/stock/${symbol}/${name}`, { state: { referrer: '/search' } })}
                                        stocks={displayedSearchResults}
                                        displayVolume={true}
                                    />
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Search;
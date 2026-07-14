import "../styles.css";
import "./documentation.css";
import { useNavigate } from 'react-router-dom';
import Header from '../Components/header';
import Footer from '../Components/footer';

const Documentation = () => {
    const navigate = useNavigate();
    
    const handleLogin = () => {
        navigate('/login');
    };

    return (
        <div className="doc-page">
            <Header onLogin={handleLogin} />
            <div className="doc-container-outside">
                <div className="doc-container-inside">
                    <div className="doc-content">
                        <div className="guide-header">
                            <h2>User Guide for InvestExpress</h2>
                        </div>

                        <div className="subheading-1">What is InvestExpress?</div>
                        <p className="description">
                            InvestExpress is a stock trading simulation platform designed to provide users with an engaging, risk-free environment to learn and practice real-world investing. Using virtual money, users can buy and sell actual stocks using real-time market data. The platform mirrors real trading experiences, making it ideal for beginners who want to understand portfolio management or seasoned traders looking to test out new strategies — all without the risk of losing real money.
                        </p>

                        <div className="subheading-1">Getting Started</div>
                        <p className="description">
                            When you first land on the InvestExpress homepage, you'll be welcomed with and explanation of what the platform is all about. From there, you can click the “Log In” button. New users can register by providing an email address and a password. Once registered and logged in, you'll be directed to your personalized dashboard, where you can view your virtual account balance, explore trending stocks, and begin making trades.
                        </p>

                        <div className="subheading-1">Using Your Dashboard</div>
                        <p className="description">
                            Your dashboard is the central hub for all your activity on InvestExpress. It displays your total funds, portfolio value, watchlist and order history, and a detailed breakdown of each stock you own. More detailed descriptions of each feature are provided below. When you're ready to end your session, simply click the “Logout” button at the top of the screen. You can log back in at any time to continue managing your virtual portfolio.
                        </p>

                        <div className="subheading-1">Your Portfolio</div>
                        <p className="description">
                            Every stock you trade and currently own will be avilable in your portfolio. For every stock in your portfolio, you can see the current market price, how many shares you own, and how its value has changed over time.
                        </p>

                        <div className="subheading-1">Industry Pie Chart</div>
                        <p className="description">
                            This chart displays a visual breakdown of your current portfolio by sector. Each slice represents a portion of your total portfolio balance allocated to a specific industry — like Technology, Automobiles, Retail, and Semiconductors. You can view this chart to assess how diversified your stocks are. Hover over each sector to view how much money you have invested into each area.
                        </p>

                        <div className="subheading-1">Using the Watchlist</div>
                        <p className="description">
                            The watchlist feature allows you to keep an eye on stocks that interest you without needing to buy or sell them. On any stock page, you can click “Add to Watchlist,” and the stock will be added to your personal watchlist view. From there, you can monitor the stock's current price and percent change throughout the day in your Dashboard. You can remove stocks from your watchlist at any time.
                        </p>

                        <div className="subheading-1">Order History</div>
                        <p className="description">
                            Every trade you make on InvestExpress is recorded in your personal order history. This log provides a clear breakdown of your trading activity, including the stock, the date and time of the transaction, whether you bought or sold, the number of shares, and the individual price of the stock and total price of your transaction. You can use the order history table to evaluate your standings over time and improve your trading strategy.
                        </p>

                        <div className="subheading-1">Searching for Stocks</div>
                        <p className="description">
                            To search for stocks, simply use the search bar in the navigation menu. As you begin typing a stock name or symbol, a list of relevant and trending stock results will appear. You can click on any of these results to view more detailed information about that particular stock. This is a quick and efficient way to find stocks you're interested in and evaluate their potential value before making a trade.
                        </p>

                        <div className="subheading-1">Exploring Stocks</div>
                        <p className="description">
                            When you click on a stock, you'll be taken to the stock's detail page. Here, you can view its current price, daily price change, and percentage change. You'll also see an interactive trend chart showing you how the stock has performed over different time periods — daily, weekly, or monthly. Below the chart, a short description offers information about the company and the industry sector. There is also a news section, where you can view financial news related to the stock to futher inform you about the stock and its current standing. Lastly, you'll find options to buy or sell shares of the stock, which is described in more detail below, as well as the ability to add or remove the stock from your personal watchlist.
                        </p>

                        <div className="subheading-1">Buying and Selling Stocks</div>
                        <p className="description">
                            To make a trade, navigate to the desired stock page. If you want to buy, simply enter the number of shares you'd like to purchase and confirm the transaction. Assuming you have enough money and/or shares, the cost will be deducted from your virtual balance, and your portfolio balance will also be updated. If you already own shares and wish to sell them, you can access the stock through your dashboard or the search function. Once you input the number of shares to sell and confirm the sale, your funds will update again.
                        </p>

                        <div className="subheading-1">Security and Privacy</div>
                        <p className="description">
                            InvestExpress is built with user privacy and security in mind. All account credentials, including passwords, are encrypted and securely stored. Only you have access to your account, portfolio, and transaction history. The platform makes sure to ensure your virtual trading experience remains protected. And since no real money is involved, there's no financial risk — so start making those trades!
                        </p>

                        <div className="subheading-1">Tips for Success</div>
                        <p className="description">
                            As you use InvestExpress, take the opportunity to explore different investment strategies. Use the trend charts and market data to guide your trades, and don't be afraid to experiment — if you ever want to start from scratch, you can reset your account, and all your trades, in your Dashboard! Monitor your portfolio regularly to track your performance, adjust your holdings, and learn from your past trades. Over time, you'll gain confidence and insight that can help you invest in the real-world!
                        </p>
                    </div>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Documentation;

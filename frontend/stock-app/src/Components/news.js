import './news.css';
import '../styles.css';
import newsarrow from '../assets/newsarrow.svg';
import newsdefault from '../assets/newsdefault.png';

const News = ({ article: { headline, summary, updated_at, images, url } }) => {
    const imageUrl = images.find(img => img.size === 'small')?.url || newsdefault;
    const decodeHTML = (text) => {
        return text
            ?.replace(/&#39;/g, "'")
            ?.replace(/&amp;/g, '&')
            .replace(/&#34;/g, '"');
    };
    
    const safeHeadline = decodeHTML(headline);
    const safeSummary = decodeHTML(summary) || '\n\n\n';

    return (
        <div className="news-container">
            <div className="news-image">
                <img
                    src={imageUrl}
                    alt="news image"
                    style={{
                        width: '100%',
                        height: '100%',
                        objectFit: 'cover',
                        borderRadius: '15.142px 15.142px 0px 0px',
                    }}
                />
            </div>
            <div className="news-content">
                <div className="news-header">
                    <div className="subheading-1 headline-truncate">{safeHeadline}</div>
                    <p style={{ fontStyle: "italic" }}>
                        Last Updated on {new Date(updated_at).toLocaleDateString()}
                    </p>
                </div>
                <div className="news-body">
                    <p className="description description-truncate">{safeSummary}</p>
                </div>
                <div className="news-button">
                    <a href={url} target="_blank" rel="noopener noreferrer" className="button-text">Read More</a>
                    <img src={newsarrow} alt="newsarrow" />
                </div>
            </div>
        </div>
    );
};

export default News;

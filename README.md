# COMP413-GroupA-Stocks
# 📈 Stock Investment Backend

The backend is built using:
- **Java 17**
- **Spring Boot**
- **Firebase Firestore** (as the database)
- **Alpaca API** (for stock data)

## 🚀 **Getting Started (Local Setup)**
### **1️⃣ Clone the Repository**
```sh
git clone <YOUR_GITHUB_REPO_URL>
cd stock-backend

java -version  # Should be 17 or later
mvn -version   # Ensure Maven is installed


Since Firebase credentials are not stored in GitHub, you need to set up your own firebase-service-account.json file. Its the key used to acess firebase


Place it inside: src/main/resources/firebase-service-account.json


After setting up Firebase, start the backend:
cd stock-backend
mvn clean install
mvn spring-boot:run

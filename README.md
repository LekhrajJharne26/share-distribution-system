Multi-Level Share Distribution System

This is a Spring Boot project developed as part of a technical assignment.
It simulates a hierarchical share distribution system involving multiple roles — Owner, Operator, Agent, and Customer — where profits and losses are distributed dynamically based on share percentages.

🚀 Tech Stack
Java 17
Spring Boot 3.5
Spring Data JPA
H2 Database (File Mode)
Maven
Postman (for API testing)

📂 Project Structure
src/main/java/com/example/sharedistribution
│
├── controller/          # REST controllers for all entities
├── dto/                 # DTOs for request and response handling
├── entity/              # JPA entities for DB mapping
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic and transaction handling
├── exception/           # Global exception handling
└── ShareDistributionApplication.java  # Main Spring Boot runner

🧩 Business Logic Overview
Roles:-
Owner → Top-level entity managing Operators
Operator → Works under an Owner, manages Agents
Agent → Works under an Operator, manages Customers
Customer → End user who performs trades (profit or loss)

Flow of Trades:-
Profit → flows downward (Owner → Operator → Agent → Customer)
Loss → flows upward (Customer → Agent → Operator → Owner)

Share Distribution Rules:-
Each role keeps a defined percentage of the amount and passes the remainder up/down:
Owner → keeps 10%, gives 90% to Operator
Operator → keeps 10%, gives 80% to Agent
Agent → keeps 20%, gives X% to Customer

You can change these percentages dynamically using APIs.

🧠 Features Implemented

✅ Create and manage entities — Owners, Operators, Agents, and Customers
✅ Define parent-child relationships dynamically
✅ Set or update share configurations between levels
✅ Execute trades (profit/loss) and calculate distribution
✅ Fetch detailed trade reports
✅ Get daily summary of distributed profit/loss

⚙️ How to Run
1️⃣ Clone the project
git clone https://github.com/LekhrajJharne26/share-distribution-system.git
cd share-distribution-system

2️⃣ Build and run
./mvnw spring-boot:run

3️⃣ Access the app

Application URL → http://localhost:8080
H2 Console → http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/sharedb
Username: sa
Password: (leave blank)

🧪 Sample API Endpoints (Postman)
1️⃣ Create Participant
POST /api/participants
{
  "name": "Agent A",
  "type": "AGENT"
}

2️⃣ Link Hierarchy
POST /api/participants/link
{
  "parentId": 1,
  "childId": 2
}

3️⃣ Define Share
POST /api/shares
{
  "parentId": 1,
  "childId": 2,
  "percentage": 90
}

4️⃣ Execute Trade
POST /api/trades
{
  "customerId": 5,
  "amount": 1000,
  "profit": true
}

5️⃣ Get Trade Details
GET /api/trades/{tradeId}

6️⃣ Daily Summary
GET /api/trades/summary?date=2025-11-10

 Example Tables Created
Table Name	Description:-
PARTICIPANT =	Stores all Owners, Operators, Agents, and Customers
HIERARCHY_LINK =	Stores parent-child relationships
SHARE_CONFIG =	Stores percentage share between two roles
TRADE =	Stores executed trade information
TRADE_DISTRIBUTION =	Stores calculated distribution results per trade



📄 License

This project is created as part of a coding assignment and is free for review and educational use.

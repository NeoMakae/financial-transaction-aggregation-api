# financial-transaction-aggregation-api
A production-grade transaction aggregation service built with Spring Boot that ingests data from multiple sources, normalizes and categorizes transactions, and exposes scalable REST APIs for querying financial insights.


#Clone the repository

git clone https://github.com/<your-username>/financial-transaction-aggregation-api.git

cd financial-transaction-aggregation-api




# Key Features

Aggregate transactions by category, date, or total sum

Filter by customer ID, category, or date range

Pagination & sorting for scalable queries

Caching for faster repeated requests

Dockerized for easy deployment

Fully tested with unit tests (controller & service)

Swagger documentation available http://localhost:8080/swagger-ui.html



# Run the Project With Docker:

docker build -t financial-transaction-api .

docker run -p 8080:8080 financial-transaction-api

# Optional Docker Compose (with Postgres):

docker-compose up --build
API Docs

# Swagger UI: http://localhost:8080/swagger-ui.html

Testing
./mvnw test

# InvestTrack

A comprehensive investment management system with Monte Carlo simulation, backtesting capabilities, and real-time market data integration.

## Features

- **Portfolio Management**: Create and manage multiple investment portfolios
- **Real-time Market Data**: Integration with Alpha Vantage and Yahoo Finance APIs
- **Monte Carlo Simulation**: Simulate potential portfolio outcomes based on historical data
- **Backtesting Engine**: Test investment strategies against historical market data
- **Financial Metrics**: Calculate Sharpe ratio, volatility, max drawdown, and more
- **Custom Alerts**: Set up personalized notifications for price movements and portfolio events
- **Interactive Dashboard**: Rich visualizations of portfolio performance and metrics

## Tech Stack

### Backend
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.4.x**: Enterprise-grade framework
- **Spring Data JPA**: Database abstraction layer
- **PostgreSQL**: Relational database for portfolio data
- **Maven**: Dependency management and build tool

### Frontend
- **Angular**: Modern SPA framework
- **TypeScript**: Type-safe JavaScript
- **Chart.js / D3.js**: Data visualization libraries
- **Angular Material**: UI component library

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 14+

## Project Structure

```
invest-track/
  backend/
    src/
      main/
        java/
          com/github/hnrdejesus/invest_track/
            config/
            controller/
            domain/
            dto/
            repository/
            service/
            InvestTrackApplication.java
        resources/
          application.yml
          application-dev.yml
      test/
    pom.xml
  frontend/
    src/
      app/
      assets/
      environments/
    angular.json
    package.json
  .gitattributes
  .gitignore
  README.md
```

## Getting Started

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/hnrdejesus/invest-track.git
cd invest-track
```

2. Configure database in `backend/src/main/resources/application-dev.yml`

3. Run the application:
```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Frontend Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Start the development server:
```bash
ng serve
```

The application will be available at `http://localhost:4200`

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Running Tests

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
ng test
```

## Key Financial Concepts

### Sharpe Ratio
Measures risk-adjusted return of an investment:
```
Sharpe Ratio = (Portfolio Return - Risk-free Rate) / Portfolio Standard Deviation
```

### Monte Carlo Simulation
Probabilistic technique that uses random sampling to estimate possible outcomes of portfolio performance.

### Backtesting
Historical simulation of a trading strategy using past market data to evaluate its viability.

## Environment Variables

Create a `.env` file or `application-local.properties`:

```properties
# API Keys
ALPHA_VANTAGE_API_KEY=your_key_here
YAHOO_FINANCE_API_KEY=your_key_here

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/investtrack_dev
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting, semicolons, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Henrique de Jesus - [GitHub](https://github.com/hnrdejesus)

## Acknowledgments

- Alpha Vantage for market data API
- Yahoo Finance for additional financial data
- Spring Boot community for excellent documentation
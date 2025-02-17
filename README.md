# Charging Stations in Slovenia

This project collects and updates data on electric vehicle charging stations in Slovenia. It fetches data from multiple
providers, compares it with previous data, and sends an email with new chargers when changes are detected. The application
runs on **Quarkus** and stores data in **PostgreSQL**.

## Features

- Fetches charging station data from multiple providers
- Compares with previous dataset
- Sends an email with new charging station for each provider
- Runs in **Quarkus** with **Docker** and **Flyway migrations**

## Getting Started

### Prerequisites

- **Docker** & **Docker Compose** installed
- **Java 21** (if running locally without Docker)

### Setup

#### 1️⃣ Clone the Repository

```sh
git clone https://github.com/your-repo/charging-stations.git
cd charging-stations
```

#### 2️⃣ Set Up Environment Variables

Create a `.env` file from .env.example or modify `docker-compose.yml` directly.

#### 3️⃣ Build the Application

```sh
./mvnw package
```


#### 3️⃣ Start the Application

```sh
docker-compose up --build
```

### Database Schema & Migration

#### **Flyway Migration**

The database schema is managed by **Flyway**, and migrations are executed automatically on startup.

### API Providers

The following providers are currently supported:

- **GremoNaElektriko**
- **Petrol**
- **MoonCharge**
- **eFrend**
- **MegaTel**

To be implemented if needed later:
- **Avant2Go**
- **Implera**

### Running in Development Mode

You can run Quarkus in dev mode outside of Docker:

```sh
./mvnw quarkus:dev
```

For any issues, feel free to submit a **GitHub Issue** or contribute via **Pull Requests**! 🚀


# STK Push - M-Pesa Daraja Integration

A Spring Boot application that integrates with Safaricom's Daraja API to enable M-Pesa STK Push payments.

## Tech Stack

- **Java 17+**
- **Spring Boot 4.0.3**
- **Spring Data JPA**
- **MySQL**
- **Lombok**
- **Spring Boot DevTools**

## Features

- Daraja OAuth2 access token generation
- STK Push (Lipa Na M-Pesa) initiation
- Safaricom payment callback handling
- Transaction status check (local DB + Daraja query)
- MySQL transaction persistence

## Project Structure

```
com.hezron.stkpush
├── config/
│   ├── AppConfig.java          # RestTemplate bean
│   └── DarajaConfig.java       # Daraja credentials config
├── controller/
│   └── MpesaController.java    # REST endpoints
├── dto/
│   ├── MpesaCallback.java      # Safaricom callback structure
│   ├── StkPushRequest.java     # Incoming STK push request
│   └── StkPushResponse.java    # Daraja STK push response
├── entity/
│   └── MpesaTransaction.java   # DB transaction entity
├── repository/
│   └── MpesaTransactionRepository.java
└── service/
    └── DarajaService.java      # Core Daraja API logic
```

## Prerequisites

- Java 17+
- MySQL running locally
- [Safaricom Daraja sandbox account](https://developer.safaricom.co.ke)
- [ngrok](https://ngrok.com) (for exposing localhost callback URL)

## Configuration

Create your `application.properties` with the following:

or

Copy `application-example.properties` to `application.properties` and fill in your credentials.

```properties
# Server
server.port=8082

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/stk_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Daraja Sandbox
daraja.consumer-key=YOUR_CONSUMER_KEY
daraja.consumer-secret=YOUR_CONSUMER_SECRET
daraja.shortcode=174379
daraja.passkey=bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919
daraja.callback-url=https://your-ngrok-url.ngrok-free.dev/api/mpesa/callback
daraja.auth-url=https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
daraja.stk-push-url=https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest
daraja.stk-query-url=https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query
```

## Database Setup

```sql
CREATE DATABASE mpesa_db;
```

Spring JPA will auto-create the `mpesa_transactions` table on first run.

## Running the App

```bash
mvn spring-boot:run
```

Expose your local server for Safaricom callbacks:

```bash
ngrok http 8082
```

Update `daraja.callback-url` in `application.properties` with the new ngrok URL and restart the app.

## API Endpoints

### Health Check
```
GET /api/mpesa/health
```

### Trigger STK Push
```
POST /api/mpesa/stk-push
Content-Type: application/json

{
  "phoneNumber": "07XXXXXXXX",
  "amount": 1
}
```

### Safaricom Callback (called automatically by Safaricom)
```
POST /api/mpesa/callback
```

### Check Transaction Status
```
GET /api/mpesa/status/{checkoutRequestId}
```

Returns the local DB record. If the transaction is still `PENDING`, it also queries Daraja for the latest status.

## Transaction Statuses

| Status | Meaning |
|---|---|
| `PENDING` | STK push sent, awaiting user action |
| `SUCCESS` | User completed payment (ResultCode: 0) |
| `FAILED` | User cancelled or payment failed |

## Reference Documentation

- [Safaricom Daraja API Docs](https://developer.safaricom.co.ke/docs)
- [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Web](https://docs.spring.io/spring-boot/4.0.3/reference/web/servlet.html)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)

# API Rate Limiter
+ [Abstract](#abstract)

+ [Strategy](#strategy)

  1. [Analysis](#analysis)

    - [Functional requirements](#functional-requirements)

    - [Non-functional requirements](#non-functional-requirements)
    
    - [Capacity estimations](#capacity-estimations)

  2. [System design](#system-design)

    - [Summary](#summary)

    - [HLD - High-Level Design](#hld---high-level-design)
    
    - [Technologies used](#tech-stack)

  3. [Security](#security)

+ [Code Source](#code-source)
  1. [Usage & Installation](#usage--installation)
  2. [Documentation](#documentation)

---
# Abstract

Rate limit is a scalable application that is used worldwide by customers to limit sending notifications by email or SMS.

# Strategy

## Analysis

For a better strategy we need to analyze carefully with the client (Company XYZ) and carefully organize their requirements as Functional and Non-Functional requirements.

### Functional requirements

This is a notification system which can send notifications either by SMS or by Email and has a certain rate limit.
It should also be able to:

- Limit too many requests within the same time window (month, week, year, …) from a client
- Limit amount of requests per time window across the whole system
- Create notification
- Read notifications
- Send notification by SMS
- Send notification by Email

### Non-Functional requirements

The system should also be able to:

- Be distributed across a cluster of servers
- Handle hard/soft throttling
- Highly available (As the company XYZ is very successfull)
- Eventually consistent (across the cluster which might even be worldwide)

### Capacity estimations

This could to have a clear picture of what resources we need to allocate and how to handle all different use-cases of the system. Considering 1M DAU (Daily active users) per each client and supposing each client will have 10 Daily notifications... 1M * 10 Nots = 10M Nots/Day.
It is difficult to provide a precise estimate of the CPU, memory, and network bandwidth required for the rate limiter platform without more information about the specific requirements and constraints of the system. Here is an example of how you might estimate the capacity of the system based on the assumption that the average client performs 10 notifications per day.

**1. Traffic patterns:**
Assuming that there are 1 million customers and each customer performs 10 notifications per day, the total number of notifications per day is 10 million.
If we assume that the traffic is evenly distributed throughout the day, the average number of notifications per second is approximately 11.5 notifications.

**2. Response time requirements:**
Let's assume that the target response time for each notification is 50 milliseconds. Normal Redis operations take less than 1 millisecond (typically 0.2 to 0.5 milliseconds, or 200 to 500 microseconds) per operation.
This means that the system must be able to handle at least 25 notifications per second per CPU core.

**3. Resource utilization:**
Based on these estimates, we can calculate the required CPU, memory, and network bandwidth for the system as follows:

    - CPU: 25 notifications / 11.5 notifications per second = 2 cores scalable
    - Memory: The required memory will depend on the specific requirements of the system and the resources required by the chosen technology stack.
    - Network bandwidth: The required network bandwidth will depend on the size of the notifications and the expected traffic patterns.

**4. Scalability:**
To ensure that the system can handle increases in traffic and resource utilization, it is important to design the system to be horizontally scalable.
You can use a container orchestration platform, such as Kubernetes, to automate the deployment and management of the containers and scale the system up or down as needed.

## System Design

### Summary

Continuing on the requirements listed above, We are going to propose the best way to store data and the best way to structure the system.

#### HLD - High-Level Design

Let's build a microservices distributed system which will handle both non-functional and functional requirements.

This is with three services (see the architecture diagram above — **Client → API Gateway → { SMS Service, Email Service }**):

- **API Gateway (Limiter service):** The single entry point for clients. It performs **distributed, Redis-backed rate limiting** and routes each request to the correct downstream service. Sending is capped system-wide (≈10 requests / 3 seconds); a per-client resolver keyed on the `X-Client-Id` header is also provided.
- **SMS Service:** Sends notifications by SMS and persists every attempt. Ships with a **mock provider** (logs the message, no account needed) that is swappable for **Twilio** via configuration.
- **Email Service:** Sends notifications by Email and persists every attempt. Delivers to a local **Mailpit** inbox out of the box; swappable for any real SMTP server via configuration.

Communication between the gateway and the services is **synchronous HTTP**. The README's message-broker (Kafka) design remains a valid future evolution for fully asynchronous, fire-and-forget delivery, but is not required for the current flow.

There are several strategies you can use to make the rate limiter platform scalable for 1 million customers such as: Use a distributed database, Use a load balancer, Use a cache, Use asynchronous communication and Use horizontal scaling


**1. Use a distributed database:**

  - Use a database technology that supports horizontal scaling, such as a NoSQL database or a distributed SQL database.
  - Design the database schema to allow for efficient sharding and partitioning of data.

**2. Use a load balancer:**

  - Use a load balancer to distribute incoming requests across multiple instances of the API gateway and back-end services.
  - Use a scalable load balancer, such as a cloud-based service, to handle high traffic.

**3. Use a cache:**

  - Use a cache, such as Redis, to store frequently accessed data and reduce the load on the database.
  - Use cache invalidation strategies, such as time-based expiration or cache tagging, to ensure the cache stays up to date.

**4. Use asynchronous communication:**

  - Use asynchronous communication, such as message queues, to decouple the microservices and allow them to scale independently.
  - Use a message broker, such as RabbitMQ or Kafka, to handle the communication between the microservices.

**5. Use horizontal scaling:**

  - Use horizontal scaling to add more instances of the API gateway and back-end services as needed to handle the load.
  - Use a container orchestration platform, such as Kubernetes, to automate the deployment and management of the containers

<img width="1588" height="669" alt="Image" src="https://github.com/user-attachments/assets/7067e3f5-ed1b-4d26-869b-ed1368e3733e" />

The solution provided doesn't tackle all the points but it is a base for the remaining elements not present on the diagram.

#### Tech Stack

- **Spring Cloud Gateway** : Reactive API gateway (routing + rate limiting)
- **PostgreSQL** : Primary data store for notification history
- **Redis** : Backing store for the distributed rate limiter
- **Mailpit** : Local SMTP server + web inbox for verifying emails
- **Twilio** *(optional)* : Real SMS delivery provider
- **Spring Boot** : All three microservices (Java 17)
- **Docker / Docker Compose** : Containerization & local orchestration
- **Kafka** *(future)* : Message broker for asynchronous delivery

## Security

Here are a few strategies we can use to make the Rate Limiter platform more secure:

**1. Use secure communication channels:**

  - Use secure protocols, such as HTTPS, for all communication between the client, the API gateway, and the back-end services.
  - Use secure message queues, such as those that support Transport Layer Security (TLS), for asynchronous communication between the microservices.

**2. Implement authentication and authorization:**

  - Use JSON Web Tokens (JWTs) or similar technologies to authenticate users and authorize their access to specific resources and actions.
  - Use a secure mechanism, such as hashing and salting, to store passwords in the database.

**3. Use encryption:**

  - Use encryption to secure sensitive data, such as financial notifications and personal information, both in transit and at rest.
  - Use strong encryption algorithms, such as AES, and regularly rotate the encryption keys.

**4. Perform regular security assessments:**

  - Regularly perform security assessments, such as penetration testing and vulnerability scanning, to identify and address potential security risks.
  - Implement a process for responding to and mitigating any identified vulnerabilities.

**5. Follow best practices:**

  - Follow best practices for secure coding and architecture, such as those outlined by the OWASP Top Ten and the SANS Top 25.
  - Use secure frameworks and libraries, and keep them up to date with the latest security patches.



# Code Source

## Usage & Installation

### 1. Clone this repository using git and open the project directory in terminal

```
git clone https://github.com/RedJanvier/api-rate-limiter.git && cd api-rate-limiter
```

### 2. Run the project

**Note:** Before starting up the application make sure the following ports are not in use to avoid any conflict.

> Ports list:
> - API Gateway (8081)
> - SMS Service (8082)
> - Email Service (8083)
> - Postgres (5432)
> - Redis (6379)
> - Mailpit SMTP (1025) & Web UI (8025)

Run the command below to build and spin up all three microservices plus Postgres, Redis, and Mailpit:

```bash
docker compose up --build
```

The default configuration needs **no external credentials**: SMS uses a mock provider (messages appear in the `sms-service` logs) and Email is delivered to Mailpit, viewable at **http://localhost:8025**.

## Documentation
> Base path for all endpoints (through the gateway) is `http://localhost:8081/api/v1`

| Method | Endpoint                       | Enable a user to:                          | Docs |
| ------ | ------------------------------ | ------------------------------------------ | ---- |
| POST   | /notifications/sms             | Send a notification by SMS                 | [Send SMS](#send-sms) |
| GET    | /notifications/sms             | List all SMS notifications (history)       | [Send SMS](#send-sms) |
| GET    | /notifications/sms/{id}        | Read a single SMS notification             | [Send SMS](#send-sms) |
| POST   | /notifications/email           | Send a notification by Email               | [Send Email](#send-email) |
| GET    | /notifications/email           | List all Email notifications (history)     | [Send Email](#send-email) |
| GET    | /notifications/email/{id}      | Read a single Email notification           | [Send Email](#send-email) |

All requests flow through the gateway, which applies the rate limit before proxying to the SMS or Email service. Exceeding the limit returns **HTTP 429 Too Many Requests**.

### Send SMS

`POST /api/v1/notifications/sms`

**Request body:**
```json
{
  "to": "+250788000000",
  "message": "Hello from the rate limiter"
}
```

**Response body** (`201 Created`): the persisted notification record.
```json
{
  "id": 1,
  "recipient": "+250788000000",
  "message": "Hello from the rate limiter",
  "status": "SENT",
  "provider": "mock",
  "error": null,
  "createdAt": "2026-08-09T10:15:30Z"
}
```

Example:
```bash
curl -X POST http://localhost:8081/api/v1/notifications/sms \
  -H 'Content-Type: application/json' \
  -d '{"to":"+250788000000","message":"Hello"}'
```

### Send Email

`POST /api/v1/notifications/email`

**Request body:**
```json
{
  "to": "user@example.com",
  "subject": "Welcome",
  "body": "Hello from the rate limiter"
}
```

**Response body** (`201 Created`): the persisted notification record (with `status`, `error`, `createdAt`). Open **http://localhost:8025** to see the delivered email in Mailpit.

Example:
```bash
curl -X POST http://localhost:8081/api/v1/notifications/email \
  -H 'Content-Type: application/json' \
  -d '{"to":"user@example.com","subject":"Welcome","body":"Hello"}'
```

### Switching SMS to Twilio (optional)

Set the following in `.env` (or as environment variables) and restart:

```bash
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_FROM_NUMBER=+1XXXXXXXXXX
```

## Author

- **Janvier Ntwali** (https://github.com/RedJanvier)

## Licence

This software is published by `Janvier Ntwali` under the [MIT licence](http://opensource.org/licenses/MIT).

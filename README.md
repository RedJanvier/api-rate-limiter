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
If we assume that the traffic is evenly distributed throughout the day, the average number of notifications per second is approximately 115.

**2. Response time requirements:**
Let's assume that the target response time for each notification is 500 milliseconds.
This means that the system must be able to handle at least 2 notifications per second per CPU core.

**3. Resource utilization:**
Based on these estimates, we can calculate the required CPU, memory, and network bandwidth for the system as follows:

    - CPU: 2 cores per notification * 115 notifications per second = 230 cores
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

This is with two services:

- **API Gateway (Limiter service):** This will be handling the authentication, rate limiting, circuit breaking, retries, etc… of the clients and will be the path to all other services.
- **Notification Service:** This is a service which will handle different methods of operations and each of their specific errors to send a notification by SMS or by Email.

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

<img width="766" alt="Screenshot 2023-03-31 at 07 25 45" src="https://user-images.githubusercontent.com/39817762/229034091-3a08b872-14b0-412f-833b-1caf88ec152d.png">

The solution provided doesn't tackle all the points but it is a base for the remaining elements not present on the diagram.

#### Tech Stack

- **PostgreSQL** : Prirmary data store and indexing data store
- **RedisDB** : Cached data store
- **Kafka** : Message queue/Brokker
- **Spring Boot** : For both Microservices
- **Docker** : Containerization

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
git clone https://github.com/RedJanvier/api-rate-limiter.git && cd the Rate Limiter
```

### 2. Run the project

**Note:** Before starting up the application make sure the following ports are not in use to avoid any conflict.

> Ports list:
> - Redis (6379)
> - Kafka (29092)
> - Postgres (5432)
> - API Gateway (8081)
> - Notification Service (8080)

Run the command `docker compose up` to spin up the Rate Limiter api-gateway and notification-microservice.

## Documentation
> Base path for all endpoints is `http://localhost:8080/api/v1`

| Method | Endpoint              | Enable a user to:                     |Docs|
| ------ | --------------------- | ------------------------------------- |------|
| GET    | /                     | Send Notification by SMS              |[Send Notification](#send-notification)|

### Send Notification

`GET /`: Sends notification by SMS.

**Request body:**
  NOT APPLICABLE
  
**Response body:** message that notification was sent.

## Author

- **Janvier Ntwali** (https://github.com/RedJanvier)

## Licence

This software is published by `Janvier Ntwali` under the [MIT licence](http://opensource.org/licenses/MIT).

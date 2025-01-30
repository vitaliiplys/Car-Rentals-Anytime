![Car Rentals Anytime.png](Images/Car%20Rentals%20Anytime.png)

# **Car Rentals Anytime**

Car Rentals Anytime this is an application for car rental, in which you can choose a car by model and brand, it will help you quickly find the right free car and book it in a way convenient for you in the shortest possible time. This application will greatly simplify the work of administrators and speed up the processing of orders for cars and will be more scalable and easy to use for customers.

#  ⚙️ Technologies Used
* Java 17+
* Maven
* Spring Boot
* Spring Data JPA
* Spring Boot Security
* JSON Web Token
* Lombok
* MapStruct
* Liquibase
* Postgres
* Hibernate
* JUnit5
* Testcontainers
* Docker
* Swagger
* Stripe
* Telegram

## 🔗 Endpoints
### 🔑 AuthenticationController: Handles registration and login requests, supporting both Basic and JWT authentication.
- `POST: /auth/registration` - The endpoint for registration.
- `POST: /auth/login` - The endpoint for login.

### 🚗 CarsController: Handles requests for car CRUD operations.
- `GET: /cars` - The endpoint for retrieving all avaliable cars.
- `GET: /cars/{id}` - The endpoint for searching a specific car by ID.
- `POST: /cars` - The endpoint for creating a new  car. (Available Administrator Only)
- `PUT: /cars/{id}` - The endpoint for updating car information. (Available Administrator Only)
- `DELETE: /cars/{id}` - The endpoint for deleting car. (Available Administrator Only)

### 👦👧 UsersController: Handles requests for user operations.
- `GET: /users/me` - The endpoint for retrieving user`s information.
- `PATCH: /users/me` - The endpoint for updating user`s information.
- `PUT: /users/{id}/role?role=` - The endpoint for updating user's role. (Available Administrator Only)

### 🛒 RentalsController: Handles requests for rental operations.
- `GET: /rentals/my` - The endpoint for retrieving all rentals by its owner.
- `GET: /rentals?userId=&is_active=` - The endpoint for retrieving all rentals by user and activity. (Available Administrator Only)
- `GET: /rentals/{id}` - The endpoint for retrieving a specific rental by ID. (Available Administrator Only)
- `POST: /rentals` - The endpoint for creating a new rental, it will send notification on creation.
- `POST: /rentals/{id}/return` - The endpoint for returning rental.

###  💸 PaymentsController: Handles requests for payment operations.
- `GET: /payments?user_id=` - The endpoint for retrieving all payments by user ID. (Available Administrator Only)
- `GET: /payments/success` - Success endpoint for payment, it will send notification on success.
- `GET: /payments/cancel` - Cancel endpoint for payment.
- `POST: /payments` - The endpoint for creating payment session using Stripe API, it will send notification on creation.

## 🔗 Structure Project
![Project Structure.png](Images/Project%20structure.png)

## 🔗 Roles
Only 2 user roles are available in this application: 'USER role' and 'MANAGER role'.
The user is given the opportunity to perform the following actions: search for a car, display a car by ID, rent a car, etc.
However, a user with the 'USER role' cannot delete cars from the database or update them. (Only a user with the 'MANAGER role' can perform these manipulations)

## 🕹️ How to run the project locally:
* Ensure you have Docker installed on your system.
* Configure your database settings in the .env file.
* Open a terminal and navigate to the root directory of your project.
* Run the application using Docker Compose: docker-compose up
* Explore the endpoints using tools like Postman or Swagger

## Overview
Lootsy is a full-featured web marketplace for gamer-related digital and physical goods.
It supports user accounts with JWT authentication, product catalog, shopping basket, 
secure payments, wallet, favorites, ratings, and seller/admin workflows.  

## Tech Stack
- Frontend: JavaScript
- Backend: Spring Boot (Java)
- Database: PostgreSQL
- Auth: JWT (access + refresh tokens), optional OAuth (Discord/Steam in future)
- Payments: Stripe (test mode)

## Features
- User registration, login, logout (JWT)
- Product catalog with search and filters
- Basket and checkout
- Secure payments
- Order history
- Seller dashboard (manage products, sales)
- Admin dashboard (approve sellers, moderate products)

## Run Instructions
1. Clone the repo:
   ```bash
   git@github.com:ghhamo/lootsy.git
   cd lootsy


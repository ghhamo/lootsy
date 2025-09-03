## 1. Specialized Online Store for Gamer Goods

## What kind of app would it be?

A full-featured web application that acts as a marketplace for gamer-related digital and physical goods. It will include
user accounts, JWT authentication, secure payment methods, shopping basket, wallet balance, favorite list, product
ratings, and sales tracking.

## Who is this project for?

Gamers who want to buy digital goods (game keys, DLC, skins, mods) or physical gear (mice, keyboards, accessories).
Sellers/publishers who want to list their products for sale.
Community members who want to share reviews and ratings of products.

## What needs will it satisfy?

Safe, organized way to buy gamer goods.
Easy login/registration and profile management.
Secure payments and wallet balance handling.
Basket/checkout flow for purchasing multiple items at once.
Ability for users to save favorite goods.
Trust and decision support via ratings and reviews.
Tracking of orders and sales for sellers.

## Functional Requirements:

User registration, login, logout (with JWT, email & password, optional OAuth like Discord/Steam).
User roles: customer, seller, admin.
Product catalog (search, filter, sort).
Product detail pages with description, images, price, seller info.
Basket/cart with add/remove/update items.
Checkout process with payment method (Stripe or PayPal test integration).
Wallet system for users (deposit balance, spend balance, track history).
Favorites list for each user.
Ratings and reviews for products.
Sales tracking for sellers (number of units sold, revenue).
Admin dashboard (approve sellers, manage products, handle disputes).

## Non-Functional Requirements:

Security: password hashing, HTTPS, secure payment APIs, JWT tokens with refresh.
Performance: fast product search and page load.
Reliability: basic monitoring/logging, error handling.
Usability: simple UI with responsive design.

## Extra Features (future/optional):

Discount coupons and promotions.
Recommendation system (“users who bought this also bought…”).
Order notifications via email.
Multilingual support.

## Instructor Feedback & Applied Changes

Feedback: Payment integration should be simple at first.  
Applied Change: Use Stripe Checkout (test mode) only for MVP; PayPal integration postponed.

Feedback: Wallet system and advanced features can wait.  
Applied Change: Moved Wallet, Discount Coupons, Recommendation Engine, and Multilingual Support to
Phase 2.

Feedback: Improve usability for finding products.  
Applied Change: Added search, filters (category, price), and sorting to the MVP catalog.

Feedback: Add moderation for community-generated content.  
Applied Change: Added Admin moderation for reviews and product approval.

## 2. Document Management System for Game Mods, Patches and Rules

## What kind of app would it be?

A web-based document management system (DMS) specialized for managing game-related documents such as mods, patch notes,
guides, and tournament rules. It allows uploading, organizing, versioning, and controlled access to documents.

## Who is this project for?

Gamers who use and share mods.
Mod developers who want to publish and maintain versions.
Esports and tournament organizers who need to distribute official rules.

## What needs will it satisfy?

Centralized storage for game mods and patches.
Easy version control to track changes in mods and rules.
Categorization and search functions for quick retrieval.
Permissions and access control (e.g., only organizers can upload rules, but players can read them).
Reliable reference point for compatible versions (mods vs. game patches).

## Functional Requirements:

User registration, login, logout (with JWT).
Document upload with metadata (title, description, version, author, category).
Version control for documents (each update creates a new version).
Categorization and tags for filtering and search.
Download access for authorized users.
Admin role for approving documents and managing categories.

## Non-Functional Requirements:

Security: JWT-based authentication, file integrity checks.
Performance: optimized document search and download speed.
Reliability: backup system for important files.
Usability: clean, intuitive UI for uploading and browsing.

## Extra Features (future/optional):

Collaborative editing of documents.
Commenting system for users to discuss mods/rules.
Notifications when a new patch or rule is published.
Integration with GitHub/Git for mod developers.
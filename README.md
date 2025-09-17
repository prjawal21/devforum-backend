# DevForum Backend 🚀

A comprehensive **Reddit-style developer forum backend** built with **Spring Boot 3**, **MongoDB**, and **JWT authentication**. This production-ready API provides all the features needed for a modern developer community platform.

## 🌟 Features

### 🔐 Authentication & Authorization
- **JWT-based authentication** with refresh tokens
- **Role-based access control** (USER, MODERATOR, ADMIN)
- **Secure password hashing** with BCrypt
- **User profile management** with bio, avatar, and reputation

### 📝 Posts & Content
- **Full CRUD operations** for posts
- **Markdown content support** ready for frontend integration
- **Tagging system** with multiple tags per post
- **Vote system** (upvotes/downvotes) with reputation tracking
- **View counting** and engagement metrics
- **Content moderation** (pin, lock, delete)

### 💬 Comments System
- **Hierarchical nested comments** (up to 10 levels deep)
- **Comment voting** with reputation effects
- **Reply system** with parent-child relationships
- **Comment moderation** capabilities
- **Efficient tree structure** building

### 🔍 Search & Discovery
- **Full-text search** across posts (title, body, tags)
- **Tag-based filtering** and post discovery
- **User search** with reputation ranking
- **Global search** with suggestions
- **Advanced filtering** (hot, trending, top, recent)

### 🛡️ Moderation & Admin
- **Multi-level role system** (USER → MODERATOR → ADMIN)
- **Admin dashboard** with system statistics
- **Content moderation** tools
- **User management** (role changes, user statistics)
- **Flagged content** management

### ⚡ Performance & Optimization
- **MongoDB indexing** for all major queries
- **Efficient pagination** for all list endpoints
- **Optimized database queries**
- **Caching-ready architecture**
- **Performance monitoring** ready

## 🏗️ Tech Stack

- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Database**: MongoDB
- **Security**: Spring Security + JWT
- **Build Tool**: Maven
- **Documentation**: OpenAPI/Swagger ready

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- MongoDB 4.4+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/prjawal21/devforum-backend.git
   cd devforum-backend
   ```

2. **Configure MongoDB**
   ```bash
   # Make sure MongoDB is running on localhost:27017
   # Or update application.properties with your MongoDB URI
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API**
   ```
   Base URL: http://localhost:8080
   ```

## 📋 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### Users
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{username}/profile` - Get user by username
- `GET /api/users/search` - Search users
- `GET /api/users/top` - Get top users by reputation

### Posts
- `GET /api/posts` - List posts (supports sorting: recent, hot, top, trending)
- `POST /api/posts` - Create new post
- `GET /api/posts/{id}` - Get specific post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `GET /api/posts/search` - Search posts
- `GET /api/posts/tag/{tag}` - Get posts by tag
- `GET /api/posts/author/{username}` - Get posts by author

### Comments
- `POST /api/comments` - Create comment/reply
- `GET /api/comments/post/{postId}` - Get comments for post (nested tree)
- `GET /api/comments/{id}` - Get specific comment
- `PUT /api/comments/{id}` - Update comment
- `DELETE /api/comments/{id}` - Delete comment

### Voting
- `POST /api/votes` - Vote on post or comment
- `GET /api/votes/{targetType}/{targetId}` - Get user's vote status

### Search
- `GET /api/search` - Global search (posts + users)
- `GET /api/search/posts` - Search posts only
- `GET /api/search/users` - Search users only
- `GET /api/search/suggestions` - Get search suggestions

### Admin/Moderation
- `GET /api/admin/dashboard` - Admin dashboard
- `GET /api/admin/users` - Manage users
- `PUT /api/admin/users/{userId}/role` - Change user role
- `POST /api/admin/posts/{postId}/moderate` - Moderate posts

## 🧪 Testing

### Sample API Calls

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com", 
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

**Create a post (requires authentication):**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "My First Post",
    "body": "This is the content of my post with **markdown** support!",
    "tags": ["java", "spring-boot", "mongodb"]
  }'
```

## 🔒 Security Features

- **JWT Authentication** with secure token generation
- **Role-based Authorization** with method-level security
- **Password Encryption** using BCrypt
- **CORS Configuration** for frontend integration
- **Input Validation** at all API endpoints
- **SQL/NoSQL Injection Protection**

## 👨‍💻 Author

**Prajwal** - [GitHub Profile](https://github.com/prjawal21)

---

**⭐ If you find this project helpful, please give it a star on GitHub!**

Ready to power your developer community! 🎉

# Git Setup Commands for DevForum Backend

After installing Git, run these commands in the project directory:

```bash
# Initialize Git repository
git init

# Add all files
git add .

# Create first commit
git commit -m "Initial commit: Complete DevForum Backend with Spring Boot 3, MongoDB, and JWT authentication

Features implemented:
- JWT-based authentication with refresh tokens
- Role-based access control (USER, MODERATOR, ADMIN)
- Complete CRUD operations for posts and comments
- Nested comments system with voting
- Full-text search and filtering
- User reputation system
- MongoDB optimization with indexing
- Admin dashboard and moderation tools
- 40+ REST API endpoints
- Production-ready architecture"

# Add GitHub remote (replace with your repository URL)
git remote add origin https://github.com/prjawal21/devforum-backend.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Alternative: Create Repository on GitHub First

1. Go to https://github.com/prjawal21
2. Click "New" repository
3. Name it "devforum-backend"
4. Make it public
5. Don't initialize with README (we already have one)
6. Copy the repository URL
7. Use the commands above with your repository URL

## Project Structure Ready for GitHub

✅ README.md - Comprehensive documentation
✅ .gitignore - Proper file exclusions
✅ LICENSE - MIT License
✅ pom.xml - Maven configuration
✅ Complete source code - 40+ Java files
✅ Application properties - Configuration
✅ All documentation - API endpoints listed

## What Gets Pushed to GitHub

📁 Project Structure:
```
devforum-backend/
├── 📄 README.md (comprehensive docs)
├── 📄 LICENSE (MIT)
├── 📄 .gitignore (proper exclusions)
├── 📄 pom.xml (Maven config)
├── 📁 src/
│   ├── 📁 main/java/com/devforum/backend/
│   │   ├── 📁 config/        # Security & MongoDB config
│   │   ├── 📁 controller/    # REST API controllers
│   │   ├── 📁 dto/          # Data transfer objects
│   │   ├── 📁 entity/       # Database entities
│   │   ├── 📁 repository/   # Data repositories
│   │   ├── 📁 security/     # JWT & auth components
│   │   ├── 📁 service/      # Business logic
│   │   └── 📄 DevforumBackendApplication.java
│   └── 📁 main/resources/
│       └── 📄 application.properties
└── 📁 .mvn/ (Maven wrapper)
```

## After Pushing

Your repository will showcase:
- ⭐ Professional README with badges and documentation
- 🔧 Complete Spring Boot 3 application
- 🚀 Production-ready code architecture
- 📚 Comprehensive API documentation
- 🛡️ Security best practices
- 📊 Database optimization
- 🎯 Clean code structure

This will be an impressive portfolio project demonstrating your backend development skills!
#!/bin/bash

# Time Tracker Quick Setup Script
# This script helps set up the Docker environment for the Time Tracker application

set -e

echo "🚀 Time Tracker Docker Setup (Java 21)"
echo "=============================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Check Docker daemon is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker daemon is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker daemon is running"

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cat > .env << EOF
# Time Tracker Environment Configuration
DB_PASSWORD=password
SPRING_PROFILE=dev
NODE_ENV=development
APP_MODE=development
API_BASE_URL=http://localhost:8080/api
CORS_ORIGINS=http://localhost:3000,http://frontend:3000
LOG_LEVEL=INFO
SHOW_SQL=false
DEBUG=false
EOF
    echo "✅ Created .env file with default values"
else
    echo "✅ .env file already exists"
fi

# Create directories if they don't exist
echo "📁 Creating necessary directories..."
mkdir -p backups
mkdir -p backend
mkdir -p frontend

echo "✅ Directories created"

# Function to test service
test_service() {
    local service=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "🧪 Testing $service..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            echo "✅ $service is responding"
            return 0
        fi
        
        echo "⏳ Waiting for $service (attempt $attempt/$max_attempts)..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service failed to respond after $max_attempts attempts"
    return 1
}

# Start database first
echo ""
echo "🗄️ Starting database..."
docker-compose up -d database

echo "⏳ Waiting for database to be ready..."
while ! docker-compose exec -T database pg_isready -U timetracker -d timetracker > /dev/null 2>&1; do
    echo "⏳ Database not ready yet, waiting..."
    sleep 2
done

echo "✅ Database is ready"

# Start backend
echo ""
echo "🔧 Starting backend..."
docker-compose up -d backend

# Test backend
if test_service "Backend API" "http://localhost:8080/api/health"; then
    echo "✅ Backend is running successfully"
else
    echo "❌ Backend failed to start properly"
    echo "📋 Backend logs:"
    docker-compose logs --tail=20 backend
    exit 1
fi

# Start frontend
echo ""
echo "🎨 Starting frontend..."
docker-compose up -d frontend

# Test frontend
if test_service "Frontend" "http://localhost:3000"; then
    echo "✅ Frontend is running successfully"
else
    echo "❌ Frontend failed to start properly"
    echo "📋 Frontend logs:"
    docker-compose logs --tail=20 frontend
    exit 1
fi

# Start adminer for development
echo ""
echo "🔧 Starting Adminer (database admin)..."
docker-compose --profile dev up -d adminer

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "📋 Service URLs:"
echo "   Frontend:  http://localhost:3000"
echo "   Backend:   http://localhost:8080"
echo "   API Docs:  http://localhost:8080/swagger-ui.html"
echo "   Adminer:   http://localhost:8081"
echo ""
echo "🧪 Quick API Test:"
echo "   curl http://localhost:8080/api/health"
echo ""
echo "📊 Check status:"
echo "   docker-compose ps"
echo ""
echo "📄 View logs:"
echo "   docker-compose logs [service_name]"
echo ""
echo "🛑 Stop everything:"
echo "   docker-compose down"
echo ""
echo "Happy coding! 🚀"
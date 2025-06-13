# Project Cronos API Documentation

## Overview

The Project Cronos API provides a comprehensive REST API for managing time tracking data. This documentation covers all available endpoints, request/response formats, and usage examples.

**Base URL:** `http://localhost:8080/api`  
**API Version:** 1.0  
**Authentication:** Not required for local deployment

## 📋 Quick Reference

| Resource     | Endpoint                        | Description            |
| ------------ | ------------------------------- | ---------------------- |
| Health       | `GET /health`                   | API health check       |
| Categories   | `GET /categories/active`        | List active categories |
| Tasks        | `GET /tasks/active`             | List active tasks      |
| Time Entries | `GET /time-entries/date/{date}` | Get entries for date   |
| Reports      | `GET /reports/daily/{date}`     | Daily summary report   |
| Export       | `GET /export/time-entries/csv`  | Export as CSV          |

## 🏥 Health Check

### GET /api/health

Check API health status.

**Response:**

```json
{
  "status": "UP",
  "timestamp": "2024-06-07T10:30:00Z",
  "version": "1.0.0"
}
```

## 📂 Categories

### GET /api/categories/active

Get all active categories for the current user.

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Work",
      "description": "Professional work tasks",
      "isDefault": true,
      "isArchived": false,
      "sortOrder": 0,
      "taskCount": 3,
      "createdAt": "2024-06-07T09:00:00Z",
      "updatedAt": "2024-06-07T09:00:00Z"
    }
  ]
}
```

### POST /api/categories

Create a new category.

**Request Body:**

```json
{
  "title": "Personal",
  "description": "Personal activities and tasks",
  "isDefault": false,
  "sortOrder": 1
}
```

**Validation Rules:**

- `title`: Required, 1-255 characters, unique per user
- `description`: Optional, max 1000 characters
- `isDefault`: Optional, only one default per user
- `sortOrder`: Optional, integer

**Response:** Same as GET with created category data.

### PUT /api/categories/{id}

Update an existing category.

**Request Body:** Same as POST (all fields optional for update)

### DELETE /api/categories/{id}

Delete a category. Archives if it has associated time entries.

**Response:**

```json
{
  "success": true,
  "message": "Category archived successfully (has associated time entries)",
  "wasArchived": true
}
```

## 🎯 Tasks

### GET /api/tasks/active

Get all active tasks across all categories.

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryTitle": "Work",
      "title": "Development",
      "description": "Software development tasks",
      "color": "#3498db",
      "icon": "code",
      "isArchived": false,
      "sortOrder": 0,
      "createdAt": "2024-06-07T09:00:00Z",
      "updatedAt": "2024-06-07T09:00:00Z"
    }
  ]
}
```

### GET /api/tasks/category/{categoryId}/active

Get active tasks for a specific category.

### POST /api/tasks

Create a new task.

**Request Body:**

```json
{
  "categoryId": 1,
  "title": "Code Review",
  "description": "Review pull requests and code quality",
  "color": "#e74c3c",
  "icon": "eye",
  "sortOrder": 1
}
```

**Validation Rules:**

- `categoryId`: Required, must exist and belong to user
- `title`: Required, 1-255 characters, unique per category
- `description`: Optional, max 1000 characters
- `color`: Required, hex format (#RRGGBB), unique per category
- `icon`: Optional, max 50 characters
- `sortOrder`: Optional, integer

### PUT /api/tasks/{id}

Update an existing task.

### DELETE /api/tasks/{id}

Delete a task. Archives if it has associated time entries.

## ⏱️ Time Entries

### GET /api/time-entries/date/{date}

Get all time entries for a specific date.

**URL Parameters:**

- `date`: YYYY-MM-DD format

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "taskId": 1,
      "taskTitle": "Development",
      "taskColor": "#3498db",
      "taskIcon": "code",
      "categoryId": 1,
      "categoryTitle": "Work",
      "entryDate": "2024-06-07",
      "startTime": "09:00",
      "endTime": "10:30",
      "durationMinutes": 90,
      "description": "Working on API documentation",
      "isBillable": true,
      "createdAt": "2024-06-07T09:00:00Z",
      "updatedAt": "2024-06-07T09:00:00Z"
    }
  ]
}
```

### POST /api/time-entries

Create a new time entry.

**Request Body:**

```json
{
  "taskId": 1,
  "entryDate": "2024-06-07",
  "startTime": "09:00",
  "endTime": "10:30",
  "description": "API development work",
  "isBillable": true
}
```

**Business Rules:**

- Minimum duration: 15 minutes
- Maximum daily total: 24 hours
- No overlapping entries allowed
- Start time must be before end time

### POST /api/time-entries/bulk

Create multiple time entries for a date.

**Request Body:**

```json
{
  "entryDate": "2024-06-07",
  "replaceExisting": false,
  "timeEntries": [
    {
      "taskId": 1,
      "startTime": "09:00",
      "endTime": "10:30",
      "description": "Morning development"
    },
    {
      "taskId": 2,
      "startTime": "11:00",
      "endTime": "12:00",
      "description": "Testing"
    }
  ]
}
```

**Parameters:**

- `replaceExisting`: If true, replaces all existing entries for the date
- `timeEntries`: Array of time entry objects

### POST /api/time-entries/validate

Validate a time entry without creating it.

**Request Body:** Same as POST /api/time-entries

**Response:**

```json
{
  "success": true,
  "valid": true,
  "warnings": ["Long duration detected: 8 hours 30 minutes"],
  "errors": []
}
```

## 📊 Reports

### GET /api/reports/daily/{date}

Get comprehensive daily report.

**Response:**

```json
{
  "success": true,
  "data": {
    "date": "2024-06-07",
    "totalMinutes": 450,
    "totalTimeFormatted": "7h 30m",
    "totalEntries": 5,
    "categoryBreakdowns": [
      {
        "categoryId": 1,
        "categoryTitle": "Work",
        "totalMinutes": 420,
        "timeFormatted": "7h 0m",
        "entryCount": 4
      }
    ],
    "taskBreakdowns": [
      {
        "taskId": 1,
        "taskTitle": "Development",
        "taskColor": "#3498db",
        "categoryTitle": "Work",
        "totalMinutes": 240,
        "timeFormatted": "4h 0m",
        "entryCount": 2
      }
    ],
    "timeEntries": [...],
    "warnings": []
  }
}
```

### GET /api/reports/weekly/{date}

Get weekly report (Monday to Sunday containing the date).

## 📤 Export

### GET /api/export/time-entries/csv

Export time entries as CSV.

**Query Parameters:**

- `startDate`: YYYY-MM-DD (required)
- `endDate`: YYYY-MM-DD (required)

**Response:** CSV file download with headers:

```csv
Date,Start Time,End Time,Duration (minutes),Duration (formatted),Task,Category,Description,Billable
2024-06-07,09:00,10:30,90,1h 30m,Development,Work,API development,true
```

## 🔧 Error Handling

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "startTime",
        "message": "Start time must be before end time"
      }
    ],
    "timestamp": "2024-06-07T10:30:00Z"
  }
}
```

### Common Error Codes

| Code                      | Description                  | HTTP Status |
| ------------------------- | ---------------------------- | ----------- |
| `VALIDATION_ERROR`        | Request validation failed    | 400         |
| `RESOURCE_NOT_FOUND`      | Requested resource not found | 404         |
| `BUSINESS_RULE_VIOLATION` | Business rule violation      | 422         |
| `TIME_OVERLAP_ERROR`      | Time entry overlaps detected | 409         |
| `DAILY_LIMIT_EXCEEDED`    | Daily 24-hour limit exceeded | 422         |

## 🧪 Testing the API

### Using curl

**Get categories:**

```bash
curl -X GET "http://localhost:8080/api/categories/active" \
  -H "Content-Type: application/json"
```

**Create time entry:**

```bash
curl -X POST "http://localhost:8080/api/time-entries" \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": 1,
    "entryDate": "2024-06-07",
    "startTime": "09:00",
    "endTime": "10:30",
    "description": "API testing"
  }'
```

**Export CSV:**

```bash
curl -X GET "http://localhost:8080/api/export/time-entries/csv?startDate=2024-06-01&endDate=2024-06-07" \
  -H "Accept: text/csv" \
  -o timesheet.csv
```

### Using Swagger UI

Access the interactive API documentation at:
`http://localhost:8080/swagger-ui.html`

## 📋 Rate Limiting

Currently no rate limiting is implemented for local deployment. Future hosted versions will include rate limiting for API endpoints.

## 🔐 Authentication

Local deployment uses a default admin user with no authentication required. Future versions will support:

- JWT token authentication
- Google OAuth integration
- Multi-user support with proper authorization

## 📝 API Versioning

The API uses URL versioning:

- Current version: `/api/v1/` (defaults to `/api/`)
- Future versions: `/api/v2/`, etc.

Breaking changes will be introduced in new API versions while maintaining backward compatibility.

---

**Need help?** Check our [Contributing Guide](../CONTRIBUTING.md) or open an issue on GitHub.

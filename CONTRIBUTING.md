# Contributing to Project Cronos

Thank you for your interest in contributing to Project Cronos! This guide will help you get started with contributing to project cronos.

> [!NOTE]
> This is just a generic outline. Due to the scope of the project, there's no need to follow strictly to this, but it serves as a good guideline 🙂

## 🎯 How to Contribute

There are many ways to contribute:

- 🐛 **Report bugs** or suggest improvements
- 💡 **Request features** or enhancements
- 📝 **Improve documentation**
- 🛠️ **Submit code** changes (bug fixes, features, tests)
- 🎨 **Design improvements** (UI/UX suggestions)
- 🌍 **Translations** (future feature)

## 🚀 Getting Started

### Prerequisites

- **Node.js** 20+ (for frontend)
- **Java** 21+ (for backend)
- **Maven** 3.9+ (for backend builds)
- **Docker** & **Docker Compose** (recommended for development)
- **Git** (for version control)

### Development Setup

1. **Fork and Clone**

   ```bash
   # Fork the repository on GitHub first
   git clone https://github.com/trashykoifish1/project.cronos.git
   cd project-cronos
   ```

2. **Start Development Environment**

   ```bash
   # Option 1: Using Docker (Recommended)
   docker-compose -f docker-compose.dev.yml up -d

   # Option 2: Manual setup
   # Terminal 1 - Database
   docker run --name timetracker-db -e POSTGRES_PASSWORD=password -e POSTGRES_DB=timetracker -p 5432:5432 -d postgres:15

   # Terminal 2 - Backend
   cd backend
   mvn spring-boot:run

   # Terminal 3 - Frontend
   cd frontend
   npm install
   npm run dev
   ```

3. **Verify Setup**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api/health
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Testing Your Setup

> [!WARNING]  
> Tests have not been implemented and is planned for the future.

```bash
# Test backend
cd backend
mvn test

# Test frontend
cd frontend
npm run test
npm run lint

# Integration tests
npm run test:e2e
```

## 📋 Development Workflow

### Branch Strategy

- `main` - Production ready code
- `develop` - Integration branch for features
- `feature/feature-name` - Individual features
- `bugfix/bug-description` - Bug fixes
- `hotfix/critical-fix` - Critical production fixes

### Making Changes

1. **Create Feature Branch**

   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b bugfix/issue-description
   ```

2. **Make Changes Following Standards**

   - Keep changes focused and atomic
   - Write meaningful commit messages
   - Add tests for new functionality
   - Update documentation as needed

3. **Test Changes Locally**

   ```bash
   # Backend tests
   cd backend
   mvn clean test
   mvn test -Dtest=*Integration*

   # Frontend tests
   cd frontend
   npm run test
   npm run typecheck
   npm run lint
   ```

4. **Commit Changes**

   ```bash
   git add .
   git commit -m "feat: add time entry bulk delete functionality"
   # or
   git commit -m "fix: resolve timesheet overlap validation issue"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   # Then create Pull Request on GitHub
   ```

## 🧪 Testing Requirements (Optional but preferred)

### Backend Testing

All backend changes must include appropriate tests:

**Unit Tests**

```java
@SpringBootTest
class TimeEntryServiceTest {
    @Test
    void shouldCreateTimeEntryWithValidation() {
        // Test implementation
    }
}
```

**Integration Tests** (For API changes)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class TimeEntryControllerIntegrationTest {
    // Integration test implementation
}
```

### Frontend Testing

Frontend changes should include:

**Component Tests**

```typescript
import { mount } from "@vue/test-utils";
import TimesheetGrid from "~/components/timesheet/TimesheetGrid.vue";

describe("TimesheetGrid", () => {
  it("renders timesheet slots correctly", () => {
    // Test implementation
  });
});
```

**E2E Tests** (For major features)

```typescript
import { test, expect } from "@playwright/test";

test("user can create time entry", async ({ page }) => {
  // E2E test implementation
});
```

## 📝 Code Standards

### Backend (Java/Spring Boot)

- **Java 21** features and syntax
- **Spring Boot 3** best practices
- **Clean Architecture** patterns
- **Comprehensive validation** on all inputs
- **Proper exception handling** with custom exceptions

**Example Service Method:**

```java
@Service
@Transactional
public class TimeEntryService {

    public TimeEntryResponse createTimeEntry(TimeEntryCreateRequest request) {
        // 1. Validate input
        validateTimeEntryRequest(request);

        // 2. Check business rules
        validateNoOverlaps(request);

        // 3. Create entity
        TimeEntry entry = timeEntryMapper.toEntity(request);
        entry.setUser(getCurrentUser());

        // 4. Save and return
        TimeEntry saved = timeEntryRepository.save(entry);
        return timeEntryMapper.toResponse(saved);
    }
}
```

### Frontend (Vue.js/Nuxt 3)

- **TypeScript** for all new code
- **Composition API** style
- **Mobile-first responsive** design
- **Accessibility** considerations (WCAG 2.1 AA)
- **PrimeVue components** when possible

**Example Component:**

```vue
<script setup lang="ts">
interface Props {
  timeEntries: TimeEntry[];
  selectedDate: Date;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "time-entry-created": [entry: TimeEntry];
  "time-entry-updated": [entry: TimeEntry];
}>();

// Component logic here
</script>

<template>
  <div class="timesheet-grid">
    <!-- Template with accessibility -->
  </div>
</template>
```

### Database Migrations

All database changes require Flyway migrations:

```sql
-- V1.5__Add_time_entry_tags.sql
CREATE TABLE time_entry_tags (
    id BIGSERIAL PRIMARY KEY,
    time_entry_id BIGINT NOT NULL REFERENCES time_entries(id),
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_time_entry_tags_entry_id ON time_entry_tags(time_entry_id);
```

## 📚 Documentation Requirements

### Code Documentation

- **Java**: JavaDoc for public methods and classes
- **TypeScript**: JSDoc comments for complex logic
- **API**: OpenAPI/Swagger annotations

### Feature Documentation

When adding new features, update:

- **README.md** - If user-facing feature
- **API Documentation** - For API changes
- **Deployment docs** - If configuration changes

## 🐛 Bug Reports

### Before Reporting

1. **Search existing issues** to avoid duplicates
2. **Try latest version** to see if already fixed
3. **Minimal reproduction** case

### Bug Report Template

```markdown
**Bug Description**
Clear description of the bug

**Steps to Reproduce**

1. Go to '...'
2. Click on '...'
3. See error

**Expected Behavior**
What should happen

**Screenshots**
If applicable

**Environment**

- OS: [e.g. macOS 13]
- Browser: [e.g. Chrome 118]
- Version: [e.g. 1.2.0]
```

## 💡 Feature Requests

### Feature Request Template

```markdown
**Feature Description**
As a [type of user], I want [goal] so that [benefit]

**Use Case**
Describe the specific scenario

**Proposed Solution**
Your suggested approach

**Alternatives Considered**
Other approaches you've thought about

**Additional Context**
Screenshots, mockups, examples
```

## 🔍 Code Review Process

### Pull Request Requirements

- [ ] **Clear title** and description
- [ ] **Tests added** for new functionality
- [ ] **Documentation updated** if needed
- [ ] **No breaking changes** (or clearly marked)
- [ ] **CI passes** (tests, linting, build)

### Review Checklist

**For Reviewers:**

- [ ] Code follows project conventions
- [ ] Tests cover new functionality
- [ ] Performance implications considered
- [ ] Security implications reviewed
- [ ] Documentation is clear and accurate

**For Contributors:**

- [ ] Self-review completed
- [ ] Tests pass locally
- [ ] Documentation updated
- [ ] Commit messages are clear
- [ ] Breaking changes documented

## 🏷️ Issue Labels

We use these labels to organize issues:

**Type:**

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `question` - Further information needed

**Priority:**

- `priority: high` - Critical issues
- `priority: medium` - Important improvements
- `priority: low` - Nice to have

**Area:**

- `area: frontend` - Frontend/UI changes
- `area: backend` - Backend/API changes
- `area: database` - Database related
- `area: deployment` - Docker/deployment related

## 📞 Getting Help

**Stuck? Need help?**

- 💬 **GitHub Discussions** - Ask questions, share ideas
- 🐛 **GitHub Issues** - Report bugs or request features
- 📖 **Documentation** - Check our docs/ folder
- 🔍 **Code Examples** - Look at existing implementations

## 🎉 Recognition

Contributors will be recognized in:

- **README.md** contributors section
- **CHANGELOG.md** for significant contributions
- **GitHub contributors** page

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for contributing to Project Cronos! 🚀**

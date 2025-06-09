# Frontend Dockerfile for Nuxt 3 Time Tracker

FROM node:20-alpine AS base
RUN npm install -g npm@latest

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app

# Install dependencies based on the preferred package manager
COPY package.json package-lock.json* ./

# Use npm ci with platform-specific rebuild for native modules
RUN npm ci --only=production && \
    npm rebuild

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Set NODE_ENV to production for build
ENV NODE_ENV=production

# Build the application with verbose logging
RUN npm run build -- --verbose

# Production image, copy all the files and run nuxt
FROM node:20-alpine AS runner
WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache \
    dumb-init \
    curl

ENV NODE_ENV=production
ENV NUXT_HOST=0.0.0.0
ENV NUXT_PORT=3000

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nuxtjs --ingroup nodejs

# Copy the built application
COPY --from=builder --chown=nuxtjs:nodejs /app/.output ./.output

USER nuxtjs

EXPOSE 3000

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]
CMD ["node", ".output/server/index.mjs"]
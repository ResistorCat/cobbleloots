# ==========================================
# Stage 1: Build & Bundle
# ==========================================
FROM node:24-slim AS builder
WORKDIR /app

# Install native dependencies required for better-sqlite3 compilation
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    make \
    g++ \
    && rm -rf /var/lib/apt/lists/*

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM node:24-slim AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV DATABASE_PATH=/app/data/bot.db

# Install native dependencies, install production dependencies, and clean up build tools
COPY package*.json ./
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    make \
    g++ \
    && npm ci --omit=dev \
    && apt-get purge -y python3 make g++ \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

# Copy built application output, agent directory, runner, and eve metadata from builder stage
COPY --from=builder /app/.output ./.output
COPY --from=builder /app/.eve ./.eve
COPY --from=builder /app/agent ./agent
COPY --from=builder /app/runner.mjs ./runner.mjs
COPY --from=builder /app/tsconfig.json ./tsconfig.json

# Create persistent data directory and ensure proper ownership for non-root node user
RUN mkdir -p /app/data && chown -R node:node /app

USER node
EXPOSE 3000

CMD ["npm", "run", "start"]

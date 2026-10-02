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
RUN npm ci && (test -f node_modules/better-sqlite3/build/Release/better_sqlite3.node || (cd node_modules/better-sqlite3 && npx --yes node-gyp rebuild))

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

# Install production dependencies only (skipping native compilation since precompiled addon is copied from builder)
COPY package*.json ./
RUN npm ci --omit=dev --ignore-scripts

# Copy built application output, agent directory, runner, and eve metadata from builder stage
COPY --from=builder /app/.output ./.output
COPY --from=builder /app/.eve ./.eve
COPY --from=builder /app/agent ./agent
COPY --from=builder /app/runner.mjs ./runner.mjs
COPY --from=builder /app/tsconfig.json ./tsconfig.json

# Copy compiled better_sqlite3 native addon from builder stage to both node_modules and bundled Nitro locations
RUN mkdir -p /app/node_modules/better-sqlite3/build/Release /app/.output/server/build/Release
COPY --from=builder /app/node_modules/better-sqlite3/build/Release/better_sqlite3.node /app/node_modules/better-sqlite3/build/Release/better_sqlite3.node
COPY --from=builder /app/node_modules/better-sqlite3/build/Release/better_sqlite3.node /app/.output/server/build/Release/better_sqlite3.node

# Create persistent data directory and ensure proper ownership for non-root node user
RUN mkdir -p /app/data && chown -R node:node /app

USER node
EXPOSE 3000

CMD ["npm", "run", "start"]

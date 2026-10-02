# ==========================================
# Stage 1: Build & Bundle
# ==========================================
FROM node:24-slim AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci --ignore-scripts

COPY . .
RUN npm run build

# Copy prebuilt better-sqlite3 native addon to .output/server locations expected by bundled Nitro
RUN ARCH=$(uname -m | sed 's/x86_64/x64/' | sed 's/aarch64/arm64/') && \
    cp -r /app/node_modules/better-sqlite3/prebuilds /app/.output/server/prebuilds && \
    mkdir -p /app/.output/server/build/Release && \
    cp /app/node_modules/better-sqlite3/prebuilds/linux-${ARCH}.node /app/.output/server/build/Release/better_sqlite3.node

# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM node:24-slim AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV DATABASE_PATH=/app/data/bot.db

# Install production dependencies only (using prebuilt native binaries)
COPY package*.json ./
RUN npm ci --omit=dev --ignore-scripts

# Copy built application output, agent directory, runner, and eve metadata from builder stage
COPY --from=builder /app/.output ./.output
COPY --from=builder /app/.eve ./.eve
COPY --from=builder /app/agent ./agent
COPY --from=builder /app/runner.mjs ./runner.mjs
COPY --from=builder /app/tsconfig.json ./tsconfig.json

# Ensure better_sqlite3.node is present in node_modules build/Release for the runner process
RUN ARCH=$(uname -m | sed 's/x86_64/x64/' | sed 's/aarch64/arm64/') && \
    mkdir -p /app/node_modules/better-sqlite3/build/Release && \
    cp /app/node_modules/better-sqlite3/prebuilds/linux-${ARCH}.node /app/node_modules/better-sqlite3/build/Release/better_sqlite3.node

# Create persistent data directory and ensure proper ownership for non-root node user
RUN mkdir -p /app/data && chown -R node:node /app

USER node
EXPOSE 3000

CMD ["npm", "run", "start"]

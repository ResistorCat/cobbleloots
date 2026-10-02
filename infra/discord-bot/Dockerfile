# ==========================================
# Stage 1: Build & Bundle
# ==========================================
FROM node:24-slim AS builder
WORKDIR /app

# Enable Corepack and pnpm
RUN corepack enable && corepack prepare pnpm@11.20.0 --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml* ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm run build

# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM node:24-slim AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

# Enable Corepack and pnpm for production runtime
RUN corepack enable && corepack prepare pnpm@11.20.0 --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml* ./
RUN pnpm install --prod --frozen-lockfile

# Copy built application output, agent directory, runner, and eve metadata from builder stage
COPY --from=builder /app/.output ./.output
COPY --from=builder /app/.eve ./.eve
COPY --from=builder /app/agent ./agent
COPY --from=builder /app/runner.mjs ./runner.mjs
COPY --from=builder /app/tsconfig.json ./tsconfig.json
# Ensure ownership for unprivileged node user
RUN chown -R node:node /app

USER node
EXPOSE 3000

CMD ["pnpm", "run", "start"]

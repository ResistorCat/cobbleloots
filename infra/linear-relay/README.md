# CobbleLoots Linear Webhook Relay

Este componente es un **Cloudflare Worker** serverless ultraligero que conecta **Linear** con la **Software Factory** en GitHub Actions.

---

## Flujo de Operación

1. Recibe el webhook de Linear cuando un ticket es creado o actualizado.
2. Valida la firma criptográfica HMAC-SHA256 (`linear-signature`).
3. Comprueba si el ticket cumple con cualquiera de estas condiciones:
   - Está asignado al usuario `Agent`.
   - Tiene la etiqueta `agent:run`.
4. Si cumple la condición:
   - Actualiza el estado del ticket a **`In Progress`** en Linear.
   - Publica un comentario en el ticket indicando el inicio del trabajo y la rama asignada.
   - Dispara un `repository_dispatch` hacia el repositorio `ResistorCat/cobbleloots` con el evento `linear_agent_task`.

---

## Requisitos Previos

- [Node.js](https://nodejs.org/) v18+ y `npm` o `pnpm`.
- Cuenta gratuita en [Cloudflare](https://dash.cloudflare.com/).
- Cuenta en [Linear](https://linear.app/) con permisos de administración en el workspace.
- Un Personal Access Token (PAT) de GitHub con permisos `repo` y `workflow`.

---

## Configuración de Secretos

Instala las dependencias y autentícate en Cloudflare:

```bash
cd infra/linear-relay
npm install
npx wrangler login
```

Configura los 3 secretos requeridos mediante `wrangler secret put`:

```bash
# 1. Secreto del Webhook de Linear (obtenido al crear el webhook en Linear)
npx wrangler secret put LINEAR_WEBHOOK_SECRET

# 2. Clave de API de Linear (Linear > Settings > Security & Access > Personal API Keys)
npx wrangler secret put LINEAR_API_KEY

# 3. Personal Access Token de GitHub (GitHub > Settings > Developer Settings > Personal Access Tokens)
npx wrangler secret put GITHUB_PAT
```

---

## Despliegue en Cloudflare Workers

Despliega el worker con un solo comando:

```bash
npx wrangler deploy
```

La salida mostrará la URL pública de tu worker, por ejemplo:
`https://cobbleloots-linear-relay.<tu-subdominio>.workers.dev`

---

## Configuración del Webhook en Linear

1. Ve a **Linear** > **Settings** > **API** > pestaña **Webhooks**.
2. Haz clic en **New Webhook**.
3. Configura:
   - **URL**: La URL de tu Cloudflare Worker (ej. `https://cobbleloots-linear-relay.<tu-subdominio>.workers.dev`).
   - **Events**: Marca **Issues** (Create y Update).
   - Copia el **Signing Secret** generado y guárdalo en Cloudflare como `LINEAR_WEBHOOK_SECRET`.
4. Guarda el webhook. ¡Listo! Ahora cualquier ticket asignado a `Agent` o etiquetado con `agent:run` disparará automáticamente la Software Factory.


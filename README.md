# 🛡️ StaffChat (Fabric 1.21.1)

Un mod de Fabric ligero, moderno y estrictamente del lado del servidor (server-side) diseñado para facilitar la comunicación privada entre los miembros del equipo de tu servidor. 

StaffChat intercepta los mensajes antes de que lleguen al flujo global, garantizando **compatibilidad nativa** con mods de chat globales como *Styled Chat* o *Fuji Essentials*, ya que no interfiere ni es interferido por ellos.

## ✨ Características Principales
- **🧩 100% Server-Side:** Los jugadores no necesitan descargar el mod para entrar a tu servidor.
- **🚥 Canales Independientes:** Soporte nativo para `StaffChat`, `ModChat`, y `AdminChat`.
- **🔑 Integración Inyectada de LuckPerms:** Soporte directo y extra-seguro para la API de LuckPerms. Si tienes LuckPerms, detectará automáticamente tus rangos/prefijos (incluyendo formatos clásicos de color como `&c[Admin]`) y los renderizará en el canal.
- **⚙️ Configuración Dinámica:** Todos los colores, mensajes de activación, de consola y de formatos son completamente configurables sin reiniciar el servidor.

---

## 📥 Instalación

1. Descarga el archivo `.jar` de la versión correspondiente.
2. Colócalo en la carpeta `mods/` de tu servidor Fabric.
3. **Dependencias Requeridas:**
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Fabric Permissions API (LuckPerms)](https://modrinth.com/mod/fabric-permissions-api) *(Requerido para manejar los permisos y lectura de grupos).*
4. Inicia el servidor. Se generará automáticamente el archivo `config/staffchat.json`.

---

## 🎮 Comandos y Permisos

Puedes usar los comandos de dos maneras:
- `/staffchat` -> **(Modo Toggle)** Ancla tu chat. A partir de ahora todos los mensajes que escribas irán por el canal privado sin necesidad de usar comandos. Vuelve a escribirlo para desactivarlo.
- `/staffchat ¡Hola a todos!` -> **(Modo Directo)** Envía un único mensaje rápido al canal sin activar el modo toggle.

| Canal / Comando | Permiso de Uso (Escribir) | Permiso de Lectura (Ver) |
|---|---|---|
| `/staffchat` | `staffchat.use` | `staffchat.read` |
| `/modchat` | `modchat.use` | `modchat.read` |
| `/adminchat` | `adminchat.use` | `adminchat.read` |

**Comandos Administrativos:**
- `/staffchatreload` (Permiso: `staffchat.reload`, por defecto Nivel 3) -> Recarga el archivo de configuración `.json` al instante sin reiniciar el servidor.

---

## ⚙️ Configuración (`config/staffchat.json`)

El mod generará un archivo fácil de modificar en tu carpeta `config/`. 
Puedes utilizar el comodín `%luckperms_prefix%` para que el mod lea tu rango automáticamente, o personalizar tu propio prefijo (`%prefix%`).

```json
{
  "channels": {
    "staffchat": {
      "command": "staffchat",
      "prefix": "⚒ Staff",
      "color": "AQUA"
    },
    "modchat": {
      "command": "modchat",
      "prefix": "🛡 Mod",
      "color": "GREEN"
    },
    "adminchat": {
      "command": "adminchat",
      "prefix": "👑 Admin",
      "color": "RED"
    }
  },
  "messages": {
    "toggledOn": "Activaste el canal privado %prefix%.",
    "switchedChannel": "Cambiaste del chat %old_prefix% a %new_prefix%.",
    "toggledOff": "Desactivaste el canal privado %prefix%.",
    "consoleDeny": "La consola no puede anclar el chat. Usa /%command% <mensaje>",
    "messageFormat": "[%prefix%] %luckperms_prefix%%player_name%: %message%"
  }
}
```

---
*Desarrollado para entornos Fabric - Hecho a la medida.*

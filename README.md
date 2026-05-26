# 🛡️ BeAuth

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.4+-blue?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Version">
  <img src="https://img.shields.io/badge/Build-Maven-red?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven Build">
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License">
</p>

---

**BeAuth** is a modern, secure, and highly performant authentication plugin for Minecraft Bukkit/Spigot/Paper servers. Designed with security best practices, it supports both offline (cracked) and premium servers, offering advanced features for account protection and server security.

---

## ✨ Key Features

*   **⚡ Automatic Premium System**: Automatically detects authentic premium players and logs them in without requiring a password.
*   **🔒 Maximum Security**: Secure password hashing using **BCrypt** with customizable hashing rounds.
*   **🌍 Built-in AntiVPN & Proxy**: Real-time detection powered by `proxycheck.io` with in-memory caching to prevent rate-limit bans, and support for whitelists and bypass permissions.
*   **🗄️ Database Flexibility**: Out-of-the-box support for both **SQLite** (for quick setup) and **MySQL** with high-performance connection pooling via **HikariCP**.
*   **🎮 Modern UI Experience**: Support for dynamic Actionbars, Titles and subtitles on join, custom sound effects, and particle animations.
*   **💼 Advanced Integrations**:
    *   **LuckPerms**: Automatically assigns a temporary group to unauthenticated players (e.g., `unauth`).
    *   **PlaceholderAPI**: Placeholders to show player status.
    *   **Discord Webhook**: Sends real-time logs (failed logins, registrations, password changes) directly to your Discord server.
    *   **Update Checker**: Automatically checks for new versions of the plugin on GitHub.
    *   **bStats**: Anonymous charts to track plugin metrics.

---

## 🚫 AntiVPN System

The BeAuth AntiVPN system analyzes the player's IP address before they fully connect to the server.

### AntiVPN Features:
- **Asynchronous**: The API request is executed on an asynchronous Netty thread during the `AsyncPlayerPreLoginEvent`, preventing any server lag.
- **Efficient Caching**: Stores results in an in-memory cache (configurable duration) to minimize queries to the external API.
- **Customizable**: Customizable kick message supporting Minecraft color codes (`&`) and the `%ip%` placeholder.
- **Flexible Bypass**: Define IP and player whitelists, or grant the `beauth.antivpn.bypass` permission to staff members or VIP players.

---

## ⚙️ Configuration

### `config.yml` (Main Configuration)
The main configuration file controls the database settings, login limits, forbidden passwords, UI elements, and integrations.
[View config.yml](file:///c:/Users/beffa/Downloads/BeAuth/src/main/resources/config.yml)

### `antivpn.yml` (AntiVPN Configuration)
Configure the AntiVPN and Proxy blocker.
```yaml
# ==========================================
# BeAuth - AntiVPN Configuration
# ==========================================

# Enable or disable the AntiVPN checks
enabled: true

# The kick message shown to players detected using a VPN/Proxy.
# Use %ip% as a placeholder for the player's IP address.
# Color codes (&) are supported.
kick-message: "&cANTIVPN >> non puoi entrare con questo ip %ip%"

# API settings for VPN detection
api:
  # The detection service to use (default: "proxycheck")
  service: "proxycheck"
  
  # API Key for proxycheck.io (optional, free tier allows 1000 daily checks without a key)
  key: ""
  
  # Connection and read timeout in milliseconds for the API request
  timeout: 3000

# Cache settings to avoid hitting API rate limits on every join
cache:
  # Cache duration in hours. Set to 0 to disable caching.
  duration-hours: 24

# Whitelist settings - players or IPs that are allowed to bypass the VPN check
whitelist:
  players:
    - "beffaxone"
  ips:
    - "127.0.0.1"
    - "0:0:0:0:0:0:0:1"

# Bypass permission. If true, players with 'beauth.antivpn.bypass' bypass the check.
permission-bypass: true
```

---

## 💻 Commands & Permissions

### Player Commands
- `/register <password> <confirm>` - Register a new account.
- `/login <password>` - Log in to your account.
- `/changepassword <old> <new>` - Change your account password.
- `/premium` - Enable or disable premium auto-login for your account.

### Admin Commands
All administrator commands require the `beauth.admin` permission.
- `/beauth reload` - Reloads all configuration files (`config.yml`, `messages.yml`, and `antivpn.yml`) and clears the VPN cache. (Permission: `beauth.admin.reload`)
- `/beauth info <player>` - Retrieves registration details for a player (IP, date, premium status, UUID).
- `/beauth forcechangepassword <player> <newPassword>` - Forces a password change for a specific player. (Permission: `beauth.admin.forcechangepassword`)
- `/beauth premium <player> <true|false>` - Manually toggles the premium status of a player. (Permission: `beauth.admin.premium`)
- `/beauth unregister <player>` - Deletes a player's registration. (Permission: `beauth.admin.unregister`)

### Other Permissions
- `beauth.antivpn.bypass` - Allows joining the server even when using a VPN or Proxy.

---

## 🛠️ Compilation

To compile the source code manually, make sure you have **Java 21** and **Maven** installed, then run:

```bash
mvn clean package
```

The optimized `.jar` file will be generated in the `target/` directory.

---

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for more details.

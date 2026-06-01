# 🛡️ BeAuth

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.4+-blue?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Version">
  <img src="https://img.shields.io/badge/Build-Maven-red?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven Build">
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License">
</p>

---

**BeAuth** is a modern, high-performance, and secure authentication solution designed for Minecraft servers and proxy networks. It features a complete multi-module architecture providing native support for **Spigot/Paper**, **BungeeCord**, and **Velocity**. BeAuth guarantees robust name-spoof protection, seamless premium auto-login, and smart AntiVPN filtering.

---

## 🚀 Key Features

*   **⚡ Premium Auto-Login**: Automatically detects authentic premium players and logs them in password-free.
*   **🔒 High-Grade Security**: Secure password hashing using **BCrypt** with configurable hashing rounds.
*   **🌐 Proxy Network Support**: Native companion plugins for BungeeCord & Velocity force online-mode checks at the gateway, preventing UUID/name spoofing.
*   **🛡️ Built-in AntiVPN & AntiProxy**: Real-time IP reputation checks using `proxycheck.io` with fast, thread-safe in-memory caching to bypass API rate limits.
*   **🗄️ Database Flexibility**: Out-of-the-box support for SQLite and MySQL with high-performance HikariCP connection pooling.
*   **🎮 Premium UX Elements**: Beautiful actionbars, join title animations, custom sounds, and particle effects.
*   **🔌 Rich Integrations**:
    *   **LuckPerms**: Temporary group assignments (e.g. `unauth`) before logging in.
    *   **PlaceholderAPI**: Placeholders displaying player status.
    *   **Discord Webhooks**: Sends instant security alerts for failed logins, password changes, and registrations.

---

## 💻 Commands & Permissions

### Player Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/register <password> <confirm>` | Register a new account | *None (All players)* |
| `/login <password>` | Log into your account | *None (All players)* |
| `/changepassword <old> <new>` | Change your current password | *None (All players)* |
| `/premium` | Toggle Auto-Login GUI or Status | *None (All players)* |

---

### Admin Commands

All administrator commands require the `beauth.admin` permission node.

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/beauth reload` | Reload configurations and clear caches | `beauth.admin.reload` |
| `/beauth info <player>` | Retrieve details (IP, premium status, registration date) | `beauth.admin.info` |
| `/beauth forcechangepassword <player> <new>` | Forcefully change a player's password | `beauth.admin.forcechangepassword` |
| `/beauth premium <player> <true\|false>` | Manually set a player's premium status | `beauth.admin.premium` |
| `/beauth unregister <player>` | Completely delete a player's account | `beauth.admin.unregister` |

---

### Security Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `beauth.antivpn.bypass` | Allows the player to bypass the AntiVPN / AntiProxy check | OP |

---

## ⚙️ Project Structure

BeAuth is organized as a multi-module Maven project to clean compilation output:

```
BeAuth (root)
├── beauth-spigot    # Spigot/Paper backend plugin
├── beauth-bungee    # BungeeCord companion gateway plugin
└── beauth-velocity  # Velocity companion gateway plugin
```

---

## 🛠️ Compilation & Installation

### 1. Build from Source
To compile the entire suite, run the following Maven command at the project root (requires **Java 21**):
```bash
mvn clean package
```
After building, the respective executable JAR files will be generated under the `target/` directory of each module:
*   **Spigot Backend**: `beauth-spigot/target/BeAuth-1.0.0.jar`
*   **BungeeCord**: `beauth-bungee/target/BeAuth-Bungee-1.0.0.jar`
*   **Velocity**: `beauth-velocity/target/BeAuth-Velocity-1.0.0.jar`

### 2. Setup
1. Place the Proxy JAR (`BeAuth-Bungee` or `BeAuth-Velocity`) inside the proxy's `plugins/` directory.
2. Place the backend JAR (`BeAuth`) inside the Spigot/Paper server's `plugins/` directory.
3. Configure IP forwarding on your proxy (`ip_forward: true` for Bungee, or Modern forwarding for Velocity) and Paper.
4. Restart the servers to generate default configuration files and adjust values accordingly.

---

## 📄 License
This project is licensed under the **MIT License**. See the `LICENSE` file for more details.

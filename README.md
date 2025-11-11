# 🚀 GigTasker Config Server

This service is the **"Head Chef"** of the GigTasker microservice platform. It is a [Spring Cloud Config Server](https://spring.io/projects/spring-cloud-config) and acts as the **single source of truth** for all externalized configuration.

Its *only* job is to provide configuration "recipes" to all other services in the ecosystem (like the `api-gateway`, `user-service`, etc.) when they start up.

---

## ✨ Core Responsibility

This server connects to a **private** Git repository (`gigtasker-config`) which holds all the `.yml` properties files for the platform.

When a service (e.g., `user-service`) starts, it calls this server. This server then "serves" a merged set of configuration files based on:
1.  **Global Config:** `application.yml` (applies to all services)
2.  **Service-Specific Config:** `user-service.yml`
3.  **Profile-Specific Config:** `user-service-local.yml` (since `local` is the default)

---

## 🛠️ Tech Stack

* **Spring Boot 3**
* **Java 25**
* **Spring Cloud Config Server**
* **JGit:** The Java library used to communicate with the Git repository.

---

## ⚙️ Configuration

This service is unique as it's the *one* service that cannot get its *own* configuration from itself. Its configuration lives in `src/main/resources/application.yml` and is critical for platform operation.

### 🔐 SSH Key Authentication

To connect to the private `gigtasker-config` GitHub repository, this server uses SSH key authentication.

**IMPORTANT:** The private SSH key is **NEVER** committed to this (or any) repository. It is securely injected at runtime as an **Environment Variable**.

The `application.yml` is configured to *look* for this variable:

```yaml
# in config-server/src/main/resources/application.yml
spring:
  cloud:
    config:
      server:
        git:
          # 1. The SSH URI for the private config repo
          uri: git@github.com:utkarsh-lohani/gigtasker-config.git
          
          # 2. We ignore local SSH configs (like ~/.ssh/config)
          ignore-local-ssh-settings: true
          
          # 3. Disable host key checking for local dev
          strict-host-key-checking: false
          
          # 4. This is the placeholder. Spring Boot will find the
          #    environment variable named CONFIG_REPO_PRIVATE_KEY
          #    and inject its value here.
          private-key: ${CONFIG_REPO_PRIVATE_KEY}
```

### 🚀 Default Local Profile

To make local development seamless, this server is configured to provide the `local` profile by default to any service that doesn't specify one. This is how all your services automatically know to use `hostname: localhost` from their `...-local.yml` files.

```yaml
# in config-server/src/main/resources/application.yml
spring:
  cloud:
    config:
      server:
        # ... (git settings) ...
        default-profile: local
```

---

## 🚀 How to Run

### 1. Set the Environment Variable (CRITICAL)

Before you can run this, you must provide the `CONFIG_REPO_PRIVATE_KEY` variable.

* **Generate the Key:** You need a single-line version of your `gigtasker_config_key` file. You can get this in Git Bash with:
    ```bash
    awk 'NF {printf "%s\\n", $0;}' ~/.ssh/gigtasker_config_key | clip
    ```
* **Set in IntelliJ:**
    1.  Go to **Run/Debug Configurations...**
    2.  Select your `ConfigServerApplication`.
    3.  Click the "Browse" icon next to **Environment variables**.
    4.  Click `+` and add:
        * **Name:** `CONFIG_REPO_PRIVATE_KEY`
        * **Value:** (Paste the entire single-line key from your clipboard).



### 2. Run the Application

This service is **Pillar 1** of the entire backend. It **MUST** be the first service you start.

1.  (Do the "First Handshake" once in Git Bash: `ssh -T git@github.com-gigtasker-config`)
2.  Run the `ConfigServerApplication.java` from your IDE.
3.  The server will start on **`http://localhost:8888`**.

Once this service is running, all other services (`service-registry`, `api-gateway`, etc.) can be started.
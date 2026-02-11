# NotifyFlow Nova

Framework-agnostic notification library for Java 21+ that unifies sending notifications across multiple channels (Email, SMS, Push, Slack) through a single, extensible API.

## Features

- **Unified API** - Same interface for all channels
- **Multi-channel** - Email, SMS, Push Notifications, Slack
- **Multi-provider** - SendGrid, Mailgun, Twilio, Vonage, FCM, APNs, Slack Webhooks
- **Async & Batch** - `CompletableFuture`-based async sending with batch support
- **Retry with Backoff** - Configurable retry policy with exponential backoff
- **Message Templates** - Reusable templates with `{{variable}}` placeholders
- **Event System (Pub/Sub)** - Subscribe to notification lifecycle events
- **Validation** - Built-in validators per channel (email format, E.164 phone, device tokens)
- **Result Type** - No exception-based control flow; safe for batch processing
- **Framework-agnostic** - No Spring, no Quarkus, no CDI. Pure Java
- **Java 21** - Records, sealed interfaces, pattern matching, switch expressions

## Installation

### Maven

```xml
<dependency>
    <groupId>com.nova.notifications</groupId>
    <artifactId>notifyflow-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.nova.notifications:notifyflow-core:1.0.0'
```

> **Note:** NotifyFlow uses SLF4J as logging API. You must provide your own SLF4J implementation (Logback, Log4j2, slf4j-simple, etc.) in your project.

## Quick Start

```java
// 1. Build the notification system
var notifyFlow = NotifyFlow.builder()
    .withSendGrid("your-sendgrid-api-key")
    .withTwilio("your-account-sid", "your-auth-token")
    .withFcm("your-fcm-server-key")
    .build();

// 2. Send an email
var email = EmailNotification.simple(
    "noreply@yourapp.com",
    "user@example.com",
    "Welcome!",
    "Thank you for signing up."
);
var result = notifyFlow.send(email);

if (result.successful()) {
    System.out.println("Sent! ID: " + result.notificationId());
} else {
    System.out.println("Failed: " + result.errorMessage());
}

// 3. Send an SMS
var sms = new SmsNotification("+15551234567", "+15559876543", "Your code is 847291");
notifyFlow.send(sms);

// 4. Send a Push Notification
var push = PushNotification.withData(
    "device-registration-token",
    "New Message",
    "You have a new message from Alice",
    Map.of("chatId", "42", "action", "open_chat")
);
notifyFlow.send(push);
```

## Configuration

All configuration is done through Java code using the fluent builder. No YAML, no properties files, no annotations.

### Email Channel

```java
// Option A: SendGrid
NotifyFlow.builder().withSendGrid("SG.your-api-key")

// Option B: Mailgun
NotifyFlow.builder().withMailgun("your-api-key", "your-domain.com")

// Option C: Custom provider
NotifyFlow.builder().withEmail(new YourCustomEmailProvider())
```

### SMS Channel

```java
// Option A: Twilio
NotifyFlow.builder().withTwilio("account-sid", "auth-token")

// Option B: Vonage
NotifyFlow.builder().withVonage("api-key", "api-secret")
```

### Push Channel

```java
// Option A: Firebase Cloud Messaging
NotifyFlow.builder().withFcm("server-key")

// Option B: Apple Push Notification service
NotifyFlow.builder().withApns("team-id", "key-id", "bundle-id")
```

### Slack Channel

```java
NotifyFlow.builder().withSlackWebhook("https://hooks.slack.com/services/T00/B00/xxx")
```

### Full Configuration Example

```java
var notifyFlow = NotifyFlow.builder()
    .withSendGrid("SG.api-key")
    .withTwilio("AC-sid", "auth-token")
    .withFcm("server-key")
    .withSlackWebhook("https://hooks.slack.com/services/...")
    .withRetryPolicy(new RetryPolicy(3, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(30)))
    .withAsyncExecutor(Executors.newFixedThreadPool(4))
    .withTemplate("welcome", "Hello {{name}}, welcome to {{company}}!")
    .withTemplate("otp", "Your code is {{code}}. Expires in {{minutes}} min.")
    .onEvent(event -> log.info("Notification {}: {}", event.eventType(), event.recipient()))
    .build();
```

## Async & Batch Sending

```java
// Async single send
CompletableFuture<NotificationResult> future = notifyFlow.sendAsync(email);
future.thenAccept(result -> {
    if (result.successful()) log.info("Sent async!");
});

// Batch send (fail-soft: one failure doesn't cancel others)
var notifications = List.of(email1, email2, sms1, push1, slack1);
CompletableFuture<List<NotificationResult>> results = notifyFlow.sendBatch(notifications);
results.thenAccept(list -> {
    long ok = list.stream().filter(NotificationResult::successful).count();
    log.info("{}/{} sent successfully", ok, list.size());
});
```

## Retry with Backoff

```java
// Default: 3 attempts, 1s initial delay, 2x backoff, 30s max
notifyFlow = NotifyFlow.builder()
    .withSendGrid("key")
    .withRetryPolicy(RetryPolicy.defaultPolicy())
    .build();

// Custom policy
var policy = new RetryPolicy(5, Duration.ofMillis(500), 1.5, Duration.ofSeconds(10));
notifyFlow = NotifyFlow.builder()
    .withSendGrid("key")
    .withRetryPolicy(policy)
    .build();

// Send with retry (only retries provider errors, not validation)
var result = notifyFlow.sendWithRetry(email);
```

## Message Templates

```java
var notifyFlow = NotifyFlow.builder()
    .withSendGrid("key")
    .withTemplate("welcome", "Hello {{name}}, welcome to {{company}}!")
    .withTemplate("otp", "Code: {{code}} (expires in {{minutes}} min)")
    .build();

// Render and use
String body = notifyFlow.renderTemplate("welcome", Map.of("name", "Alice", "company", "TechCorp"));
var email = EmailNotification.simple("noreply@app.com", "alice@mail.com", "Welcome!", body);
notifyFlow.send(email);
```

## Event System (Pub/Sub)

Subscribe to notification lifecycle events for logging, metrics, or alerting:

```java
var notifyFlow = NotifyFlow.builder()
    .withSendGrid("key")
    .onEvent(event -> {
        switch (event.eventType()) {
            case SENDING  -> log.debug("Sending to {}", event.recipient());
            case SENT     -> metrics.increment("notifications.sent");
            case FAILED   -> alerting.notify("Failed: " + event.recipient());
            case RETRYING -> log.warn("Retrying attempt {}", event.attempt());
        }
    })
    .build();

// Events: QUEUED, SENDING, SENT, FAILED, RETRYING
```

## Error Handling

NotifyFlow uses a Result type pattern instead of exceptions:

```java
var result = notifyFlow.send(email);

if (result.successful()) {
    // result.notificationId() - provider-generated ID
} else {
    // result.errorSource() - "VALIDATION", "PROVIDER:SendGrid", "CONFIGURATION", "SYSTEM"
    // result.errorMessage() - human-readable description
    // result.cause()        - original exception (if any)
}
```

Error sources:
- `VALIDATION` - Invalid notification data (bad email, missing fields)
- `PROVIDER:<name>` - Provider failed (API error, timeout)
- `CONFIGURATION` - Channel not configured or unavailable
- `SYSTEM` - Unexpected runtime error

## Supported Providers

| Channel | Provider | Config Method |
|---------|----------|---------------|
| Email | SendGrid (v3 API) | `withSendGrid(apiKey)` |
| Email | Mailgun | `withMailgun(apiKey, domain)` |
| SMS | Twilio | `withTwilio(accountSid, authToken)` |
| SMS | Vonage | `withVonage(apiKey, apiSecret)` |
| Push | Firebase Cloud Messaging | `withFcm(serverKey)` |
| Push | Apple Push Notifications | `withApns(teamId, keyId, bundleId)` |
| Slack | Incoming Webhooks | `withSlackWebhook(webhookUrl)` |

## API Reference

### Core Classes

| Class | Description |
|-------|-------------|
| `NotifyFlow` | Main facade. Use `NotifyFlow.builder()` to start |
| `NotifyFlowBuilder` | Fluent builder for configuration |
| `NotificationResult` | Immutable result of a send attempt |
| `RetryPolicy` | Retry configuration (attempts, backoff) |
| `TemplateRegistry` | Message template storage and rendering |

### Notification Types

| Type | Fields |
|------|--------|
| `EmailNotification` | `from`, `to`, `subject`, `body`, `isHtml`, `cc`, `bcc` |
| `SmsNotification` | `from`, `phoneNumber`, `message` |
| `PushNotification` | `deviceToken`, `title`, `body`, `data`, `badge`, `sound` |
| `SlackNotification` | `channel`, `message`, `username`, `iconEmoji` |

### Interfaces (for extending)

| Interface | Purpose |
|-----------|---------|
| `Notification` | Sealed interface — add new permitted records for new channels |
| `NotificationChannel<T>` | Implement for custom channels |
| `NotificationProvider<T>` | Implement for custom providers |
| `NotificationValidator<T>` | Implement for custom validators |
| `EventListener` | Implement for custom event handling |

## Extending the Library

### Adding a New Provider

```java
public class AmazonSesProvider implements NotificationProvider<EmailNotification> {

    private final String accessKey;

    public AmazonSesProvider(String accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public NotificationResult send(EmailNotification notification) {
        // Your SES implementation here
        return NotificationResult.success("ses-" + UUID.randomUUID());
    }

    @Override
    public String getProviderName() {
        return "AmazonSES";
    }
}

// Use it
var notifyFlow = NotifyFlow.builder()
    .withEmail(new AmazonSesProvider("access-key"))
    .build();
```

### Adding a New Channel

```java
// 1. Define the notification type
public record WhatsAppNotification(String phoneNumber, String message) implements Notification {
    @Override public ChannelType channelType() { return ChannelType.WHATSAPP; } // Add to enum
    @Override public String recipient() { return phoneNumber; }
}

// 2. Create provider, validator, and channel
// 3. Register with builder
notifyFlow = NotifyFlow.builder()
    .withChannel(ChannelType.WHATSAPP, new WhatsAppChannel(provider, validator))
    .build();
```

## Security Best Practices

- **Never hardcode credentials** in source code. Use environment variables or a secrets manager:
  ```java
  NotifyFlow.builder()
      .withSendGrid(System.getenv("SENDGRID_API_KEY"))
      .withTwilio(System.getenv("TWILIO_SID"), System.getenv("TWILIO_TOKEN"))
      .build();
  ```
- **Credentials are never logged** - All providers mask API keys, tokens, and secrets in log output as `[***]`
- **Credentials are never in `toString()`** - Provider objects don't expose sensitive data
- **Use a secrets manager** in production (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault)
- **Rotate API keys** regularly following each provider's recommendation

## Running with Docker

No Java or Maven installation required — Docker handles everything.

### Prerequisites

- **Docker** installed (verify: `docker --version`)

### Build & Run

```bash
# 1. Build the image (multi-stage: compiles with JDK 21, runs on JRE 21 Alpine)
docker build -t notifyflow-demo -f notifyflow-demo/docker/Dockerfile .

# 2. Run all demos
docker run --rm notifyflow-demo

# 3. Run a specific demo mode
docker run --rm -e DEMO_MODE=sync notifyflow-demo
docker run --rm -e DEMO_MODE=async notifyflow-demo
docker run --rm -e DEMO_MODE=batch notifyflow-demo
docker run --rm -e DEMO_MODE=retry notifyflow-demo
docker run --rm -e DEMO_MODE=template notifyflow-demo
docker run --rm -e DEMO_MODE=validation notifyflow-demo
```

Available modes: `all`, `sync`, `async`, `batch`, `retry`, `template`, `validation`

## Project Structure

```
notifyflow-nova/
├── notifyflow-core/              # Core library (no framework dependencies)
│   └── src/main/java/
│       └── com/nova/notifications/
│           ├── domain/           # Models, results, exceptions, events
│           ├── application/      # Ports, services, async, retry, templates, pub/sub
│           └── infrastructure/   # Channels, providers, validators, config
└── notifyflow-demo/              # Demo application with usage examples
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Sealed `Notification` interface** | Enables exhaustive pattern matching with compile-time safety. Extensibility is at the provider level (Strategy), not the channel level. Adding a new channel requires a new permitted record — an intentional, visible change |
| **`NotificationResult` instead of exceptions** | Enables safe batch processing where one failure doesn't abort others |
| **`EnumMap` for channel routing** | O(1) lookup, type-safe, memory efficient |
| **Separate Provider and Channel** | Channel handles validation + error wrapping; Provider handles external API. Single Responsibility |
| **Builder for configuration** | Fluent API, no reflection, no annotations, compile-time safety |
| **`CopyOnWriteArrayList` for Pub/Sub** | Thread-safe iteration during event publishing without external locks |
| **SLF4J API only in core** | Consumer chooses their logging implementation. No forced dependencies |

## Running Locally

### Prerequisites

- **Java 21+** (verify: `java --version`)
- **Maven 3.9+** (verify: `mvn --version`)

If using SDKMAN:
```bash
sdk install java 21.0.2-tem
sdk install maven
```

### Build & Run

```bash
# 1. Clone the repository
git clone https://github.com/your-user/notifyflow-nova.git
cd notifyflow-nova

# 2. Compile and run tests (87 tests)
mvn clean install

# 3. Run the demo (all modes)
java -jar notifyflow-demo/target/notifyflow-demo.jar all

# Run a specific demo mode
java -jar notifyflow-demo/target/notifyflow-demo.jar sync
java -jar notifyflow-demo/target/notifyflow-demo.jar async
java -jar notifyflow-demo/target/notifyflow-demo.jar batch
java -jar notifyflow-demo/target/notifyflow-demo.jar retry
java -jar notifyflow-demo/target/notifyflow-demo.jar template
java -jar notifyflow-demo/target/notifyflow-demo.jar validation
```

### Useful Maven Commands

```bash
# Compile only
mvn clean compile

# Run tests only
mvn test

# Package without tests
mvn clean package -DskipTests

# Install to local repository (needed for multi-module)
mvn clean install
```

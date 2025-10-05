# Extension Framework

A lightweight and easy-to-use Java extension point framework designed to solve extensibility problems in complex business systems. By defining extension point interfaces and implementing dynamic matching mechanisms, it achieves perfect decoupling between system universal processes and business-specific logic.

📖 English Documentation | [📖 中文文档](README.md)

## ✨ Core Features

- **Lightweight**: Minimal dependencies, focused on core functionality
- **Simple and Easy**: Core API has only a few classes, get started in 5 minutes
- **Progressive Complexity**: From simple to complex, use advanced features as needed
- **Spring Friendly**: Seamless integration with Spring Boot, supports dependency injection

## 🚀 Quick Start

### Maven Dependency

**Spring Boot Project (Recommended):**
```xml
<dependency>
    <groupId>io.github.qoggy</groupId>
    <artifactId>extension-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Pure Java Project:**
```xml
<dependency>
    <groupId>io.github.qoggy</groupId>
    <artifactId>extension-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 5-Minute Quick Example

#### 1. Define Extension Point
```java
// Business extension point interface
interface PaymentProcessor {
    PayResult processPayment(Order order);
}

// Context object
class PaymentContext {
    private String paymentType;
    // getter/setter...
}
```

#### 2. Implement Extension
```java
// Use @Extension annotation to mark extension implementation
@Extension
class AlipayProcessor implements PaymentProcessor, Matcher<PaymentContext>, Priority {
    @Override
    public PayResult processPayment(Order order) {
        // Alipay payment logic
        return new PayResult("alipay", "success");
    }

    @Override
    public boolean match(PaymentContext context) {
        return "alipay".equals(context.getPaymentType());
    }

    @Override
    public int getPriority() {
        return 10;
    }
}

// WeChat implementation
@Extension
class WechatProcessor implements PaymentProcessor, Matcher<PaymentContext> {
    @Override
    public PayResult processPayment(Order order) {
        // WeChat payment logic
        return new PayResult("wechat", "success");
    }

    @Override
    public boolean match(PaymentContext context) {
        return "wechat".equals(context.getPaymentType());
    }
}
```

#### 3. Use Extension Point
```java
@Service
class OrderService {
    @Autowired
    private ExtensionContext extensionContext;
    
    // Auto-inject extension point proxy
    @ExtensionInject
    private PaymentProcessor paymentProcessor;

    public PayResult processOrder(Order order) {
        PaymentContext context = new PaymentContext(order.getPaymentType());
        
        try (var ignored = extensionContext.initScope(context)) {
            // Framework automatically selects matching implementation
            return paymentProcessor.processPayment(order);
        }
    }
}
```

## 🌈 Pure Java Project Usage

### 1. Define Extension Implementation
```java
// Alipay implementation (without @Extension annotation)
class AlipayProcessor implements PaymentProcessor, Matcher<PaymentContext>, Priority {
    @Override
    public PayResult processPayment(Order order) {
        return new PayResult("alipay", "success");
    }

    @Override
    public boolean match(PaymentContext context) {
        return "alipay".equals(context.getPaymentType());
    }

    @Override
    public int getPriority() {
        return 10;
    }
}

// Default implementation
class DefaultPaymentProcessor implements PaymentProcessor, Priority {
    @Override
    public PayResult processPayment(Order order) {
        return new PayResult("default", "failed");
    }

    @Override
    public int getPriority() {
        return Priority.LOWEST_PRECEDENCE;
    }
}
```

### 2. Manual Registration and Usage

**Method 1: Direct Lookup**
```java
class OrderService {
    private static final ExtensionContext extensionContext = new ExtensionContext();
    
    static {
        // Manually register extension implementations
        extensionContext.register(
            new AlipayProcessor(),
            new DefaultPaymentProcessor()
        );
    }

    public PayResult processOrder(Order order) {
        PaymentContext context = new PaymentContext(order.getPaymentType());
        
        try (var scope = extensionContext.initScope(context)) {
            // Automatically select matching implementation
            PaymentProcessor processor = extensionContext.find(PaymentProcessor.class);
            return processor.processPayment(order);
        }
    }
}
```

**Method 2: Proxy Pattern**
```java
class OrderService {
    private static final ExtensionContext extensionContext = new ExtensionContext();
    
    static {
        // Manually register extension implementations
        extensionContext.register(
            new AlipayProcessor(),
            new DefaultPaymentProcessor()
        );
    }
    
    // Create proxy object that automatically routes to matching implementation
    private final PaymentProcessor paymentProcessor = extensionContext.proxy(PaymentProcessor.class);

    public PayResult processOrder(Order order) {
        PaymentContext context = new PaymentContext(order.getPaymentType());
        
        try (var ignored = extensionContext.initScope(context)) {
            // Direct call, framework automatically selects implementation
            return paymentProcessor.processPayment(order);
        }
    }
}
```

## 📖 Core Concepts

### ExtensionContext
Extension point manager responsible for registration, lookup, and context management of extension implementations.

```java
ExtensionContext context = new ExtensionContext();

// Register extension implementations (direct instance registration)
context.register(new AlipayProcessor(), new WechatProcessor());

// Find single implementation (highest priority matching implementation)
PaymentProcessor processor = context.find(PaymentProcessor.class);

// Find all matching implementations (sorted by priority)
List<PaymentProcessor> processors = context.findAll(PaymentProcessor.class);

// Create proxy object
PaymentProcessor proxy = context.proxy(PaymentProcessor.class);

// Manage context scope
try (ExtensionScope scope = context.initScope(contextObject)) {
    // Make extension point calls within this scope
}
```

### Matcher<T>
Matcher interface used to determine whether an extension implementation should be executed.

```java
public interface Matcher<T> {
    boolean match(T context);
}
```

### Priority
Priority interface used to control the execution order of multiple matching implementations.

```java
public interface Priority {
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    
    int getPriority();
}
```

## 🎯 Advanced Features

### Extension Point Reuse
The same extension implementation can serve multiple business scenarios:

```java
class StandardPriceCalculator implements PriceCalculator, Matcher<OrgContext> {
    @Override
    public boolean match(OrgContext context) {
        // Support multiple organizations using the same implementation
        return Arrays.asList("alibaba", "taobao", "tmall").contains(context.getOrgId());
    }

    @Override
    public BigDecimal calculate(Order order) {
        return standardPriceCalculation(order);
    }
}
```

### Default Implementation
Provide fallback implementation for extension points:

```java
@Extension
class DefaultInventoryProcessor implements InventoryProcessor, Priority {
    @Override
    public void processInventory(Order order) {
        // Default processing logic
    }

    @Override
    public int getPriority() {
        return Priority.LOWEST_PRECEDENCE; // Lowest priority, used as fallback
    }
}
```

### Multiple Implementation Execution
Execute all matching extension implementations:

```java
// Get all matching notification senders
List<NotificationSender> senders = extensionContext.findAll(NotificationSender.class);

// Execute notification sending one by one
for (NotificationSender sender : senders) {
    try {
        sender.sendNotification(message);
    } catch (Exception e) {
        // Handle exceptions from individual senders without affecting others
        logger.warn("Failed to send notification via " + sender.getClass().getSimpleName(), e);
    }
}
```
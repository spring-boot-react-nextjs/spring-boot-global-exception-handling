# Global Exception Handling with Spring Boot
<small>Extended with i18n internationalization</small>

In modern web development, robust error handling is crucial for ensuring application stability and delivering a seamless user experience.<br/>
Within the <strong>Spring Boot</strong> framework, which is widely used for building enterprise-level Java applications, <strong>global exception handling</strong> plays a pivotal role in managing errors effectively.<br/>
In this article, we will delve into the significance of global exception handling, explore its implementation within Spring Boot, and provide a basic example to demonstrate its usage.

<b>Author:</b> <a href="https://github.com/spring-boot-react-nextjs" target="_blank">spring-boot-react-nextjs</a><br>
<b>Created:</b> 2024-07-07<br>
<b>Last updated:</b> 2024-08-04

[![](https://img.shields.io/badge/Spring%20Boot-8A2BE2)]() [![](https://img.shields.io/badge/release-Jul%2018,%202024-blue)]() [![](https://img.shields.io/badge/version-3.3.2-blue)]()

## 1. Why Global Exception Handling Matters?
1. <b>Centralized Error Handling:</b> <strong>Global exception handling</strong> allows developers to centralize error-handling logic, making it easier to manage and maintain.<br/>
Instead of scattering try-catch blocks throughout the codebase, developers can define a single location to handle exceptions.
<br/><br/>
2. <b>Consistent Response Formats:</b> Implementing <strong>global exception handling</strong> ensures a consistent format for error responses across the application.<br/>
This consistency enhances the user experience by providing clear and standardized error messages, making it easier for clients to understand and handle errors gracefully.
<br/><br/>
3. <b>Improved Debugging and Logging:</b> By centralizing error handling, developers gain better visibility into application errors.<br/>
<strong>Global exception handling</strong> enables comprehensive logging of exceptions, including detailed information such as stack traces, request parameters, and headers, facilitating easier debugging and troubleshooting.
<br/><br/>
4. <b>Enhanced Security:</b> Proper error handling is crucial for security, as it helps prevent information leakage and protects against potential vulnerabilities.<br/>
With <strong>global exception handling</strong>, developers can ensure that sensitive information is not exposed in error responses, thereby bolstering the application's security posture.

## 2. How a Global Exception Handling Implementation Works in Spring Boot?

With the provided example and guidelines, you can effectively implement <strong>global exception handling</strong> in your <strong>Spring Boot</strong> projects, contributing to the overall quality and stability of the software systems.

### 2.1 Create a Spring Boot Application
For this basic example we will start with a simple REST endpoint which we can call to see the i18n implementation at work.
Let's create an application using the dependencies as previewed:

![01-start-spring-io](https://github.com/spring-boot-react-nextjs/spring-boot-global-exception-handling/blob/main/images/01-start-spring-io.png)

[![](https://img.shields.io/badge/Lombok-8A2BE2)]()
Because it is just that easy to use.
Want to know more about <b>Project Lombok</b>? [Click this link](https://projectlombok.org/features/)

[![](https://img.shields.io/badge/Spring%20Web-8A2BE2)]()
This Spring Framework dependency will provide us with all the necessary functionality to create and manage our REST endpoints.

### 2.2 Configure Global Exception Handling

Now that the <strong>Spring Boot</strong> application is created, it is time to add configurations for the Global Exception Handling to work.
Within your `src/main/resources/application.yml` file, add the following configuration:

```yml
spring:
  application:
    name: exception
    error-uri: /error
```

*If you are using a application.properties file, the configuration is as follows:*
```properties
spring.application.name=exception
spring.application.error-uri=/error
```

The `error-uri` is the URI which you would like to show in your JSON error responses.<br/>
For this specific example we will just use `/error`

### 2.3 Create an Exception

Let's create an Exception we can use in our application.

`ResourceNotFoundException.java`
```java
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

`ResourceNotFoundException` extends the `RuntimeException` class.<br/>
By extending <b>RuntimeException</b> rather than <b>Exception</b>, ResourceNotFoundException becomes an unchecked exception, which means it does not need to be declared in method signatures or caught explicitly by callers. This simplifies error handling within the application.

### 2.4 Create the Global Exception Handling Class

1. <b>Create a Custom Exception Handler:</b> In <strong>Spring Boot, global exception handling</strong> is achieved by creating a class annotated with `@ControllerAdvice` or `@RestControllerAdvice`.<br/>
This class contains methods annotated with `@ExceptionHandler` to handle specific exceptions or groups of exceptions.

2. <b>Define Exception Handling Logic:</b> Within the exception handler methods, you can define the logic to handle various types of exceptions.<br/>
This may include returning customized error responses, logging exception details, and performing additional actions such as notifying administrators or rolling back transactions.

3. <b>Handle Different Types of Exceptions:</b> <strong>Global exception handling</strong> allows you to handle different types of exceptions, including application-specific exceptions, system errors, and client errors such as HTTP 400 and 500 status codes.

`GlobalExceptionHandler.java`
```java
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.application.error-uri}")
    private String errorUri;

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return getProblemDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    private ProblemDetail getProblemDetail(HttpStatus httpStatus, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                httpStatus,
                detail
        );
        pd.setType(URI.create(errorUri));
        return pd;
    }
}
```

`@ControllerAdvice` is defines that this class will be called whenever an exception is thrown in your application.<br/>
`@RequiredArgsConstructor` is a <b>Lombok</b> annotation, creating the necessary class constructor upon application compilation.

### 2.5 Using the `ResourceNotFoundException` Class

Create a `User` entity, `UserController` and a `UserService`.

`User.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String email;
}
```

<br/>

`UserController.java`
```java
@RestController
@RequestMapping(value = "/api/v1/users")
public record UserController(UserService userService) {

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }
}
```

<br/>

`UserService.java`
```java
@Slf4j
@Service
public record UserService(I18nService i18nService) {

    public List<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        users.add(User.builder()
                .email("john@test.com")
                .username("john-doe")
                .build()
        );
        users.add(User.builder()
                .email("jane@test.com")
                .username("jane-doe")
                .build()
        );

        return users;
    }

    public User getUserByUsername(String username) {
        Optional<User> user = this.findUsernameInUserList(username);
        if (user.isEmpty()) {
            log.error(i18nService.getLogMessage("user.not.found.log"), username);
//            throw new ResourceNotFoundException(String.format("User with username %s not found!", username));
            throw new ResourceNotFoundExceptionWithI18n("user.not.found", username);
        }
        return user.get();
    }

    private Optional<User> findUsernameInUserList(String username) {
        List<User> allUsers = this.getAllUsers();
        return allUsers.stream()
                .filter(user -> username.equals(user.getUsername()))
                .findAny();
    }
}
```

`@RestController`, `@Service`: Spring Boot, by default, scans for classes annotated with stereotypes such as `@RestController`, `@Service`, `@Repository`, etc., during the application startup. When it encounters a class annotated with `@Service`, it registers the class as a Spring bean.

`ResourceNotFoundException`: is thrown when a requested resource (in this case, a user with a specific username) is not found. It is used to handle situations where a user is requested but does not exist in the system.

## 3 Spring Boot Global Exception Handling In Action

- To test the <b>REST endpoints</b>, a tool like <b>Postman</b> can be used to send <b>HTTP GET requests</b>.
- A Postman collection is added within the repository `src/main/resources/postman/collection-to-import.json`
- When requesting a user with an incorrect `username`, the exception is thrown.

[![](https://img.shields.io/badge/GET-green)]()<br/>
<small>Endpoint:</small> `http://localhost:8081/api/v1/users`<br/>
<small>Returns:</small> All users in JSON format.

![02-postman-get-all-users](https://github.com/spring-boot-react-nextjs/spring-boot-global-exception-handling/blob/main/images/02-postman-get-all-users.png)
<br><br>

[![](https://img.shields.io/badge/GET-green)]()<br/>
<small>Endpoint:</small> `http://localhost:8081/api/v1/users/{username}`
<small>Returns:</small> The requested user by username in JSON format.

![03-postman-get-by-username](https://github.com/spring-boot-react-nextjs/spring-boot-global-exception-handling/blob/main/images/03-postman-get-by-username.png)

## 4 Spring Boot Global Exception Handling With i18n Internationalization

This repository is extended with the <b>i18n Internationalization</b> functionality.<br/>
With the implementation of the `I18nService`, it is now possible to provide a `Accept-Language` header in your REST calls.<br/>
The language tag provided will ensure the correct translation of the message.<br/>

See the <b>[spring-boot-i18n-internationalization](https://github.com/spring-boot-react-nextjs/spring-boot-i18n-internationalization)</b> repository for more details about the i18n Internationalization implementation.

## Let's Stay Connected

If you have any questions in regard to this repository and/or documentation, please do reach out.

Don't forget to:
- <b>Star</b> the [repository](https://github.com/spring-boot-react-nextjs/spring-boot-global-exception-handling)
- [Follow me](https://github.com/spring-boot-react-nextjs) for more interesting repositories!
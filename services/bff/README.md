# Optimize Serverless Apps In Google Cloud - BFF Service

### Create a Spring Boot Application

```
# Note: subject to change!
git clone git@github.com:ddobrin/optimize-serverless-google-cloud-java.git

# Note: subject to change!
cd prod/bff
```

### Validate that you have Java 17 and Maven installed
```shell
java -version

./mvnw --version
```

### Validate that the starter app is good to go
```
./mvnw clean package spring-boot:run
```

From a terminal window, test the app
```
curl localhost:8080

# Output
Hello from your local environment!
```

### Build a JVM and Native Java application image
```
./mvnw clean package 

./mvnw clean package -Pnative -DskipTests
```

### Build a JVM and Native Java Docker Image
```
./mvnw clean package -Pjvm-image -DskipTests

./mvnw clean package -Pnative-image -DskipTests
```
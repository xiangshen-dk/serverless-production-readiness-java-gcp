# Optimize Serverless Apps In Google Cloud - Faulty Service

### Create a Spring Boot Application

```
# Note: subject to change!
git clone git@github.com:ddobrin/optimize-serverless-google-cloud-java.git

# Note: subject to change!
cd prod/faulty-service
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
curl localhost:8087
```

### Build a JVM and Native Java application image
```
./mvnw clean package 

./mvnw native:compile -Pnative
```

### Build a JVM and Native Java Docker Image
```
./mvnw spring-boot:build-image -Pjit

./mvnw spring-boot:build-image -Pnative
```
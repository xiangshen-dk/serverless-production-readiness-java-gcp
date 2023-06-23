# Optimize Serverless Apps In Google Cloud - Quotes Service

### Create a Spring Boot Application

```
# Note: subject to change!
git clone git@github.com:ddobrin/optimize-serverless-google-cloud-java.git

# Note: subject to change!
cd prod/quotes
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
./mvnw package -DskipTests 

./mvnw native:compile -Pnative -DskipTests
```

### Build a JVM and Native Java application tests
```
./mvnw verify

 ./mvnw -PnativeTest test
```

### Start your app with AOT enabled
```shell
java -Dspring.aot.enabled -jar target/quotes-1.0.0.jar
```
### Build a Docker image with Dockerfiles
```shell
# build an image with jlink
docker build . -f ./containerize/Dockerfile -t quotes-jlink

# build an image with a fat JAR
docker build -f ./containerize/Dockerfile-fatjar -t quotes-fatjar .

# build an image with custom layers
docker build -f ./containerize/Dockerfile-custom -t quotes-custom .
```
### Build a JIT and Native Java Docker Image with Buildpacks
```
./mvnw spring-boot:build-image -Pjit

./mvnw spring-boot:build-image -Pnative
```

### Build, test with CloudBuild in Cloud Build
```shell
gcloud builds submit  --machine-type E2-HIGHCPU-32

gcloud builds submit  --machine-type E2-HIGHCPU-32 --config cloudbuild-native.yaml
```

### Deploy Docker images to Cloud Run

Check existing deployed Cloud Run Services
```shell
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
echo   $PROJECT_ID

gcloud run services list
```

Deploy the Quotes JIT image
```shell
gcloud run deploy quotes \
     --image gcr.io/${PROJECT_ID}/quotes \
     --region us-central1 \
     --memory 2Gi --allow-unauthenticated
```

Deploy the Quotes Native Java image
```shell
gcloud run deploy quotes-native \
     --image gcr.io/${PROJECT_ID}/quotes-native \
     --region us-central1 \
     --memory 2Gi --allow-unauthenticated
```

Test the application in Cloud Run
```shell
TOKEN=$(gcloud auth print-identity-token)

# Test JIT image
http -A bearer -a $TOKEN  https://quotes-ndn7ymldhq-uc.a.run.app/random-quote
http -A bearer -a $TOKEN  https://quotes-ndn7ymldhq-uc.a.run.app/quotes

# Test Native Java image
http -A bearer -a $TOKEN https://quotes-native-ndn7ymldhq-uc.a.run.app/random-quote
http -A bearer -a $TOKEN https://quotes-native-ndn7ymldhq-uc.a.run.app/quotes
```
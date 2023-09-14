## Install CDKTF

* The [Terraform CLI](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli) (1.2+).
* [Node.js](https://nodejs.org/en) and npm v16+.
* Java [OpenJDK v17](https://openjdk.java.net/) and [Gradle](https://gradle.org/install/)

__Note:__ The latest CDKTF for Java is using Gradle instead Maven, which provides faster synthesizing.


For more details, read the [Install CDKTF tutorial](https://developer.hashicorp.com/terraform/tutorials/cdktf/cdktf-install).

## Build the container images

The images need to be built and stored in a registry. For this application, you can build different types of images. The following commands provide an example:

```bash
# Assuming from the root directory of the project
ROOT_DIR=$(pwd)

cd $ROOT_DIR/services/audit
gcloud builds submit ..  --machine-type E2-HIGHCPU-32 --config cloudbuild.yaml 


cd $ROOT_DIR/services/faulty
gcloud builds submit ..  --machine-type E2-HIGHCPU-32 --config cloudbuild.yaml --substitutions=_TYPE=jit

cd $ROOT_DIR/services/bff
gcloud builds submit ..  --machine-type E2-HIGHCPU-32 --config cloudbuild.yaml --substitutions=_TYPE=jit

cd $ROOT_DIR/services/reference
gcloud builds submit ..  --machine-type E2-HIGHCPU-32 --config cloudbuild.yaml --substitutions=_TYPE=jit

cd $ROOT_DIR/services/quotes
gcloud builds submit ..  --machine-type E2-HIGHCPU-32 --config cloudbuild.yaml
```

## Use CDKTF to deploy the infrastructure and the app

You can use different backends to manage the Terraform state. Here is an example using the GCS backend.

```bash
export PROJECT_ID=[Use your GCP project Id here]
export GCS_BACKEND_BUCKET_NAME=${PROJECT_ID}-cdktf-state
gcloud storage buckets create gs://${GCS_BACKEND_BUCKET_NAME}
```

Use the CDKTF CLI to deploy. Notice you can pass in various parameters for the deployment.
```bash
cd $ROOT_DIR/iac
cdktf deploy application-dev --var='referenceImageName=reference-jit' --var='bffImageName=bff-jit' --var='faultyImageName=faulty-jit' --auto-approve
```

Wait for a few minutes and check the results.

## Destroy

To destroy the deployment, you can run the following commands:
```bash
export PROJECT_ID=[Use your GCP project Id here]
export GCS_BACKEND_BUCKET_NAME=${PROJECT_ID}-cdktf-state
cdktf destroy application-dev --auto-approve
```
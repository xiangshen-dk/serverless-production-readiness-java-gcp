package com.mycompany.app.reference;

import imports.google.cloud_run_service_iam_policy.CloudRunServiceIamPolicy;

import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.TerraformOutputConfig;
import imports.google.cloud_run_service.CloudRunService;
import imports.google.cloud_run_service.CloudRunServiceConfig;
import imports.google.cloud_run_service.CloudRunServiceTemplate;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpec;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpecContainers;
import imports.google.cloud_run_service_iam_policy.CloudRunServiceIamPolicyConfig;
import imports.google.data_google_iam_policy.DataGoogleIamPolicy;
import imports.google.data_google_iam_policy.DataGoogleIamPolicyBinding;
import imports.google.data_google_iam_policy.DataGoogleIamPolicyConfig;
import software.constructs.Construct;

import java.util.List;

public class Reference extends Construct {

    static String imagename = "gcr.io/cloudrun/hello";

    public Reference(Construct scope, String id, String project) {
        super(scope, id);

        CloudRunService cr = CloudRunService.Builder.create(this, "reference-cr-service")
                .name("reference-service").project(project).location("us-east1")
                .template(
                        CloudRunServiceTemplate
                                .builder().spec(
                                        CloudRunServiceTemplateSpec.builder()
                                                .containers(List.of(CloudRunServiceTemplateSpecContainers.builder()
                                                        .image(imagename).build()))
                                                .build())
                                .build())
                .build();

        DataGoogleIamPolicy crPolicy = DataGoogleIamPolicy.Builder.create(this, "datanoauth").binding(
                List.of(DataGoogleIamPolicyBinding.builder().role("roles/run.invoker")
                        .members(List.of("allUsers"))
                        .build()))
                .build();

        CloudRunServiceIamPolicy.Builder.create(this, "reference-policy")
                .location("us-east1")
                .project(project).service(cr.getName())
                .policyData(crPolicy.getPolicyData()).build();

        TerraformOutput.Builder.create(this, "reference-service-url")
                .value(cr.getStatus().get(0).getUrl()).build();
    }
}

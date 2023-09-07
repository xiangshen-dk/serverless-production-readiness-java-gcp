package com.mycompany.app.faulty;

import java.util.List;
import java.util.Map;

import com.hashicorp.cdktf.TerraformOutput;

import imports.google.cloud_run_v2_service.CloudRunV2Service;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplate;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainers;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersResources;
import software.constructs.Construct;

public class FaultyService extends Construct {

    private String svcUrl;

    public String getSvcUrl() {
        return this.svcUrl;
    }

    public FaultyService(Construct scope, String id, String project, String region) {
        super(scope, id);

        String imageName = "gcr.io/" + project + "/faulty-jit";

        CloudRunV2Service cr = CloudRunV2Service.Builder.create(this, "faulty-cr-service")
                .name("faulty-service").project(project).location(region)
                .template(CloudRunV2ServiceTemplate.builder()
                        .containers(List.of(CloudRunV2ServiceTemplateContainers.builder()
                                .image(imageName)
                                .resources(CloudRunV2ServiceTemplateContainersResources.builder()
                                        .limits(Map.of("memory", "2Gi")).build())
                                .build()))
                        .build())
                .build();

        this.svcUrl = cr.getUri();
        TerraformOutput.Builder.create(this, "faulty-service-url").value(svcUrl).build();
    }
}

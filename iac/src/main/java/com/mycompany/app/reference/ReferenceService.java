package com.mycompany.app.reference;

import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2Service;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplate;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainers;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersResources;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class ReferenceService extends Construct {

    private String svcUrl;

    public String getSvcUrl() {
        return this.svcUrl;
    }

    public ReferenceService(Construct scope, String id, String project, String region, String imagename) {
        super(scope, id);
        
        CloudRunV2Service cr = CloudRunV2Service.Builder.create(this, "reference-cr-service")
                .name("reference-service").project(project).location(region)
                .template(CloudRunV2ServiceTemplate.builder()
                        .containers(List.of(CloudRunV2ServiceTemplateContainers.builder()
                                .image(imagename)
                                .resources(CloudRunV2ServiceTemplateContainersResources.builder()
                                        .limits(Map.of("memory", "2Gi")).build())
                                .build()))
                        .build())
                .build();


        this.svcUrl = cr.getUri();
        TerraformOutput.Builder.create(this, "reference-service-url").value(svcUrl).build();
    }
}

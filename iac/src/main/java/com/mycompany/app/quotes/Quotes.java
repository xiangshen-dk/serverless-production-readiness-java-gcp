package com.mycompany.app.quotes;

import java.util.List;
import java.util.Map;

import com.hashicorp.cdktf.TerraformOutput;

import imports.google.cloud_run_service.CloudRunService;
import imports.google.cloud_run_service.CloudRunServiceTemplate;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpec;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpecContainers;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpecContainersEnv;
import imports.google.cloud_run_service.CloudRunServiceTemplateSpecContainersResources;
import imports.google.cloud_run_service_iam_policy.CloudRunServiceIamPolicy;
import imports.google.data_google_iam_policy.DataGoogleIamPolicy;
import imports.google.data_google_iam_policy.DataGoogleIamPolicyBinding;
import imports.google.sql_database.SqlDatabase;
import imports.google.sql_database_instance.SqlDatabaseInstance;
import imports.google.sql_database_instance.SqlDatabaseInstanceSettings;
import imports.google.sql_user.SqlUser;
import software.constructs.Construct;

public class Quotes extends Construct {

    static String imageName = "gcr.io/shenxiang-gcp-solution/quotes";

    public Quotes(Construct scope, String id, String project) {
        super(scope, id);

        SqlDatabaseInstance sqlDBinstnace = SqlDatabaseInstance.Builder.create(this, "quotesDBinstance")
                .name("serverless-db-instance")
                .region("us-east1").databaseVersion("POSTGRES_14")
                .settings(SqlDatabaseInstanceSettings.builder().tier("db-f1-micro").build())
                .build();

       SqlDatabase.Builder.create(this, "quotesDb")
                .name("quote_db").instance(sqlDBinstnace.getName()).build();

        SqlUser.Builder.create(this, "sqlUser").name("user").password("password").instance(sqlDBinstnace.getName());

        CloudRunServiceTemplateSpecContainersEnv envDbHost = CloudRunServiceTemplateSpecContainersEnv.builder()
                .name("DB_HOST").value(sqlDBinstnace.getFirstIpAddress()).build();

        CloudRunService cr = CloudRunService.Builder.create(this, "quotes-cr-service")
                .name("quotes-service").project(project).location("us-east1").template(
                        CloudRunServiceTemplate.builder().spec(
                                CloudRunServiceTemplateSpec.builder()
                                        .containers(List.of(CloudRunServiceTemplateSpecContainers.builder()
                                                .env(List.of(envDbHost))
                                                .image(imageName)
                                                .resources(CloudRunServiceTemplateSpecContainersResources.builder()
                                                        .limits(Map.of("memory", "2Gi")).build())
                                                .build()))
                                        .build())
                                .build())
                .build();

        DataGoogleIamPolicy crPolicy = DataGoogleIamPolicy.Builder.create(this, "datanoauth")
                .binding(
                        List.of(DataGoogleIamPolicyBinding.builder().role("roles/run.invoker")
                                .members(List.of("allUsers"))
                                .build()))
                .build();

        CloudRunServiceIamPolicy.Builder.create(this, "quotes-policy")
                .location("us-east1")
                .project(project).service(cr.getName())
                .policyData(crPolicy.getPolicyData()).build();

        TerraformOutput.Builder.create(this, "quotes-service-url")
                .value(cr.getStatus().get(0).getUrl()).build();
    }

    // public static void syncCreateBuild() throws Exception {
    // // This snippet has been automatically generated and should be regarded as a
    // code template only.
    // // It will require modifications to work:
    // // - It may require correct/in-range values for request initialization.
    // // - It may require specifying regional endpoints when creating the service
    // client as shown in
    // //
    // https://cloud.google.com/java/docs/setup#configure_endpoints_for_the_client_library
    // try (CloudBuildClient cloudBuildClient = CloudBuildClient.create()) {
    // CreateBuildRequest request =
    // CreateBuildRequest.newBuilder()
    // .setParent(LocationName.of("shenxiang-gcp-solution", "global").toString())
    // .setProjectId("shenxiang-gcp-solution")
    // .setBuild(Build.newBuilder().setSource().build())
    // .build();
    // Build response = cloudBuildClient.createBuildAsync(request).get();
    // System.out.println(response.getStatus().name());
    // System.out.println(response.getStatusDetail());
    // }
    // }
}

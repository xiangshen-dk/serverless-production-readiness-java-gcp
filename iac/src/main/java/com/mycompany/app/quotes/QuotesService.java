package com.mycompany.app.quotes;

import java.util.List;
import java.util.Map;

import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.TerraformVariable;
import com.hashicorp.cdktf.TerraformVariableConfig;
import com.hashicorp.cdktf.providers.google.data_google_project.DataGoogleProject;
import com.hashicorp.cdktf.providers.random_provider.password.Password;
import com.hashicorp.cdktf.providers.google.secret_manager_secret_iam_member.SecretManagerSecretIamMember;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2Service;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplate;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainers;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnv;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnvValueSource;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnvValueSourceSecretKeyRef;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersResources;
import com.hashicorp.cdktf.providers.google.sql_database.SqlDatabase;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstance;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstanceSettings;
import com.hashicorp.cdktf.providers.google.sql_user.SqlUser;
import com.hashicorp.cdktf.providers.google.project_service.ProjectService;
import com.hashicorp.cdktf.providers.google.secret_manager_secret.SecretManagerSecret;
import com.hashicorp.cdktf.providers.google.secret_manager_secret.SecretManagerSecretReplication;
import com.hashicorp.cdktf.providers.google.secret_manager_secret_version.SecretManagerSecretVersion;
import software.constructs.Construct;

public class QuotesService extends Construct {

    private String svcUrl;

    public String getSvcUrl() {
        return this.svcUrl;
    }

    public QuotesService(Construct scope, String id, String project, String region,
            String imageName) {
        super(scope, id);

        // Initialize the parameters
        TerraformVariable dbInstnaceSize = new TerraformVariable(this, "dbInstnaceSize",
                TerraformVariableConfig.builder().type("string").defaultValue("db-f1-micro")
                        .description("Cloud SQL instnace type").build());
        TerraformVariable databaseVersion = new TerraformVariable(this, "databaseVersion",
                TerraformVariableConfig.builder().type("string").defaultValue("POSTGRES_15")
                        .description("Cloud SQL PostGres version").build());
        TerraformVariable dbUser = new TerraformVariable(this, "dbUser",
                TerraformVariableConfig.builder().type("string").defaultValue("user")
                        .description("Cloud SQL DB user").build());
        TerraformVariable dbName = new TerraformVariable(this, "dbName",
                TerraformVariableConfig.builder().type("string").defaultValue("quote_db")
                        .description("Cloud SQL DB name").build());

        ProjectService sqlAdminService =
                ProjectService.Builder.create(this, "enableSqlAdminService").disableOnDestroy(false)
                        .project(project).service("sqladmin.googleapis.com").build();
        SqlDatabaseInstance sqlDBinstnace = SqlDatabaseInstance.Builder
                .create(this, "quotesDBinstance").name("serverless-db-instance").region(region)
                .databaseVersion(databaseVersion.getStringValue()).deletionProtection(false)
                .settings(SqlDatabaseInstanceSettings.builder()
                        .tier(dbInstnaceSize.getStringValue()).build())
                .dependsOn(List.of(sqlAdminService)).build();

        // Create the DB instance
        SqlDatabase.Builder.create(this, "quotesDb").name(dbName.getStringValue())
                .instance(sqlDBinstnace.getName()).build();

        // Create the DB password and store it in the secret manager
        Password pw = Password.Builder.create(this, "random-pw").length(12).build();

        SecretManagerSecret dbSecret = SecretManagerSecret.Builder.create(this, "db-secret")
                .secretId("db-secret")
                .replication(
                        SecretManagerSecretReplication.builder().automatic(true).build())
                .build();
        SecretManagerSecretVersion.Builder.create(this, "db-pass").secret(dbSecret.getId())
                .secretData(pw.getResult()).build();

        // Create the SQL user
        SqlUser.Builder.create(this, "sqlUser").name(dbUser.getStringValue())
                .password(pw.getResult()).instance(sqlDBinstnace.getName());

        // Set the environment variables for the Cloud Run service
        CloudRunV2ServiceTemplateContainersEnv envDbHost = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_HOST").value(sqlDBinstnace.getFirstIpAddress()).build();
        CloudRunV2ServiceTemplateContainersEnv envDbName = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_DATABASE").value(dbName.getStringValue()).build();
        CloudRunV2ServiceTemplateContainersEnv envDbUser = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_USERT").value(dbUser.getStringValue()).build();
        CloudRunV2ServiceTemplateContainersEnv envDbPasswd = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_PASS")
                .valueSource(CloudRunV2ServiceTemplateContainersEnvValueSource.builder()
                        .secretKeyRef(CloudRunV2ServiceTemplateContainersEnvValueSourceSecretKeyRef
                                .builder().secret(dbSecret.getSecretId()).version("latest").build())
                        .build())
                .build();

        DataGoogleProject deployProject =
                DataGoogleProject.Builder.create(this, "project").projectId(project).build();

        SecretManagerSecretIamMember.Builder
                .create(this, "secret-access").secretId(dbSecret.getSecretId())
                .role("roles/secretmanager.secretAccessor").member("serviceAccount:"
                        + deployProject.getNumber() + "-compute@developer.gserviceaccount.com")
                .build();
        // Deploy the Cloud Run service
        CloudRunV2Service cr =
                CloudRunV2Service.Builder.create(this, "quotes-cr-service").name("quotes-service")
                        .project(project).location(region)
                        .template(CloudRunV2ServiceTemplate.builder()
                                .containers(List.of(CloudRunV2ServiceTemplateContainers.builder()
                                        .image(imageName)
                                        .resources(CloudRunV2ServiceTemplateContainersResources
                                                .builder().limits(Map.of("memory", "2Gi")).build())
                                        .env(List.of(envDbHost, envDbName, envDbUser, envDbPasswd))
                                        .build()))
                                .build())
                        .build();

        this.svcUrl = cr.getUri();
        TerraformOutput.Builder.create(this, "quotes-service-url").value(svcUrl).build();
    }
}

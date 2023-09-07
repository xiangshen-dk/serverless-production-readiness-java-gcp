package com.mycompany.app.quotes;

import java.util.List;
import java.util.Map;

import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.google.data_google_project.DataGoogleProject;
import imports.google.secret_manager_secret_iam_member.SecretManagerSecretIamMember;
import imports.google.cloud_run_v2_service.CloudRunV2Service;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplate;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainers;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnv;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnvValueSource;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnvValueSourceSecretKeyRef;
import imports.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersResources;
import imports.google.sql_database.SqlDatabase;
import imports.google.sql_database_instance.SqlDatabaseInstance;
import imports.google.sql_database_instance.SqlDatabaseInstanceSettings;
import imports.google.sql_user.SqlUser;
import imports.google.project_service.ProjectService;
import imports.google.secret_manager_secret.SecretManagerSecret;
import imports.google.secret_manager_secret.SecretManagerSecretReplication;
import imports.google.secret_manager_secret_version.SecretManagerSecretVersion;
import imports.random.password.Password;
import software.constructs.Construct;

public class QuotesService extends Construct {

    static String dbInstnaceSize = "db-f1-micro";
    static String databaseVersion = "POSTGRES_15";
    static String dbUser = "user";
    static String dbName = "quote_db";
    private String svcUrl;

    public String getSvcUrl() {
        return this.svcUrl;
    }

    public QuotesService(Construct scope, String id, String project, String region) {
        super(scope, id);

        String imageName = "gcr.io/" + project + "/quotes";

        ProjectService sqlAdminService =
                ProjectService.Builder.create(this, "enableSqlAdminService").disableOnDestroy(false)
                        .project(project).service("sqladmin.googleapis.com").build();
        SqlDatabaseInstance sqlDBinstnace = SqlDatabaseInstance.Builder
                .create(this, "quotesDBinstance").name("serverless-db-instance").region(region)
                .databaseVersion(databaseVersion).deletionProtection(Boolean.FALSE)
                .settings(SqlDatabaseInstanceSettings.builder().tier(dbInstnaceSize).build())
                .dependsOn(List.of(sqlAdminService)).build();

        SqlDatabase.Builder.create(this, "quotesDb").name(dbName).instance(sqlDBinstnace.getName())
                .build();

        Password pw = Password.Builder.create(this, "random-pw").length(12).build();

        SecretManagerSecret dbSecret = SecretManagerSecret.Builder.create(this, "db-secret")
                .secretId("db-secret")
                .replication(
                        SecretManagerSecretReplication.builder().automatic(Boolean.TRUE).build())
                .build();
        SecretManagerSecretVersion.Builder.create(this, "db-pass").secret(dbSecret.getId())
                .secretData(pw.getResult()).build();

        SqlUser.Builder.create(this, "sqlUser").name(dbUser).password(pw.getResult())
                .instance(sqlDBinstnace.getName());

        CloudRunV2ServiceTemplateContainersEnv envDbHost = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_HOST").value(sqlDBinstnace.getFirstIpAddress()).build();
        CloudRunV2ServiceTemplateContainersEnv envDbName = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_DATABASE").value(dbName).build();
        CloudRunV2ServiceTemplateContainersEnv envDbUser = CloudRunV2ServiceTemplateContainersEnv
                .builder().name("DB_USERT").value(dbUser).build();
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

package com.mycompany.app;

import com.hashicorp.cdktf.providers.google.compute_project_default_network_tier.ComputeProjectDefaultNetworkTier;
import com.hashicorp.cdktf.providers.google.compute_project_default_network_tier.ComputeProjectDefaultNetworkTierConfig;
import com.mycompany.app.quotes.Quotes;
import com.mycompany.app.reference.Reference;
import com.hashicorp.cdktf.*;
import software.constructs.Construct;

import com.hashicorp.cdktf.providers.google.provider.GoogleProvider;
import com.hashicorp.cdktf.providers.google.provider.GoogleProviderConfig;
import com.hashicorp.cdktf.providers.local.provider.LocalProvider;

public class Main {

    public static class ReferenceStack extends TerraformStack {

        public ReferenceStack(Construct scope, String name, String environment, String project) {
            super(scope, name);

            new GoogleProvider(this, "google-cloud", GoogleProviderConfig.builder()
                    .region("us-east1")
                    .project(project)
                    .build());

            // new ComputeProjectDefaultNetworkTier(this, "network-tier",
            // ComputeProjectDefaultNetworkTierConfig.builder()
            // .project(project)
            // .networkTier("PREMIUM")
            // .build()
            // );

            // new LocalProvider(this, "local");

            new Reference(this, "reference-" + environment, project);
            new Quotes(this, "quotes-" + environment, project);

        }
    }

    // public static class PostsStack extends TerraformStack{

    // private String httpsTriggerUrl;

    // public PostsStack(Construct scope, String name, String environment, String
    // user, String project){
    // super(scope, name);

    // new GoogleProvider(this, "google-cloud", GoogleProviderConfig.builder()
    // .region("us-east1")
    // .project(project)
    // .build()
    // );

    // TerraformVariable dbPass = new TerraformVariable(this, "DB_PASS",
    // TerraformVariableConfig.builder()
    // .type("string")
    // .sensitive(true)
    // .description("The password for the database")
    // .build()
    // );

    // Posts posts = new Posts(this, "posts-" + environment + "-" + user,
    // environment, user, project, dbPass.getStringValue());

    // this.httpsTriggerUrl = posts.getHttpsTriggerUrl();
    // }

    // public String getHttpsTriggerUrl(){
    // return this.httpsTriggerUrl;
    // }
    // }

    public static void main(String[] args) {
        final App app = new App();

        Boolean USE_REMOTE_BACKEND = (System.getenv("USE_REMOTE_BACKEND") == "true");

        // DEV
        // PostsStack postsDev = new PostsStack(app, "posts-dev", "development",
        // System.getenv("CDKTF_USER"), System.getenv("PROJECT_ID"));
        // if (USE_REMOTE_BACKEND) {
        // new RemoteBackend(postsDev, RemoteBackendConfig.builder()
        // .organization("terraform-demo-mad")
        // .workspaces(new
        // NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
        // .build()
        // );
        // }
        ReferenceStack referenceDev = new ReferenceStack(app, "reference-dev", "development",
                System.getenv("PROJECT_ID"));
        if (USE_REMOTE_BACKEND) {
            new RemoteBackend(referenceDev, RemoteBackendConfig.builder()
                    .organization("terraform-demo-mad")
                    .workspaces(new NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
                    .build());
        }

        // Prod
        // PostsStack postsProd = new PostsStack(app, "posts-prod", "production",
        // System.getenv("CDKTF_USER"), System.getenv("PROJECT_ID"));
        // if (USE_REMOTE_BACKEND) {
        // new RemoteBackend(postsProd, RemoteBackendConfig.builder()
        // .organization("terraform-demo-mad")
        // .workspaces(new
        // NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
        // .build()
        // );
        // }
        ReferenceStack referenceProd = new ReferenceStack(app, "reference-prod", "production",
                System.getenv("PROJECT_ID"));
        if (USE_REMOTE_BACKEND) {
            new RemoteBackend(referenceProd, RemoteBackendConfig.builder()
                    .organization("terraform-demo-mad")
                    .workspaces(new NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
                    .build());
        }

        app.synth();
    }
}
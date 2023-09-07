package com.mycompany.app;

import com.hashicorp.cdktf.*;

public class Main {
    public static void main(String[] args) {
        final App app = new App();

        Boolean USE_REMOTE_BACKEND = (System.getenv("USE_REMOTE_BACKEND") == "true");

        String region = "us-central1";

        ApplicationStack devStack = new ApplicationStack(app, "application-dev", "development",
                System.getenv("PROJECT_ID"), region);
        if (USE_REMOTE_BACKEND) {
            new RemoteBackend(devStack,
                    RemoteBackendConfig.builder().organization("terraform-demo-mad").workspaces(
                            new NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
                            .build());
        }

        ApplicationStack prodStack = new ApplicationStack(app, "application-prod", "production",
                System.getenv("PROJECT_ID"), region);
        if (USE_REMOTE_BACKEND) {
            new RemoteBackend(prodStack,
                    RemoteBackendConfig.builder().organization("terraform-demo-mad").workspaces(
                            new NamedRemoteWorkspace("cdktf-integration-serverless-java-example"))
                            .build());
        }

        app.synth();
    }
}

package com.mycompany.app;

import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;
import com.mycompany.app.audit.AuditService;
import com.mycompany.app.bff.BffService;
import com.mycompany.app.faulty.FaultyService;
import com.mycompany.app.quotes.QuotesService;
import com.mycompany.app.reference.ReferenceService;
import imports.google.provider.GoogleProvider;
import imports.google.provider.GoogleProviderConfig;
import imports.random.provider.RandomProvider;

public class ApplicationStack extends TerraformStack {

    public ApplicationStack(Construct scope, String name, String environment, String project,
            String region) {
        super(scope, name);

        new GoogleProvider(this, "google-cloud",
                GoogleProviderConfig.builder().region("us-east1").project(project).build());

        new RandomProvider(this, "radnom-provider");

        ReferenceService refSvc =
                new ReferenceService(this, "reference-" + environment, project, region);
        QuotesService quotesSvc = new QuotesService(this, "quotes-" + environment, project, region);
        FaultyService faultySvc = new FaultyService(this, "faulty-" + environment, project, region);
        new AuditService(this, "audit-" + environment, project, region);
        new BffService(this, "bff-" + environment, project, region, refSvc.getSvcUrl(),
                quotesSvc.getSvcUrl(), faultySvc.getSvcUrl());
    }

}

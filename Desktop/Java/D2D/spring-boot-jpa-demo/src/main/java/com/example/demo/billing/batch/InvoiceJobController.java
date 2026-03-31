package com.example.demo.billing.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
public class InvoiceJobController {

    private final JobLauncher jobLauncher;
    private final Job monthlyInvoicingJob;

    public InvoiceJobController(JobLauncher jobLauncher, Job monthlyInvoicingJob) {
        this.jobLauncher = jobLauncher;
        this.monthlyInvoicingJob = monthlyInvoicingJob;
    }

    @PostMapping("/run-invoicing")
    public ResponseEntity<String> runInvoicing() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(monthlyInvoicingJob, jobParameters);
            
            return ResponseEntity.ok("Batch job has been invoked successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Batch job failed: " + e.getMessage());
        }
    }
}

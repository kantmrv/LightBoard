package com.dashboard.model;

import com.dashboard.model.enums.ProcessingStatus;
import com.dashboard.model.enums.JobType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "upload_jobs", indexes = {
        @Index(name = "idx_upload_jobs_job_status", columnList = "job_status"),
        @Index(name = "idx_upload_jobs_dataset_id", columnList = "dataset_id"),
        @Index(name = "idx_upload_jobs_created_at", columnList = "created_at")
})
@Getter
@Setter
public class UploadJobs {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //uuid
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    //текущее состояние
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcessingStatus status;

    //тип задачи
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private JobType type;

    //обработано
    @Column(name = "processed_progress")
    private Integer processedProgress = 0;

    //всего строк
    @Column(name = "total_progress")
    private Integer totalProgress = 0;

    @Column(name = "progress_percentage")
    private Double progressPercentage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    //ссылка на
    @Column(name = "result")
    private String result;

    @Column(columnDefinition = "text")
    private String error;


    public void updateProgress(int processed, int total) {
        this.processedProgress = processed;
        this.totalProgress = total;
        if (total > 0) {
            this.progressPercentage = (processed * 100.0) / total;
        }
    }
}

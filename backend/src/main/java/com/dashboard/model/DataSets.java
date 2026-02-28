package com.dashboard.model;


import com.dashboard.model.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "datasets", indexes = {
        @Index(name = "idx_dataset_created_at", columnList = "createdAt"),
        @Index(name = "idx_dataset_status", columnList = "status")
})
public class DataSets{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false)
    private String name;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "column_count")
    private Integer columnCount;


    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @Getter
    @Column(name = "table_name", unique = true)
    private String tableName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column(length = 500)
    private String description;


    @OneToMany(mappedBy = "dataSets", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatasetColumn> columns = new ArrayList<>();

    public void addColumn(DatasetColumn column){
        columns.add(column);
        column.setDataSets(this);
    }

    public void remove(DatasetColumn column){
        columns.remove(column);
        column.setDataSets(this);
    }

}
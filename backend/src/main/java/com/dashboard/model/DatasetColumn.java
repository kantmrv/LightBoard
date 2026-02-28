package com.dashboard.model;

import com.dashboard.model.enums.ColumnType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dataset_columns", indexes = {
        @Index(name = "idx_column_dataset_id", columnList = "dataset_id"),
        @Index(name = "idx_column_position", columnList = "dataset_id, position")
})
@Getter
@Setter
public class DatasetColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long primaryId;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "column_index", nullable = false)
    private int columnIndex;

    @Enumerated(EnumType.STRING)
    public ColumnType detectedType;

    public String postgresType;

    @Column(name = "sample_value")
    private String sampleValue;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "max_value")
    private String maxValue;

    @Column(name = "unique_values_count")
    private Long uniqueValuesCount;

    @Column(name = "is_nullable")
    private Boolean nullable = true;

    @ManyToOne
    @JoinColumn(name = "data_sets_id")
    public DataSets dataSets;


}

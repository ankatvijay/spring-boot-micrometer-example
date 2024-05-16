package com.ankat.consumer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TRACE")
public class Trace {

    @Id
    @Column(name = "TRACE_ID", unique = true, nullable = false)
    private String traceId;

    @Column(name = "TRACE_NAME", nullable = false)
    private String traceName;

    @Column(name = "TRACE_MESSAGE")
    private String traceMessage;

    @Override
    public String toString() {
        return "Trace{" +
                "traceId=" + traceId +
                ", traceMessage='" + traceMessage + '\'' +
                '}';
    }
}

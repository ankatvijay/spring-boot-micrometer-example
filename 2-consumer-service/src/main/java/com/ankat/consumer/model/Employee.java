package com.ankat.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.StringJoiner;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Employee {

    @JsonProperty(value = "emp_id")
    private Integer empId;

    @JsonProperty(value = "emp_name")
    private String empName;

    @JsonProperty(value = "emp_address")
    private String empAddress;

    @Override
    public String toString() {
        return new StringJoiner(", ",  "{", "}")
                .add("\"emp_id\":" + empId)
                .add("\"emp_name\":\"" + empName + "\"")
                .add("\"emp_address\":\"" + empAddress + "\"")
                .toString();
    }
}

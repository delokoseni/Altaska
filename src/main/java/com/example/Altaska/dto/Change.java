package com.example.Altaska.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Change {
    private String field;
    private Object oldValue;
    private Object newValue;
}

package com.blueveery.springrest2ts.examples.model;

import com.blueveery.springrest2ts.examples.model.core.BaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public class PersonDTO extends BaseDTO {
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private AddressDTO homeAddress;
    @JsonIgnore
    private AddressDTO workAddress;
}

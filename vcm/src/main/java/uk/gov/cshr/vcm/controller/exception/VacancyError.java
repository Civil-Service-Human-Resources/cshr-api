package uk.gov.cshr.vcm.controller.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyError implements Serializable {

    private static final long serialVersionUID = 1L;

    private HttpStatus status;

    private String message;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    private SearchStatusCode searchStatusCode;
}

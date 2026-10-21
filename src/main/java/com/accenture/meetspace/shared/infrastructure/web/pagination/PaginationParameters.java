package com.accenture.meetspace.shared.infrastructure.web.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(name = "page", in = ParameterIn.QUERY, description = "Zero-based page index",
                schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")),
        @Parameter(name = "size", in = ParameterIn.QUERY, description = "Number of elements per page",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")),
        @Parameter(name = "sort", in = ParameterIn.QUERY,
                description = "Sort criterion as property or property,asc|desc; repeat it to sort by several "
                        + "properties",
                array = @ArraySchema(schema = @Schema(type = "string")))
})
public @interface PaginationParameters {
}

package app.nail.interfaces.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

/** English: Unified pagination parameter bean with validation and defaults. */
public record PageRequestParams(
        @PositiveOrZero int page,
        @Min(1) @Max(100) int size
) {
    public static PageRequestParams of(Integer page, Integer size) {
        int resolvedPage = page == null ? 0 : Math.max(page, 0);
        int resolvedSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        return new PageRequestParams(resolvedPage, resolvedSize);
    }
}

package app.nail.interfaces.common.dto;

import java.util.List;

/** English: Simple paging response wrapper. */
public record PageResp<T>(List<T> content, long totalElements, int totalPages, int page, int size) {}

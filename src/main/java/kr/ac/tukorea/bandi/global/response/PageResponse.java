package kr.ac.tukorea.bandi.global.response;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
) {

    private static final int MAX_PAGE_SIZE = 100;

    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize,
                                         long totalElements) {
        validate(page, pageSize, totalElements);
        int totalPages = calculateTotalPages(pageSize, totalElements);
        return new PageResponse<>(List.copyOf(items), page, pageSize, totalElements,
                totalPages, totalPages > 0 && page > 0,
                page + 1 < totalPages);
    }

    private static int calculateTotalPages(int pageSize, long totalElements) {
        if (totalElements == 0) {
            return 0;
        }
        return Math.toIntExact((totalElements + pageSize - 1) / pageSize);
    }

    private static void validate(int page, int pageSize, long totalElements) {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || totalElements < 0) {
            throw new IllegalArgumentException("잘못된 페이지 정보입니다.");
        }
    }
}

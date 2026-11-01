package com.kopnus.banking.shared.transform;

import com.kopnus.banking.shared.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageTransform {

    public PageResponse toPage(
            Integer currentPage,
            Long totalElements,
            Long offsetElements,
            Integer totalElementsPerPage,
            Integer totalPages,
            List content,
            Boolean isLast
    ){
        PageResponse response = new PageResponse<>();
        response.setCurrentPage(currentPage);
        response.setTotalElements(totalElements.intValue());
        response.setOffsetElements(offsetElements.intValue());
        response.setTotalElementsPerPage(totalElementsPerPage);
        response.setTotalPages(totalPages);
        response.setContent(content);
        response.setIsLast(isLast);
        return response;
    }

}

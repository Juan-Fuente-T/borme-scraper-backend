package com.lambda.borme_processor.dto

class PaginatedCompaniesDTO {
    boolean success
    String message
    long total
    int totalPages
    int currentPage
    int pageSize
    List<CompanyDTO> companies
}
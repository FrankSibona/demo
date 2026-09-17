package com.example.demo.dto;

import java.util.List;

public record PageDTO<T>(
    List<T> content,      
    int pageNumber,       
    int pageSize,         
    int totalElements,    
    int totalPages        
) {
}

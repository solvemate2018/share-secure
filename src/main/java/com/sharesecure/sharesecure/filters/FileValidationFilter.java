package com.sharesecure.sharesecure.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.sharesecure.sharesecure.services.utils.validation.ValidationService;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class FileValidationFilter implements Filter {

    @Autowired
    ValidationServiceInterface validationService;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            if(validationService.isFileAllowed(multipartRequest.getFile("file"))){
                chain.doFilter(request, response);
            }
            else{
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("Invalid File, please make sure you meet the file requirements!");

                return;
            }
        }
        else
        chain.doFilter(request, response);
    }
}
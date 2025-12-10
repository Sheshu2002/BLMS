package com.example.bank.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException e, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", "Duplicate or invalid data. Please check your inputs.");
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleAnyException(Exception e, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String message = e.getMessage() != null ? e.getMessage() : "Unexpected error occurred.";
        redirectAttributes.addFlashAttribute("errorMessage", message);
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }
} 
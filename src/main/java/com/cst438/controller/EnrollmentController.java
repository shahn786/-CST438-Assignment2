package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(@PathVariable("sectionNo") int sectionNo) {

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            EnrollmentDTO dto = new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getFinalGrade(),
                    enrollment.getUser().getId(),
                    enrollment.getUser().getName(),
                    enrollment.getUser().getEmail(),
                    enrollment.getSection().getCourse().getCourseId(),
                    enrollment.getSection().getCourse().getTitle(),
                    enrollment.getSection().getSecId(),
                    enrollment.getSection().getSectionNo(),
                    enrollment.getSection().getBuilding(),
                    enrollment.getSection().getRoom(),
                    enrollment.getSection().getTimes(),
                    enrollment.getSection().getCourse().getCredits(),
                    enrollment.getSection().getTerm().getYear(),
                    enrollment.getSection().getTerm().getSemester()
            );
            enrollmentDTOs.add(dto);
        }
            return enrollmentDTOs;
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {

        for (EnrollmentDTO dto : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Enrollment not found"));

            if (!enrollment.getUser().getType().equals("INSTRUCTOR")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only INSTRUCTOR can update enrollment grades");
            }

            enrollment.setFinalGrade(dto.grade());
            enrollmentRepository.save(enrollment);
        }
    }
}


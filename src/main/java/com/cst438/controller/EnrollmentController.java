package com.cst438.controller;

import com.cst438.domain.*;
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

    @Autowired
    SectionRepository sectionRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(@PathVariable("sectionNo") int sectionNo) {

        Section section = sectionRepository.findById(sectionNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        if (section == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found.");
        }

        String instructorEmail = section.getInstructorEmail();
        int year = section.getTerm().getYear();
        String semester = section.getTerm().getSemester();

        // Check if the user is the instructor of the section
        if (sectionRepository.findByInstructorEmailAndYearAndSemester(instructorEmail, year, semester) != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the section instructor can view enrollments.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        if (enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No enrollments found for this section.");
        }

        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment e : enrollments) {
            EnrollmentDTO dto = new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    e.getStudent().getId(),
                    e.getStudent().getName(),
                    e.getStudent().getEmail(),
                    e.getSection().getCourse().getCourseId(),
                    e.getSection().getCourse().getTitle(),
                    e.getSection().getSecId(),
                    e.getSection().getSectionNo(),
                    e.getSection().getBuilding(),
                    e.getSection().getRoom(),
                    e.getSection().getTimes(),
                    e.getSection().getCourse().getCredits(),
                    e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester()
            );
            enrollmentDTOs.add(dto);
        }
            return enrollmentDTOs;
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {

        if (dlist.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment list is empty.");
        }

        // Check if the user is the instructor for the enrollment's section
        Enrollment firstEnrollment = enrollmentRepository.findById(dlist.get(0).enrollmentId()).orElse(null);
        if (firstEnrollment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "First enrollment not found.");
        }

        Section section = firstEnrollment.getSection();

        if (sectionRepository.findByInstructorEmailAndYearAndSemester(section.getInstructorEmail(), section.getTerm().getYear(), section.getTerm().getSemester()) != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the section instructor can update enrollment grades.");
        }

        for (EnrollmentDTO dto : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
            if (enrollment == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found for ID: " + dto.enrollmentId());
            }
            enrollment.setGrade(dto.grade());
            enrollmentRepository.save(enrollment);
        }
    }
}

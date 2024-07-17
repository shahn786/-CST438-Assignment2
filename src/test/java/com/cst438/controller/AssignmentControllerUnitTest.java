package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.cst438.test.utils.TestUtils.*;
import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssignmentRepository assignmentRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private GradeRepository gradeRepository;

    @MockBean
    private SectionRepository sectionRepository;


    //Test to Add a new Assignment
    @Test
    public void addAssignment() throws Exception {

        MockHttpServletResponse response;

        // Create DTO with data for a new assignment.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment 1",
                "2024-02-16",
                "cst438",
                1,
                10
        );

        // Issue a HTTP POST request to add a new assignment
        response = mvc.perform(
                        post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // Check the response code for 200 OK
        assertEquals(200, response.getStatus());

        // Convert response data from JSON to DTO
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        // Primary key should have a non-zero value from the database
        assertNotEquals(0, result.id());
        // Check other fields of the DTO for expected values
        assertEquals("Assignment 1", result.title());

        // Check the database for the newly added assignment
        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("cst438", a.getSection().getCourse().getCourseId());

        // Clean up after test: Issue HTTP DELETE request for the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/"+result.id()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // Check database to ensure assignment is deleted
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);  // Assignment should not be found after delete
    }


    // Test to check if adding a new assignment fails when outside term limits
    @Test
    public void addAssignmentFailsOutsideTerm() throws Exception {
        MockHttpServletResponse response;

        // Create DTO with data for a new assignment.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment 2",
                "2025-12-01", // Due date outside of term
                "cst438",
                10,
                1
        );

        // Issue a HTTP POST request to add a new assignment
        response = mvc.perform(
                        post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // Check the response code for 400 BAD REQUEST
        assertEquals(400, response.getStatus());

        // Check the expected error message
        String errorMessage = response.getErrorMessage();
        assertEquals("Assignment due date is outside of the course term", errorMessage);
    }


    // Test to add an assignment with invalid section number
    @Test
    public void addAssignmentWithInvalidSectionNumber() throws Exception {
        // Create a DTO with data for a new assignment, including an invalid section number
        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "New Assignment",
                "2024-09-30", // Due date within the term
                "cst438", // Valid course ID
                1, // Valid section ID
                999 // Invalid section number
        );

        // Perform a HTTP POST request to add a new assignment
        MockHttpServletResponse response = mvc.perform(
                        post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn().getResponse();

        // Verify the response status code
        assertEquals(404, response.getStatus());
    }


    // Test to grade a new assignment and verify the update
    @Test
    public void testGradeAssignmentAndVerifyUpdated() throws Exception {

        MockHttpServletResponse response;

        // Create DTO with data for a new assignment.
        AssignmentDTO a = new AssignmentDTO(
                0,
                "Assignment 1",
                "2024-02-16",
                "cst438",
                1,
                10
        );

        // Perform the POST request to create the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(a)))
                .andReturn()
                .getResponse();

        // Mock the behavior of the EnrollmentRepository to return a list of enrollments
        User student = new User();
        student.setId(1);
        student.setName("John Doe");
        student.setEmail("john.doe@example.com");

        Enrollment e = new Enrollment();
        e.setEnrollmentId(3);
        e.setStudent(student);

        List<Enrollment> enrollments = List.of(e);

        // Mock the repository to return the populated enrollments list
        Mockito.when(enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(10))
                .thenReturn(enrollments);

        // Loop through the list of students and assign grades
        for (Enrollment enrollment : enrollments) {
            GradeDTO grade = new GradeDTO(
                    0,
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    a.title(),
                    a.courseId(),
                    a.secId(),
                    90
            );

            response = mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/grades")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(grade)))
                    .andReturn()
                    .getResponse();
        }

        // Verify that the grades were updated in the database
        for (Enrollment enrollment : enrollments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), a.id());
            assertEquals(90, grade.getScore());
        }
    }



}
package com.cst438.controller;

import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    public void addSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the section is inserted.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                "Capstone",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        SectionDTO result = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.secNo());
        // check other fields of the DTO for expected values
        assertEquals("cst499", result.courseId());

        // check the database
        Section s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNull(s);  // section should not be found after delete
    }

    @Test
    public void addSectionFailsBadCourse( ) throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                "Software Engineering",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // response should be 400, BAD_REQUEST
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("course not found cst599", message);

    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Check whether student is able to enroll
    @Test
    public void enrollStudentInSection() throws Exception {

        // Construct an enrollment DTO with studentId and sectionId
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
                0,
                null,  // grade initially null
                3,
                "thomas edison",
                "tedison@csumb.edu",
                "cst363",
                "Introduction to Database",
                1,
                8,
                "052",
                "104",
                "M W 10:00-11:50",
                4,
                2024,
                "Spring"
        );

        // Send PUT request to enrollments endpoint
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(Collections.singletonList(enrollmentDTO))))
                .andReturn()
                .getResponse();

        // Verify response status is 200 OK
        assertEquals(200, response.getStatus());

        // Check database to ensure student is enrolled in the section
        Section section = sectionRepository.findById(enrollmentDTO.sectionId()).orElse(null);
        assertNotNull(section);
        assertTrue(section.getEnrollments().stream().anyMatch(enrollment -> enrollment.getStudent().getId() == enrollmentDTO.studentId()));
    }

    // Check enrolled when already enrolled
    @Test
    public void EnrollWhenAlreadyEnrolled() throws Exception {

        // Construct an enrollment DTO with studentId and sectionId
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
                3,
                null,  // grade initially null
                3,
                "thomas edison",
                "tedison@csumb.edu",
                "cst438",
                "Software Engineering",
                1,
                10,
                "052",
                "222",
                "T Th 12:00-1:50",
                4,
                2024,
                "Spring"
        );

        // Send PUT request to enrollments endpoint
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(Collections.singletonList(enrollmentDTO))))
                .andReturn()
                .getResponse();

        // Verify response status is 200 OK
        assertEquals(200, response.getStatus());

        // Check database to ensure student is enrolled in the section
        Section section = sectionRepository.findById(enrollmentDTO.sectionId()).orElse(null);
        assertNotNull(section);
        assertTrue(section.getEnrollments().stream().anyMatch(enrollment -> enrollment.getStudent().getId() == enrollmentDTO.studentId()));
    }


}

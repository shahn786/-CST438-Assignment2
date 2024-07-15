package com.cst438.controller;

import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
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

import static org.junit.jupiter.api.Assertions.*;

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
                "",
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
    public void addSectionFailsBadCourse() throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                "",
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

    @Test
    public void addMultipleSections() throws Exception {

        MockHttpServletResponse response;

        // create DTOs with data for new sections.
        SectionDTO section1 = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                "",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        SectionDTO section2 = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                "",
                2,
                "052",
                "105",
                "M W 3:00-4:50 pm",
                "Alice Smith",
                "asmith@csumb.edu"
        );

        // issue http POST request to add the first section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section1)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        SectionDTO result1 = fromJsonString(response.getContentAsString(), SectionDTO.class);
        assertNotEquals(0, result1.secNo());
        assertEquals("cst499", result1.courseId());

        // issue http POST request to add the second section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section2)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        SectionDTO result2 = fromJsonString(response.getContentAsString(), SectionDTO.class);
        assertNotEquals(0, result2.secNo());
        assertEquals("cst499", result2.courseId());

        // check the database for both sections
        Section s1 = sectionRepository.findById(result1.secNo()).orElse(null);
        assertNotNull(s1);
        assertEquals("cst499", s1.getCourse().getCourseId());

        Section s2 = sectionRepository.findById(result2.secNo()).orElse(null);
        assertNotNull(s2);
        assertEquals("cst499", s2.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for both sections
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result1.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result2.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s1 = sectionRepository.findById(result1.secNo()).orElse(null);
        assertNull(s1);

        s2 = sectionRepository.findById(result2.secNo()).orElse(null);
        assertNull(s2);
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
}

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

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Test
    public void addSection() throws Exception {
        SectionDTO section = createSectionDTO("cst499", "W F 1:00-2:50 pm", "Joshua Gross", "jgross@csumb.edu");

        SectionDTO result = addSectionRequest(section);
        assertSection(result, "cst499");
        deleteSectionRequest(result.secNo());
    }

    @Test
    public void addSectionFailsBadCourse() throws Exception {
        SectionDTO section = createSectionDTO("cst599", "W F 1:00-2:50 pm", "Joshua Gross", "jgross@csumb.edu");

        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        assertEquals("course not found cst599", response.getErrorMessage());
    }

    @Test
    public void addMultipleSections() throws Exception {
        SectionDTO section1 = createSectionDTO("cst499", "W F 1:00-2:50 pm", "Joshua Gross", "jgross@csumb.edu");
        SectionDTO section2 = createSectionDTO("cst499", "M W 3:00-4:50 pm", "Alice Smith", "asmith@csumb.edu");

        SectionDTO result1 = addSectionRequest(section1);
        assertSection(result1, "cst499");

        SectionDTO result2 = addSectionRequest(section2);
        assertSection(result2, "cst499");

        deleteSectionRequest(result1.secNo());
        deleteSectionRequest(result2.secNo());
    }

    private SectionDTO createSectionDTO(String courseId, String meetingTimes, String instructorName, String instructorEmail) {
        return new SectionDTO(
                0,
                2024,
                "Spring",
                courseId,
                "",
                1,
                "052",
                "104",
                meetingTimes,
                instructorName,
                instructorEmail
        );
    }

    private SectionDTO addSectionRequest(SectionDTO section) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());
        return fromJsonString(response.getContentAsString(), SectionDTO.class);
    }

    private void assertSection(SectionDTO section, String expectedCourseId) {
        assertNotEquals(0, section.secNo());
        assertEquals(expectedCourseId, section.courseId());

        Section s = sectionRepository.findById(section.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals(expectedCourseId, s.getCourse().getCourseId());
    }

    private void deleteSectionRequest(int secNo) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/" + secNo))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());
        assertNull(sectionRepository.findById(secNo).orElse(null));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

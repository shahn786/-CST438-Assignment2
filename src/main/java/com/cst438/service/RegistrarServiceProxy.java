package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TermRepository termRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        processMessage(message);

    }


    public void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }
    public static String asJsonString(final Object obj) {
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
    @Transactional
    public void processMessage(String msg) {
        String[] parts = msg.split(" ", 2);
        String action = parts[0];

        if (action.equals("addCourse")) {
            CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
            Course c = new Course();
            c.setCourseId(dto.courseId());
            c.setTitle(dto.title());
            c.setCredits(dto.credits());
            courseRepository.save(c);

        } else if (action.equals("deleteCourse")) {
            courseRepository.deleteById(parts[1]);

        } else if (action.equals("updateCourse")) {
            CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
            Course c = courseRepository.findById(dto.courseId()).orElse(null);
            if (c != null) {
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            }

        } else if (action.equals("addSection")) {
            SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
            Section s = new Section();
            s.setSectionNo(dto.secNo());
            Course c = courseRepository.findById(dto.courseId()).orElse(null);
            if (c != null) {
                s.setCourse(c);
                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                s.setInstructor_email(dto.instructorEmail());
                sectionRepository.save(s);
            }

        } else if (action.equals("deleteSection")) {
            sectionRepository.deleteById(Integer.parseInt(parts[1]));

        } else if (action.equals("updateSection")) {
            SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
            Section s = sectionRepository.findById(dto.secNo()).orElse(null);
            if (s != null) {
                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                if (c != null) {
                    s.setCourse(c);
                    s.setSecId(dto.secId());
                    s.setBuilding(dto.building());
                    s.setRoom(dto.room());
                    s.setTimes(dto.times());
                    s.setInstructor_email(dto.instructorEmail());
                    sectionRepository.save(s);
                }
            }

        } else if (action.equals("addUser")) {
            UserDTO dto = fromJsonString(parts[1], UserDTO.class);
            User u = new User();
            u.setId(dto.id());
            u.setName(dto.name());
            u.setEmail(dto.email());
            u.setType(dto.type());
            userRepository.save(u);

        } else if (action.equals("deleteUser")) {
            userRepository.deleteById(Integer.parseInt(parts[1]));

        } else if (action.equals("updateUser")) {
            UserDTO dto = fromJsonString(parts[1], UserDTO.class);
            User u = userRepository.findById(dto.id()).orElse(null);
            if (u != null) {
                u.setName(dto.name());
                u.setEmail(dto.email());
                u.setType(dto.type());
                userRepository.save(u);
            }

        } else if (action.equals("addEnrollment")) {
            EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
            Enrollment e = new Enrollment();
            e.setEnrollmentId(dto.enrollmentId());
            Section s = sectionRepository.findById(dto.sectionNo()).orElse(null);
            User u = userRepository.findById(dto.studentId()).orElse(null);
            if (s != null && u != null) {
                e.setSection(s);
                e.setStudent(u);
                e.setGrade(dto.grade());
                enrollmentRepository.save(e);
            }

        } else if (action.equals("deleteEnrollment")) {
            enrollmentRepository.deleteById(Integer.parseInt(parts[1]));

        } else if (action.equals("updateEnrollment")) {
            EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
            Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
            if (e != null) {
                Section s = sectionRepository.findById(dto.sectionNo()).orElse(null);
                User u = userRepository.findById(dto.studentId()).orElse(null);
                if (s != null && u != null) {
                    e.setSection(s);
                    e.setStudent(u);
                    e.setGrade(dto.grade());
                    enrollmentRepository.save(e);
                }
            }

        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

}
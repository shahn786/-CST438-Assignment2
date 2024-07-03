package com.cst438;

import com.cst438.domain.Enrollment;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Cst438Assignment2Main {

	public static void main(String[] args) {
		SpringApplication.run(Cst438Assignment2Main.class, args);
	}

}


//List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
//
//List<EnrollmentDTO> transcript = new ArrayList<>();
//       for (Enrollment e : enrollments) {
//		transcript.add(new EnrollmentDTO(
//		e.getEnrollmentId(),
//                   e.getGrade(),
//                   e.getStudent().getId(),
//                   e.getStudent().getName(),
//                   e.getStudent().getEmail(),
//                   e.getSection().getCourse().getCourseId(),
//                   e.getSection().getCourse().getTitle(),
//                   e.getSection().getSecId(),
//                   e.getSection().getSectionNo(),
//                   e.getSection().getBuilding(),
//                   e.getSection().getRoom(),
//                   e.getSection().getTimes(),
//                   e.getSection().getCourse().getCredits(),
//                   e.getSection().getTerm().getYear(),
//                   e.getSection().getTerm().getSemester()
//           ));
//				   }
//				   return transcript;
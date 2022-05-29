package com.cst438.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cst438.domain.CourseDTOG;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;


@RestController
public class CourseController {
	
	@Autowired
	EnrollmentRepository enrollmentRepository;

	
	/*
	 * endpoint used by gradebook service to transfer final course grades
	 */
	@PutMapping("course/{course_id}")
	@Transactional
	public void updateCourseGrades( @RequestBody CourseDTOG courseDTO, @PathVariable("course_id") int course_id) {
		
		//TODO  complete this method in homework 4
		
		int size = courseDTO.grades.size();
		
		for (int i = 0; i< size; i++)
		{
			String grade= courseDTO.grades.get(i).grade;
			String email = courseDTO.grades.get(i).student_email;
			
			System.out.println(grade);
			System.out.println(email);
			
			//Enrollment en = new Enrollment ();
			//courseDTO.course_id = course_id;
			
			// check for email 
			
			if (enrollmentRepository.findByEmailAndCourseId( email  , course_id) != null)
			{
				enrollmentRepository.findByEmailAndCourseId( email  , course_id).setCourseGrade(grade);
				
				enrollmentRepository.save(enrollmentRepository.findByEmailAndCourseId( email  , course_id));
				
				System.out.println(enrollmentRepository.findByEmailAndCourseId( email  , course_id).getCourseGrade());
			}

			
			
		}
		

		

		
		
		//courseDTO.grades.get(0).
		
	}

}

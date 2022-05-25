package com.cst438.service;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.CourseDTOG;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;


public class GradebookServiceMQ extends GradebookService {
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	Queue gradebookQueue;
	
	
	public GradebookServiceMQ() {
		System.out.println("MQ grade book service");
	}
	
	// send message to grade book service about new student enrollment in course
	@Override
	public void enrollStudent(String student_email, String student_name, int course_id) {
		 
		//TODO  complete this method in homework 4
		EnrollmentDTO enrollmentDTO = new EnrollmentDTO (student_email,student_name,course_id);
		rabbitTemplate.convertAndSend(gradebookQueue.getName(), enrollmentDTO );
	}
	
	@RabbitListener(queues = "registration-queue")
	@Transactional
	public void receive(CourseDTOG courseDTOG) {
		
		//TODO  complete this method in homework 4
		
		
		int size = courseDTOG.grades.size();
		 int course_id = courseDTOG.course_id;
		for (int i = 0; i< size; i++)
		{
			String grade= courseDTOG.grades.get(i).grade;
			String email = courseDTOG.grades.get(i).student_email;
			
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
		
	}
	
	

}

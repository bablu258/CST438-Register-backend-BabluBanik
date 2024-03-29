package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000/", "https://frontend258.herokuapp.com"})
public class ScheduleController {
	
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	StudentRepository studentRepository;
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	GradebookService gradebookService;
	
	@Value("${frontend.post.login.url}")
	String redirect_url;
	
	
	
	
	
	
	@GetMapping("/")

	
	public boolean VerifyAdminUser( @AuthenticationPrincipal OAuth2User principal) {
		
		
		Map attributes = principal.getAttributes();
		String Teacher_email = (String) attributes.get("email");
		
		System.out.println("\n\n\n\n\n"+ "credentials here  em "+ Teacher_email+("\n\n\n\n\n"));
		System.out.println("\n\n\n\n\n"+ "num of repo "+ courseRepository.count()+("\n\n\n\n\n"));
		
		boolean exist = false;

		for (Course course : courseRepository.findAll())
		{
			
				System.out.println("\n\n\n\n\n"+ "from DB "+ course.getInstructor().trim()+("\n\n\n\n\n"));
				System.out.println("\n\n\n\n\n"+ "credentials here  em "+ Teacher_email+("\n\n\n\n\n"));
				
				if (course.getInstructor().trim().equalsIgnoreCase(Teacher_email.trim()))
				{
					exist = true;
				
					return true;
					
				}
					
			
		}
		
		return false;
		

	}
	
	
	



	
	
	
	/*
	 * get current schedule for student.
	 */
	@GetMapping("/schedule")
	
	public ScheduleDTO getSchedule( @RequestParam("year") int year, @RequestParam("semester") String semester , @AuthenticationPrincipal OAuth2User principal) {
		
		//String student_email = principal.getAttribute("email");//"test@csumb.edu";   // student's email 
		
		
		Map attributes = principal.getAttributes();
		String student_email = (String) attributes.get("email");
		
		
		if (studentRepository.findByEmail(student_email) == null) {
	         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not Authorized to view!");
	      }
		
		
		Student student = studentRepository.findByEmail(student_email);
		if (student != null) {
			List<Enrollment> enrollments = enrollmentRepository.findStudentSchedule(student_email, year, semester);
			ScheduleDTO sched = createSchedule(year, semester, student, enrollments);
			return sched;
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student not found. " );
		}
	}
	
	
	
	@GetMapping("/getStudent")
	public Student[] getStudent( ) {
		

			List<Student> students = (List<Student>) studentRepository.findAll();
			
			Student[] arraySte = new Student[students.size()];
			
			for (int i =0; i <students.size(); i++ )
			{
				
				arraySte[i]=students.get(i);
				
				
			}
				
			
			
			//return students;
			
			return arraySte;
	
	}

	
	@PostMapping("/schedule")
	@Transactional
	public ScheduleDTO.CourseDTO addCourse( @RequestBody ScheduleDTO.CourseDTO courseDTO, @AuthenticationPrincipal OAuth2User principal   ) { 
		
		//String student_email = "test@csumb.edu";   // student's email 
		
		
		
		Map attributes = principal.getAttributes();
		String student_email = (String) attributes.get("email");
		
		
		if (studentRepository.findByEmail(student_email) == null) {
	         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not Authorized to view!");
	      }
		
		
		
		
		Student student = studentRepository.findByEmail(student_email);
		Course course  = courseRepository.findByCourse_id(courseDTO.course_id);
		
		
		
		
		
		
		
		// student.status
		// = 0  ok to register
		// != 0 hold on registration.  student.status may have reason for hold.
		
		if (student!= null && course!=null && student.getStatusCode()==0) {
			// TODO check that today's date is not past add deadline for the course.
			Enrollment enrollment = new Enrollment();
			enrollment.setStudent(student);
			enrollment.setCourse(course);
			enrollment.setYear(course.getYear());
			enrollment.setSemester(course.getSemester());
			Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
			
			gradebookService.enrollStudent(student_email, student.getName(), course.getCourse_id());
			
			ScheduleDTO.CourseDTO result = createCourseDTO(savedEnrollment);
			return result;
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Course_id invalid or student not allowed to register for the course.  "+courseDTO.course_id);
		}
		
	}
	
	@DeleteMapping("/schedule/{enrollment_id}")
	@Transactional
	public void dropCourse(  @PathVariable int enrollment_id ,@AuthenticationPrincipal OAuth2User principal ) {
		
		//String student_email = "test@csumb.edu";   // student's email 
		
		Map attributes = principal.getAttributes();
		String student_email = (String) attributes.get("email");
		
		
		if (studentRepository.findByEmail(student_email) == null) {
	         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not Authorized to view!");
	      }
		
		
		// TODO  check that today's date is not past deadline to drop course.
		
		Enrollment enrollment = enrollmentRepository.findById(enrollment_id);
		
		// verify that student is enrolled in the course.
		if (enrollment!=null && enrollment.getStudent().getEmail().equals(student_email)) {
			// OK.  drop the course.
			 enrollmentRepository.delete(enrollment);
		} else {
			// something is not right with the enrollment.  
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Enrollment_id invalid. "+enrollment_id);
		}
	}
	
	/* 
	 * helper method to transform course, enrollment, student entities into 
	 * a an instance of ScheduleDTO to return to front end.
	 * This makes the front end less dependent on the details of the database.
	 */
	private ScheduleDTO createSchedule(int year, String semester, Student s, List<Enrollment> enrollments) {
		ScheduleDTO result = new ScheduleDTO();
		result.semester = semester;
		result.year = year;
		result.student_email = s.getEmail();
		result.student_id = s.getStudent_id();
		ArrayList<ScheduleDTO.CourseDTO> courses = new ArrayList<>();
		
		for (Enrollment e : enrollments) {
			ScheduleDTO.CourseDTO courseDTO = createCourseDTO(e);
			courses.add(courseDTO);
		}
		result.courses = courses;
		return result;
	}
	
	private ScheduleDTO.CourseDTO createCourseDTO(Enrollment e) {
		ScheduleDTO.CourseDTO courseDTO = new ScheduleDTO.CourseDTO();
		Course c = e.getCourse();
		courseDTO.id =e.getEnrollment_id();
		courseDTO.building = c.getBuilding();
		courseDTO.course_id = c.getCourse_id();
		courseDTO.endDate = c.getEnd().toString();
		courseDTO.instructor = c.getInstructor();
		courseDTO.room = c.getRoom();
		courseDTO.section = c.getSection();
		courseDTO.startDate = c.getStart().toString();
		courseDTO.times = c.getTimes();
		courseDTO.title = c.getTitle();
		courseDTO.grade = e.getCourseGrade();
		return courseDTO;
	}
	
	private StudentDTO.StuDTO createSTDTO(Student e) {
		StudentDTO.StuDTO StDTO = new StudentDTO.StuDTO();
		
		
		//StDTO.student_id = e.getStudent_id();
		StDTO.email = e.getEmail();
		StDTO.name = e.getName();
		StDTO.statueCode = e.getStatusCode();
		
		
		return StDTO;
	}
	
	
	// new code
	
	@PostMapping("/addStudent")
	@Transactional
	public StudentDTO.StuDTO addStudent( @RequestBody Student student  ) { 

		student.setStatusCode(0);
		
		if (student!= null && studentRepository.findByEmail(student.getEmail()) == null) {
			
			Student Savedstudent = studentRepository.save(student);
			
			//gradebookService.enrollStudent(student_email, student.getName(), course.getCourse_id());
			
			//ScheduleDTO result = createSTDTO(Savedstudent);
			
			StudentDTO.StuDTO result = createSTDTO(Savedstudent);
			
			
			return result;
			
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with same email is already present in the system.  "+student.getEmail());
		}
		
	}
	
	
	
	@PostMapping("/RegistrationHold")
	@Transactional
	public StudentDTO.StuDTO RegistrationHold( @RequestBody Student student  ) { 

		if (studentRepository.findByEmail(student.getEmail()) != null) {
			
			Student Updatedstudent = studentRepository.findByEmail(student.getEmail());
		
			if (Updatedstudent.getStatusCode() == 0)
				Updatedstudent.setStatusCode(1);
			else
				Updatedstudent.setStatusCode(0);
				
			studentRepository.save(Updatedstudent);
			//gradebookService.enrollStudent(student_email, student.getName(), course.getCourse_id());
			
			StudentDTO.StuDTO result = createSTDTO(Updatedstudent);
			return result;
			
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with the given email does not exist in the system.  "+student.getEmail());
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
}

package com.cst438.domain;


public class StudentDTO {
	
	public static class StuDTO {
		
		
		public int student_id;
		public int statueCode;
		public String email;
		public String name;
		
		
		@Override
		public String toString() {
			return "[email=" + email + ", name=" + name + ", statue_code=" + statueCode+"  ]";
		}
		
	}

	public int student_id;
	public String student_email;
	
	@Override
	public String toString() {
		return "StuDTO [student_id=" + student_id + ", email=[" + student_email + "] ]";
	}

}

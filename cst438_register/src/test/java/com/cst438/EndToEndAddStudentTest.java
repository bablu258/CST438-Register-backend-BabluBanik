package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;


@SpringBootTest
public class EndToEndAddStudentTest {
	
	public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

	public static final String URL = "https://frontend258.herokuapp.com";

	public static final String TEST_USER_EMAIL = "rkhan@csumb.edu";
	
	public static final String TEST_USER_Name = "Robert Khan";

	public static final int SLEEP_DURATION = 1000; // 1 second.
	
	@Autowired
	StudentRepository studentRepository;
	
	
	@Test
	public void addCourseTest() throws Exception {

		Student x = null;
		do {
			x = studentRepository.findByEmail(TEST_USER_EMAIL);
			if (x != null)
				studentRepository.delete(x);
		} while (x != null);

		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		try {

			driver.get(URL);
			Thread.sleep(SLEEP_DURATION);

			// click on add student button
		    driver.findElement(By.linkText("Add Student")).click();
		    Thread.sleep(SLEEP_DURATION);
		    // click on add student button 
		    driver.findElement(By.xpath("//button[@type='button']")).click();
	
		    Thread.sleep(SLEEP_DURATION);
		    // clear input field and type test user email 
		    driver.findElement(By.id("mui-21")).clear();
		    driver.findElement(By.id("mui-21")).sendKeys(TEST_USER_Name);
		    
		    driver.findElement(By.id("mui-22")).clear();
		    driver.findElement(By.id("mui-22")).sendKeys(TEST_USER_EMAIL);

			// click on save button
			driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Cancel'])[1]/following::button[1]")).click();
			Thread.sleep(SLEEP_DURATION);

			// Get student name from UI and verify if it matches with the test name
			Student student = studentRepository.findByEmail(TEST_USER_EMAIL);
			String StUname = driver.findElement(By.xpath("//div[@id='root']/div/div/div/div[3]/div/div[2]/div[2]/div/div/div/div[5]/div/div")).getText();
			assertEquals(TEST_USER_Name, StUname);

			// verify that student row has been inserted to database.
			assertNotNull(student, "Student not found in database.");

		} catch (Exception ex) {
			throw ex;
		} finally {

			// clean up database.
			Student student = studentRepository.findByEmail(TEST_USER_EMAIL);
			if (student != null)
				studentRepository.delete(student);

			driver.quit();
		}

	}

}

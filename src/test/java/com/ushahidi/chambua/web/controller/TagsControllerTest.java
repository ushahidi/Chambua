package com.ushahidi.chambua.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link com.ushahidi.chambua.web.controller.TagsController}
 * 
 * @author ekala
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
		"classpath:chambua-servlet.xml"
})
@ActiveProfiles(value = {"test"})
public class TagsControllerTest {

	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc; 

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	/**
	 * Tests POST requests to /v1/tags 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetTags() throws Exception {
		// The input text to be tagged
		String tagInput = "{\"text\":\"The Syrian government vociferously denied mounting any " +
				"chemical attack, and its ally, Russia, blamed Syrian rebels for launching a " +
				"rocket with an unknown chemical agent that had caused civilian casualties, " +
				"calling it a preplanned effort to accuse the government of President Bashar " +
				"al-Assad of using chemical weapons. A team of weapons investigators sent by " +
				"the United Nations arrived in the country on Sunday to begin looking into " +
				"several other reports of chemical weapons.\"}";

		this.mockMvc.perform(post("/v1/tags")
				.content(tagInput)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json;charset=UTF-8"))
			.andExpect(jsonPath("$.people", contains("Bashar")))
			.andExpect(jsonPath("$.places[0].name").value("Russia"))
			.andExpect(jsonPath("$.organizations", contains("United Nations")));
	}
}

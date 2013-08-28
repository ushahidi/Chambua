/**
 * Chambua - Simplified text analysis 
 * Copyright(C) 2013, Ushahidi Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ushahidi.chambua.web.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
			.andExpect(jsonPath("$.places[0].place_type").value("Country"))
			.andExpect(jsonPath("$.organizations", contains("United Nations")));
	}
}

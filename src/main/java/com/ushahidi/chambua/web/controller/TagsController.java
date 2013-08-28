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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ushahidi.chambua.service.EntityExtractorService;
import com.ushahidi.chambua.web.dto.APIResponseDTO;
import com.ushahidi.chambua.web.exception.BadRequestException;

@Controller
@RequestMapping("/v1")
public class TagsController {

	@Autowired
	private EntityExtractorService entityExtractor;

	@RequestMapping(value="/tags", method = RequestMethod.POST)
	@ResponseBody
	public APIResponseDTO getTags(@RequestBody Map<String, String> body) {
		// Check for the "text" parameter
		if (!body.containsKey("text"))
			throw new BadRequestException("The 'text' parameter is missing");

		return entityExtractor.getEntities(body.get("text"));
	}
}

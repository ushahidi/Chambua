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

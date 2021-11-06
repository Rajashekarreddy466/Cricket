package com.cricket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cricket.service.CricketService;

@RestController
@RequestMapping("api/cricket")
public class CricketController {
	
	@Autowired
	public CricketService crickerService;

	@GetMapping("/welcome")
	public String getWelcome() {
		return "Welcome Home";
	}
	
	@PostMapping(path = "/top11players", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTop11Players(@RequestParam(defaultValue = "5") int no_of_batsmen, 
								@RequestParam(defaultValue = "4") int no_of_bowlers, 
								@RequestParam(defaultValue = "2") int no_of_allrounders) 
	{
		//Here lets have validation on the input data.
		//like minimum number of batsmen, bowlers, allrounders and 
		//sum of all of them should not exceed more than 11.
		int TotalPlayers = no_of_batsmen+no_of_allrounders+no_of_bowlers;
		String mainString = "";
		if(TotalPlayers > 11) {
			return "Maximum You can mention 11 players including all the roles.";
		}
		else {
			if(TotalPlayers < 11) {
				mainString += "Please mention exact 11 players count with different roles to form a team";
			}
			else if(TotalPlayers == 11) {
				if(no_of_batsmen<1) {
					mainString += "You need to have atleast one batsmen ";
				}
				if(no_of_bowlers<2) {
					mainString += "You need to have bowler count of atleast 2 ";
				}
				if(no_of_allrounders<1) {
					mainString += "You need to have allrounder count of atleast 1 ";
				}
			}
		}
		
		if(!mainString.isEmpty()) {
			return mainString;
		}
		else {
			//return "Success and will proceed further for fetching top mentioned players";
			//Here we need to proceed further for getting top players from api.
			return crickerService.gettopplayers(no_of_batsmen, no_of_bowlers, no_of_allrounders);
		}
	}
	
}
